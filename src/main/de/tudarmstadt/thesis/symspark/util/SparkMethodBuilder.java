package de.tudarmstadt.thesis.symspark.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

public class SparkMethodBuilder {
	
	private static final Logger LOGGER = JPF.getLogger(SparkMethodBuilder.class.getName());
	private static final String CLASS = SparkMethodBuilder.class.getSimpleName()+": ";

	private static final String ARGUMENT_SYM_NAME = "sym"; 
	private static final String ARGUMENT_CON_NAME = "con";
	private static final String LAMBDA_CLASS = "$$Lambda";
	
	private String className;
	private String methodName;
	private SparkMethod sparkMethod;
	private boolean hasIterativeAction = false;
	
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
	
	public SparkMethodBuilder setSparkMethod(MethodInfo mi) {
		sparkMethod = SparkMethod.getSparkMethod(mi.getName());
		return this;
	}
	
	public SparkMethodBuilder setHasIterativeAction(ThreadInfo th) {
		Config conf = th.getVM().getConfig();
		if(conf.getInt("spark.reduce.iterations") > 0) {
			hasIterativeAction = true;
		}
		return this;
	}
	
	public String build() {			
		return className+"."+methodName+"("+produceSymbolicArguments()+")";
	}
	
	public String build(ThreadInfo th, ElementInfo ei, MethodInfo mi) {		
		String className = ei.getClassInfo().getName();		
		
		return this.setClassName(className)
				   .setSparkMethod(mi)
				   .setHasIterativeAction(th)
				   .build();
	}
	
	private String produceSymbolicArguments() {
		String arguments = "";
		String delimiter = "#";
		if(sparkMethod.equals(SparkMethod.REDUCE)) {
			if(hasIterativeAction) {
				arguments = ARGUMENT_SYM_NAME + delimiter + ARGUMENT_SYM_NAME;
			} else {
				arguments = ARGUMENT_CON_NAME + delimiter + ARGUMENT_SYM_NAME;
			}			
		} else if (sparkMethod != null){
			arguments = ARGUMENT_SYM_NAME;
		}
		return arguments;
	}
}
