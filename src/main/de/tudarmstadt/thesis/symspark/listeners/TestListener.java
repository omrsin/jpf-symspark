package de.tudarmstadt.thesis.symspark.listeners;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.concolic.PCAnalyzer;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicConstraintsGeneral;
import gov.nasa.jpf.symbc.sequences.SequenceChoiceGenerator;
import gov.nasa.jpf.symbc.string.StringSymbolic;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NoUncaughtExceptionsProperty;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;

public class TestListener extends PropertyListenerAdapter implements PublisherExtension {
	
	Set<Vector<String>> values = new LinkedHashSet<Vector<String>>();
	
	// this set will store all the method sequences.
	// will be printed at last.
	// 'methodSequences' is a set of 'methodSequence's
	// A single 'methodSequence' is a vector of invoked 'method's along a path
	// A single invoked 'method' is represented as a String.
	Set<Vector> methodSequences = new LinkedHashSet<Vector>();
	
	// Name of the class under test
	String className ="";
	
	// custom marker to mark error strings in method sequences
	private final static String exceptionMarker = "##EXCEPTION## ";
	
	public TestListener(Config conf, JPF jpf) {
		jpf.addPublisherExtension(ConsolePublisher.class, this);
	}
	
	@Override
	public void propertyViolated (Search search){
		System.out.println("--------->property violated");
		VM vm = search.getVM();
		SystemState ss = vm.getSystemState();
		ChoiceGenerator<?> cg = vm.getChoiceGenerator();
		if (!(cg instanceof PCChoiceGenerator)){
			ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
			while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
				prev_cg = prev_cg.getPreviousChoiceGenerator();
			}
			cg = prev_cg;
		}
		Property prop = search.getLastError().getProperty();
		String errAnn="";
		if (prop instanceof NoUncaughtExceptionsProperty) {
			String exceptionClass=((NoUncaughtExceptionsProperty)prop).getUncaughtExceptionInfo().getExceptionClassname();
			errAnn = "(expected = "+ exceptionClass +".class)";
		}
	
		String error = search.getLastError().getDetails();
		error = "\"" + error.substring(0,error.indexOf("\n")) + "...\"";
	
