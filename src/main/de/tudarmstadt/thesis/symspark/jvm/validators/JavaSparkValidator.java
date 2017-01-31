package de.tudarmstadt.thesis.symspark.jvm.validators;

import java.util.Arrays;

/**
 * Validator for the Java implementation of the Spark framework.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class JavaSparkValidator implements SparkValidator {

	private static final String CLASS_NAME = "JavaRDD";
	@Override
	public boolean validateClass(String clsName) {		
		return clsName.contains(CLASS_NAME);
	}

	@Override
	public boolean validateMethod(String methodName) {		
		return Arrays.stream(SPARK_METHODS).parallel().anyMatch(methodName::contains);
	}

	@Override
	public boolean isValid(String clsName, String methodName) {		
		return validateClass(clsName) && validateMethod(methodName);
	}	
}
