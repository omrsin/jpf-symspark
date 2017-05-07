package de.tudarmstadt.thesis.symspark.jvm.bytecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

public class INVOKEVIRTUAL extends gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL {
	
	private static final Logger LOGGER = JPF.getLogger(INVOKEVIRTUAL.class.getName());
	private static final String CLASS = INVOKEVIRTUAL.class.getSimpleName()+": ";

	public INVOKEVIRTUAL(String clsName, String methodName, String methodSignature) {
		super(clsName, methodName, methodSignature);
	}

	@Override
	public Instruction execute(ThreadInfo th) {		
		LOGGER.log(Level.FINER, CLASS + "InvokedMethod: "+getInvokedMethodName());
		logInstructions();	
		
		Config conf = th.getVM().getConfig();
		List<String> methods = new ArrayList<String>();
		methods.addAll(Arrays.asList(Optional.ofNullable(conf.getStringArray("symbolic.method")).orElse(new String[0])));		
		
		String method = buildMethodToAnalyze(th);
		LOGGER.log(Level.INFO, CLASS + "Method to be analyzed: "+method);
		methods.add(method);		
		conf.setProperty("symbolic.method", join(methods, ";"));		

		return super.execute(th);
	}
	
	private void logInstructions() {
		MethodInfo methodInfo = getInvokedMethod();
		for(int i = 0; i < methodInfo.getInstructions().length; i++) {
			LOGGER.log(Level.FINEST, CLASS + "Instruction["+i+"]: "+methodInfo.getInstructions()[i]);			
		}		
	}

	private String buildMethodToAnalyze(ThreadInfo th) {
		return new SparkMethodBuilder().build(th);
	}	

	private String join(List<String> list, String separator) {
		StringBuilder builder = new StringBuilder();
		for(String element : list) {
			builder.append(element+separator);
		}
		return builder.toString();
	}	
	
	//TODO: Move the responsibility to another method. Maybe extract the internal class SparkMethodBuilder to an external class
	private class SparkMethodBuilder {
				
		private final String ARGUMENT_NAME = "sym";
		private final String LAMBDA_CLASS = "$$Lambda";
		
		private String className;
		private String methodName;
		private int numberOfArguments;
		
		public SparkMethodBuilder setClassName(String className) {						
			if(className.contains(LAMBDA_CLASS)) {
				LOGGER.log(Level.FINER, CLASS + "Invoked from a Lambda class: "+className);
				String[] splitClassName = className.split("\\$");
				String classPath = splitClassName[0];
				String methodNumber = splitClassName[splitClassName.length-1];
				this.className = classPath;
				this.methodName = "lambda$"+methodNumber;
			} else {
				LOGGER.log(Level.FINER, CLASS + "Invoked from an anonymous class: "+className);
				this.className = className;
				this.methodName = "call";
			}			
			return this;
		}
		
		public SparkMethodBuilder setNumberOfArguments(int numberOfArguments) {
			this.numberOfArguments = numberOfArguments;
			return this;
		}
		
		public String build() {			
			return className+"."+methodName+"("+produceSymbolicArguments()+")";
		}
		
		public String build(ThreadInfo th) {
			String className = ((ElementInfo)getArgumentValues(th)[0]).getClassInfo().getName();
			int numberOfArguments = getNumberOfArguments();
			
			return this.setClassName(className)
					   .setNumberOfArguments(numberOfArguments)
					   .build();
		}	

		//TODO: In particular, move this method somewhere else
		private int getNumberOfArguments() {
			int numberOfArguments = 0;
			SparkMethod sparkMethod = SparkMethod.getSparkMethod(getInvokedMethod().getName());				
			if(sparkMethod == SparkMethod.FILTER || sparkMethod == SparkMethod.MAP) {
				numberOfArguments = 1;
			} else if(sparkMethod == SparkMethod.REDUCE) {
				numberOfArguments = 2;
			}		
			return numberOfArguments;
		}
		
		private String produceSymbolicArguments() {
			String arguments = "";
			String delimiter = "#";
			for(int i = 0; i < numberOfArguments; i++) {
				if(i == numberOfArguments - 1) {
					delimiter = "";
				}
				arguments += ARGUMENT_NAME+delimiter;								
			}
			return arguments;
		}
	}
}