		if ((cg instanceof PCChoiceGenerator) &&
				      ((PCChoiceGenerator) cg).getCurrentPC() != null){
	
			PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();
			System.out.println("pc "+ pc.count() + " "+pc);
	
			//solve the path condition
			if (SymbolicInstructionFactory.concolicMode) { //TODO: cleaner
				SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
				PCAnalyzer pa = new PCAnalyzer();
				pa.solve(pc,solver);
			}
			else
				pc.solve();
	
			// get the chain of choice generators.
			ChoiceGenerator<?> [] cgs = ss.getChoiceGenerators();
			Vector<String> methodSequence = getMethodSequence(cgs);
			// Now append the error String and then add methodSequence to methodSequences
			// prefix the exception marker to distinguish this from
			// an invoked method.
			if (errAnn!="")
				methodSequence.add(0,errAnn);
			methodSequence.add(exceptionMarker + error);
			methodSequences.add(methodSequence);
		}
	}
	
	
	@Override
	 public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInstruction, Instruction insn) {		
		if (!vm.getSystemState().isIgnored()) {			
			SystemState ss = vm.getSystemState();			
			Config conf  = vm.getConfig();
	
			if (insn instanceof JVMInvokeInstruction && insn.isCompleted(ti)) {
				JVMInvokeInstruction md = (JVMInvokeInstruction) insn;
				String methodName = md.getInvokedMethodName();
				int numberOfArgs = md.getArgumentValues(ti).length;
				MethodInfo mi = md.getInvokedMethod();
	
				StackFrame sf = ti.getTopFrame();
				String shortName = methodName;

				if (methodName.contains("("))
					shortName = methodName.substring(0,methodName.indexOf("("));

				if(!mi.equals(sf.getMethodInfo()))
					return;
				
				/* [OE] Validation for bridge modifier should be placed in MethodInfo (core)
				 * also, the validation of synthetic bridge method should be part of isMethodSymbolic
				 * and could be validated with a property (set false by default) if the user wants to
				 * actually analyze such methods
				 */	
				boolean isSyntheticBridgeMethod = mi.isSynthetic() && ((mi.getModifiers() & 0x00000040) != 0);
				if ((BytecodeUtils.isMethodSymbolic(conf, mi.getFullName(), numberOfArgs, null)) && !isSyntheticBridgeMethod) {
					className = mi.getClassName();
					Object [] argValues = md.getArgumentValues(ti);	
					byte[] argTypes = mi.getArgumentTypes();
					Object[] attributes = new Object[numberOfArgs];
	
					int count = 1 ;
					if (md instanceof INVOKESTATIC)
						count = 0;
					for (int i = 0; i < numberOfArgs; i++) {
						attributes[i] = sf.getLocalAttr(count);
						count++;
						if(argTypes[i]== Types.T_LONG || argTypes[i] == Types.T_DOUBLE)
							count++;
					}
	
					// Create a new SequenceChoiceGenerator.
					// this is just to store the information
					// regarding the executed method.
					SequenceChoiceGenerator cg = new SequenceChoiceGenerator(shortName);
					cg.setArgValues(argValues);
					cg.setArgAttributes(attributes);
					// Does not actually make any choice
					ss.setNextChoiceGenerator(cg);
					// nothing to do as there are no choices.
				}
			}
			//else if (insn instanceof JVMReturnInstruction){
			// I don't think we need to do anything  here for printing method sequences...
			//}
		}
	
	}
	
	public void stateBacktracked(Search search) {
		VM vm = search.getVM();
		ChoiceGenerator<?> cg = vm.getChoiceGenerator();		
		SystemState ss = vm.getSystemState();			
	
		if (!(cg instanceof PCChoiceGenerator)){
			ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
			while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
					prev_cg = prev_cg.getPreviousChoiceGenerator();
			}
			cg = prev_cg;
		}

		if ((cg instanceof PCChoiceGenerator) &&
			      ((PCChoiceGenerator) cg).getCurrentPC() != null){

			PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();
			//solve the path condition
			if (SymbolicInstructionFactory.concolicMode) { //TODO: cleaner
				SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
				PCAnalyzer pa = new PCAnalyzer();
				pa.solve(pc,solver);
			}
			else
				pc.solve();
			// get the chain of choice generators.
			ChoiceGenerator<?> [] cgs = ss.getChoiceGenerators();
			methodSequences.add(getMethodSequence(cgs));
			values.add(getMethodValues(cgs));
		}
	}
	
	
	private Vector<String> getMethodValues(ChoiceGenerator [] cgs) {
		Vector<String> methodValues = new Vector<String>();
		ChoiceGenerator cg = null;
		for(int i = 0; i < cgs.length; i++) {
			cg = cgs[i];
			if ((cg instanceof SequenceChoiceGenerator)){
				methodValues.add(getInvokedMethodValues((SequenceChoiceGenerator)cg));
			}
		}
		return methodValues;
	}
	
	/**
	 * A single invoked 'method' is represented as a String.
	 * information about the invoked method is got from the SequenceChoiceGenerator
	 */
	private String getInvokedMethodValues(SequenceChoiceGenerator cg){
		String invokedMethodValues = "";
	
		// get method name
//		String shortName = cg.getMethodShortName();
//		invokedMethod +=  shortName + "(";
	
		// get argument values
		Object[] argValues = cg.getArgValues();
	
		// get number of arguments
		int numberOfArgs = argValues.length;
	
		// get symbolic attributes
		Object[] attributes = cg.getArgAttributes();
	
		// get the solution
		for(int i=0; i<numberOfArgs; i++){
			Object attribute = attributes[i];
			if (attribute != null){ // parameter symbolic
				// here we should consider different types of symbolic arguments
				//IntegerExpression e = (IntegerExpression)attributes[i];
				Object e = attributes[i];
				String solution = "(";
	
	
				if(e instanceof IntegerExpression) {
					// trick to print bools correctly						
					if(argValues[i].toString()=="true" || argValues[i].toString()=="false") {
						if(((IntegerExpression) e).solution() == 0)
							solution = solution+ "false";
						else
							solution = solution+ "true";
					}
					else
						solution = solution+ ((IntegerExpression) e).solution();
				}
				else if (e instanceof RealExpression)
					solution = solution+ ((RealExpression) e).solution();
				else
					solution = solution+ ((StringSymbolic) e).solution();
				invokedMethodValues += solution + ",";
			}
			else { // parameter concrete - for a concrete parameter, the symbolic attribute is null
				invokedMethodValues += argValues[i] + ",";
			}
		}
	
		// remove the extra comma
		if (invokedMethodValues.endsWith(","))
			invokedMethodValues = invokedMethodValues.substring(0,invokedMethodValues.length()-1);
		invokedMethodValues += ")";
	
		return invokedMethodValues;
	}
	
	
	/**
	 *
	 * traverses the ChoiceGenerator chain to get the method sequence
	 * looks for SequenceChoiceGenerator in the chain
	 * SequenceChoiceGenerators have information about the methods
	 * executed and hence the method sequence can be obtained.
	 * A single 'methodSequence' is a vector of invoked 'method's along a path
	 * A single invoked 'method' is represented as a String.
	 *
	 */
	private Vector<String> getMethodSequence(ChoiceGenerator [] cgs){
		// A method sequence is a vector of strings
		Vector<String> methodSequence = new Vector<String>();
		ChoiceGenerator cg = null;
		// explore the choice generator chain - unique for a given path.
		for (int i=0; i<cgs.length; i++){
			cg = cgs[i];
			if ((cg instanceof SequenceChoiceGenerator)){
				methodSequence.add(getInvokedMethod((SequenceChoiceGenerator)cg));
			}
		}
		return methodSequence;
	}
	
	
	
	/**
	 * A single invoked 'method' is represented as a String.
	 * information about the invoked method is got from the SequenceChoiceGenerator
	 */
	private String getInvokedMethod(SequenceChoiceGenerator cg){
		String invokedMethod = "";
	
		// get method name
		String shortName = cg.getMethodShortName();
		invokedMethod +=  shortName + "(";
	
		// get argument values
		Object[] argValues = cg.getArgValues();
	
		// get number of arguments
		int numberOfArgs = argValues.length;
	
		// get symbolic attributes
		Object[] attributes = cg.getArgAttributes();
	
		// get the solution
		for(int i=0; i<numberOfArgs; i++){
			Object attribute = attributes[i];
			if (attribute != null){ // parameter symbolic
				// here we should consider different types of symbolic arguments
				//IntegerExpression e = (IntegerExpression)attributes[i];
				Object e = attributes[i];
				String solution = "";
	
	
				if(e instanceof IntegerExpression) {
					// trick to print bools correctly						
					if(argValues[i].toString()=="true" || argValues[i].toString()=="false") {
						if(((IntegerExpression) e).solution() == 0)
							solution = solution+ "false";
						else
							solution = solution+ "true";
					}
					else
						solution = solution+ ((IntegerExpression) e).solution();
				}
				else if (e instanceof RealExpression)
					solution = solution+ ((RealExpression) e).solution();
				else
					solution = solution+ ((StringSymbolic) e).solution();
				invokedMethod += solution + ",";
			}
			else { // parameter concrete - for a concrete parameter, the symbolic attribute is null
				invokedMethod += argValues[i] + ",";
			}
		}
	
		// remove the extra comma
		if (invokedMethod.endsWith(","))
			invokedMethod = invokedMethod.substring(0,invokedMethod.length()-1);
		invokedMethod += ")";
	
		return invokedMethod;
	}
	
	
	
	  //	-------- the publisher interface
	public void publishFinished (Publisher publisher) {	
	
		PrintWriter pw = publisher.getOut();
		// here just print the method sequences
		publisher.publishTopicStart("Method Sequences");
		printMethodSequences(pw);
	
		// print JUnit4.0 test class
		publisher.publishTopicStart("JUnit 4.0 test class");
		printJUnitTestClass(pw);
		
		publisher.publishTopicStart("Method values");
		printMethodValues(pw);
	
	}
	
	
	/**
	 * @author Mithun Acharya
	 *
	 * prints the method sequences
	 */
	private void printMethodSequences(PrintWriter pw){
		Iterator<Vector> it = methodSequences.iterator();
		while (it.hasNext()){
			pw.println(it.next());
		}
	}
	
	private void printMethodValues(PrintWriter pw) {
//		for(List<String> value : values) {
			pw.println(values);
//		}		
	}
	
	
	  /**
	   * @author Mithun Acharya
	   * Dumb printing of JUnit 4.0 test class
	   * FIXME: getting class name and object name is not smart.
	   */
	  private void printJUnitTestClass(PrintWriter pw){
		  // imports
		  pw.println("import static org.junit.Assert.*;");
		  pw.println("import org.junit.Before;");
		  pw.println("import org.junit.Test;");
	
		  String objectName = (className.toLowerCase()).replace(".", "_");
	
		  pw.println();
		  pw.println("public class " + className.replace(".", "_") + "Test {"); // test class
		  pw.println();
		  pw.println("	private " + className + " " + objectName + ";"); // CUT object to be tested
		  pw.println();
		  pw.println("	@Before"); // setUp method annotation
		  pw.println("	public void setUp() throws Exception {"); // setUp method
		  pw.println("		" + objectName + " = new " + className + "();"); // create object for CUT
		  pw.println("	}"); // setUp method end
		  // Create a test method for each sequence
		  int testIndex = 0;
		  Iterator<Vector> it = methodSequences.iterator();
		  while (it.hasNext()){
			  Vector<String> methodSequence = it.next();
			  pw.println();
			  Iterator<String> it1 = methodSequence.iterator();
			  if (it1.hasNext()) {
				  String errAnn = (String)(it1.next());
	
				  if (errAnn.contains("expected")) {
					  pw.println("	@Test"+errAnn); // Corina: added @Test annotation with exception expected
				  }
				  else {
					  pw.println("	@Test"); // @Test annotation
					  it1 = methodSequence.iterator();
				  }
			  }
			  else
				  it1 = methodSequence.iterator();
	
			  //pw.println("	@Test"); // @Test annotation
			  pw.println("	public void test" + testIndex + "() {"); // begin test method
			  //Iterator<String> it1 = methodSequence.iterator();
			  while(it1.hasNext()){
				  String invokedMethod = it1.next();
				  if (invokedMethod.contains(exceptionMarker)) { // error-string. not a method
					  // add a comment about the exception
					  pw.println("		" + "//should lead to " + invokedMethod);
				  }
				  else{ // normal method
					  pw.println("		" + objectName + "." + invokedMethod + ";"); // invoke a method in the sequence
				  }
			  }
			  pw.println("	}"); // end test method
			  testIndex++;
		  }
		  pw.println("}"); // test class end
	  }	
}