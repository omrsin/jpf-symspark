package de.tudarmstadt.thesis.symspark.jvm.validators;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.tudarmstadt.thesis.symspark.jvm.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.Instruction;

/**
 * Validator for the Java implementation of the Spark framework.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class JavaSparkValidator implements SparkValidator {

	private static final String CLASS_NAME = "JavaRDD";
	private static final String FULL_CLASS_NAME = "org.apache.spark.api.java.JavaRDD";
	private static final String INTERNAL_METHOD = "call";
	
	private String[] sparkMethods;
	
	public JavaSparkValidator(Config conf) {
		List<String> allSparkMethods = Arrays.stream(SparkMethods.values()).parallel().map(e -> e.name().toLowerCase()).collect(Collectors.toList());
		String[] validSelectedMethods = Arrays.asList(conf.getStringArray("spark.methods")).stream()
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
	public boolean isInternalMethod(Instruction instruction) {
		if(instruction instanceof gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL) {
			String clsName = ((gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL)instruction).getInvokedMethodClassName();
			String methodName = ((gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL)instruction).getInvokedMethodName();
			return isInternalMethod(clsName, methodName);			
		}
		return false;
	}

	@Override
	public boolean isInternalMethod(String clsName, String methodName) {		
		if(methodName != null) {
			return methodName.contains(INTERNAL_METHOD);
		} else {
			return false;
		}
	}

	@Override
	public Optional<String> getSparkMethod(Instruction instruction) {		
		if(instruction instanceof INVOKEVIRTUAL) {
			return getSparkMethod((INVOKEVIRTUAL)instruction);
		}		
		return Optional.empty();
	}

	@Override
	public Optional<String> getSparkMethod(String clsName, String methodName) {
		if(isSparkMethod(clsName, methodName)) {
			return Arrays.stream(sparkMethods).parallel().filter(methodName::contains).findFirst();
		}
		return Optional.empty();
	}
	
	// Private methods

	private boolean validateSparkClass(String clsName) {		
		return clsName.contains(CLASS_NAME);
	}
	
	private boolean validateSparkMethod(String methodName) {
		if(sparkMethods != null) {
			return Arrays.stream(sparkMethods).parallel().anyMatch(methodName::contains);
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
}
