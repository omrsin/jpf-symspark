package de.tudarmstadt.thesis.symspark.listeners;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.tudarmstadt.thesis.symspark.choice.SparkIterativeChoiceGenerator;
import de.tudarmstadt.thesis.symspark.choice.SparkMultipleOutputChoiceGenerator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidatorFactory;
import de.tudarmstadt.thesis.symspark.strategies.MethodStrategy;
import de.tudarmstadt.thesis.symspark.util.PCChoiceGeneratorUtils;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.symbc.string.StringSymbolic;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class MethodSequenceCoordinator {
	
	private final SparkValidator validator;
	private MethodStrategy methodStrategy;	
	private Set<String> values;
	private Expression inputExpression;
	
	private boolean endStateReached = false;
	
	public MethodSequenceCoordinator(Config conf) {
		values = new HashSet<String>();
		validator = SparkValidatorFactory.getValidator(conf);
		inputExpression = null;
	}
	
	/**
	 * Process the execution of a Spark method. In the first case, whenever one of the Spark
	 * methods is called, it prepares accordingly, setting up the right methodStrategy. Next,
	 * when the higher order function passed to the Spark method is executed, the pre-processing
	 * of the symbolic execution is taken care of.
	 * @param currentThread
	 * @param instruction Executed instruction detected in the listener. Should be INVOKEVIRTUAL
	 */
	public void detectSparkInstruction(ThreadInfo currentThread, Instruction instruction) {
		if(validator.isSparkMethod(instruction)) {			
			setMethodStrategy(instruction, currentThread);
		} else if(validator.isInternalMethod(instruction, currentThread)){
			methodStrategy.preProcessing(currentThread, instruction);
			if(inputExpression == null) {
				inputExpression = methodStrategy.getInputExpression();
			}
		}	
	}	
	
	public void processSolution(VM vm) {
		if(vm.getChoiceGenerator() instanceof PCChoiceGenerator) {
			//TODO: Now that I had to check if the backtracked CG was a PCCG then this optional might be unnecessary
			Optional<PCChoiceGenerator> option = PCChoiceGeneratorUtils.getPCChoiceGenerator(vm.getChoiceGenerator());
			option.ifPresent(pccg -> {
				// This is done to restore the right method strategy.
				setMethodStrategy(pccg.getThreadInfo().getCallerStackFrame().getPrevious().getPrevious().getPC(), pccg.getThreadInfo());
				if(endStateReached) {
					if(pccg.getCurrentPC().solve()) {
						values.add(getSolution());
					} else {					
						//TODO: Do something if a PathCondition is unsatisfiable
						System.out.println("Current path condition not satisfiable: "+pccg.getCurrentPC());
					}
					endStateReached = false;				
				}						
			});
		} else if(vm.getChoiceGenerator() instanceof SparkMultipleOutputChoiceGenerator) {
			SparkMultipleOutputChoiceGenerator cg = (SparkMultipleOutputChoiceGenerator) vm.getChoiceGenerator();
			methodStrategy.setSingleOutputExpression(cg.getNextExpression());			
		} else if(vm.getChoiceGenerator() instanceof SparkIterativeChoiceGenerator) {
			// Here we should check if it is done and add all the values to the result;
		}
	}	

	public void percolateToNextMethod(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		if(methodStrategy != null) {
			methodStrategy.postProcessing(vm, currentThread, exitedMethod);
			endStateReached = methodStrategy.isEndStateForced();
		}
	}
			
	public Set<String> getValues() {
		return values;
	}
	
	public void setEndStateReached(boolean endStateReached){
		this.endStateReached = endStateReached;
	}
	
	// Private methods
	
	/**
	 * Invoked when a Spark method is detected (Those that are part of the RDD interface, like
	 * filter, map, etc). The coordinator prepares for the symbolic execution of the respective
	 * higher order function passed to the detected method.
	 * 
	 * @param instruction The JVM instruction that invokes the Spark method.
	 */
	private void setMethodStrategy(Instruction instruction, ThreadInfo currentThread) {		
		Optional<String> option = validator.getSparkMethod(instruction); 
		option.map(SparkMethod::getSparkMethod)
			.ifPresent(sparkMethod -> {
				methodStrategy = MethodStrategyFactory.switchMethodStrategy(sparkMethod, methodStrategy, currentThread);
			});		
	}
	
	private String getSolution() {
		if(inputExpression instanceof SymbolicInteger) {
			return String.valueOf(((SymbolicInteger) inputExpression).solution);
		} else if (inputExpression instanceof StringSymbolic) {
			StringSymbolic symString = ((StringSymbolic) inputExpression);			
			return "\""+symString.solution+"\"";
		}
		return null;
	}
}
