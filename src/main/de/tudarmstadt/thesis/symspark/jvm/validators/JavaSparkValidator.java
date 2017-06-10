package de.tudarmstadt.thesis.symspark.jvm.validators;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.tudarmstadt.thesis.symspark.jvm.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.bytecode.INVOKEDYNAMIC;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Validator for the Java implementation of the Spark framework.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class JavaSparkValidator implements SparkValidator {

	private static final String CLASS_NAME = "JavaRDD";
	private static final String FULL_CLASS_NAME = "org.apache.spark.api.java.JavaRDD";
	private static final String INTERNAL_METHOD = "call";
	private static final String INTERNAL_LAMBDA_METHOD = "lambda$";
	
	private String[] sparkMethods;
	
	public JavaSparkValidator(Config conf) {
		List<String> allSparkMethods = Arrays.stream(SparkMethod.values()).parallel()
				.map(e -> e.name().toLowerCase())
				.collect(Collectors.toList());
		String[] validSelectedMethods = Arrays.asList(conf.getStringArray(SPARK_METHODS)).stream().parallel()
				.map(String::toLowerCase)
				.filter(allSparkMethods::contains)
				.toArray(String[]::new);				
		
		this.sparkMethods = validSelectedMethods;
	}	
	
	@Override	
	public boolean isSparkMethod(Instruction instruction) {
		if(instruction instanceof INVOKEVIRTUAL) {
			return validateInstruction((INVOKEVIRTUAL)instruction);			
		}
		return false;
	}

	@Override
	public boolean isSparkMethod(String clsName, String methodName) {		
		return validateSparkClass(clsName) && validateSparkMethod(methodName);
	}	
		
	@Override
	public boolean isInternalMethod(Instruction instruction, ThreadInfo currentThread) {		
		if((instruction instanceof gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL || instruction instanceof INVOKESTATIC) &&
			isInvokedBySpark(currentThread)) {			
			String clsName = ((JVMInvokeInstruction)instruction).getInvokedMethodClassName();
			String methodName = ((JVMInvokeInstruction)instruction).getInvokedMethodName();
			return isInternalMethod(clsName, methodName);			
		}
		return false;
	}	

	@Override
	public boolean isInternalMethod(String clsName, String methodName) {		
		if(methodName != null) {
			String method = methodName.toLowerCase(); 
			return method.contains(INTERNAL_METHOD) || method.contains(INTERNAL_LAMBDA_METHOD);			
		} else {
			return false;
		}
	}

	@Override
	public Optional<String> getSparkMethod(Instruction instruction) {		
		if(isValidInstruction(instruction)) {
			return getSparkMethod((INVOKEVIRTUAL)instruction);
		}		
		return Optional.empty();
	}	

	@Override
	public Optional<String> getSparkMethod(String clsName, String methodName) {
		if(isSparkMethod(clsName, methodName)) {
			return Arrays.stream(sparkMethods).parallel().filter(methodName.toLowerCase()::contains).findFirst();
		}
		return Optional.empty();
	}
	
	// Private methods

	private boolean validateSparkClass(String clsName) {		
		return clsName.contains(CLASS_NAME);
	}
	
	private boolean validateSparkMethod(String methodName) {
		if(sparkMethods != null) {
			return Arrays.stream(sparkMethods).parallel().anyMatch(methodName.toLowerCase()::contains);
		} else {
			return false;
		}
	}
	
	private boolean validateInstruction(INVOKEVIRTUAL instruction) {
		String clsName = instruction.getInvokedMethodClassName();
		String methodName = instruction.getInvokedMethodName();
		return isSparkMethod(clsName, methodName);
	}
	
	private Optional<String> getSparkMethod(INVOKEVIRTUAL instruction) {
		String clsName = instruction.getInvokedMethodClassName();
		String methodName = instruction.getInvokedMethodName();
		return getSparkMethod(clsName, methodName);
	}
	
	private boolean isValidInstruction(Instruction instruction) {
		if(instruction instanceof INVOKEVIRTUAL) {
			return true;
		} else if (instruction instanceof INVOKEDYNAMIC) {
			return true;
		}
		return false;
	}
	
	private boolean isInvokedBySpark(ThreadInfo currentThread) {
		StackFrame sf = currentThread.getCallerStackFrame();
		if(sf != null && sf.getPrevious() != null && sf.getPrevious().getPrevious() != null) {
			return isSparkMethod(sf.getPrevious().getPrevious().getPC());
		}
		return false;		 
	}
}
