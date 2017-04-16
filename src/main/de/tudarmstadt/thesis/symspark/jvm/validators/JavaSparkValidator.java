package de.tudarmstadt.thesis.symspark.jvm.validators;

import java.util.Arrays;

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
	
	private String[] sparkMethods;
	
	public JavaSparkValidator(Config conf) {
		//TODO consider creating a control group to filter methods written in the property file that don't belong to spark
		this.sparkMethods = conf.getStringArray("spark.methods");
	}	
	
	@Override	
	public boolean isValid(Instruction instruction) {
		if(instruction instanceof INVOKEVIRTUAL) {
			return validateInstruction((INVOKEVIRTUAL) instruction);			
		}
		return false;
	}

	@Override
	public boolean isValid(String clsName, String methodName) {		
		return validateClass(clsName) && validateMethod(methodName);
	}
		
	public boolean validateClass(String clsName) {		
		return clsName.contains(CLASS_NAME);
	}
	
	public boolean validateMethod(String methodName) {
		if(sparkMethods != null) {
			return Arrays.stream(sparkMethods).parallel().anyMatch(methodName::contains);
		} else {
			return false;
		}
	}
	
	private boolean validateInstruction(INVOKEVIRTUAL instruction) {
		String clsName = instruction.getInvokedMethodClassName();
		String methodName = instruction.getInvokedMethodName();
		return isValid(clsName, methodName);
	}
}
