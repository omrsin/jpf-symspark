package de.tudarmstadt.thesis.symspark.jvm.validators;

import java.util.Arrays;

import gov.nasa.jpf.Config;

/**
 * Validator for the Java implementation of the Spark framework.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class JavaSparkValidator implements SparkValidator {

	private static final String CLASS_NAME = "JavaRDD";
	
	private String[] sparkMethods;
	
	public JavaSparkValidator(Config conf) {
		//TODO consider creating a control group to filter methods written in the property file that don't belong to spark
		this.sparkMethods = conf.getStringArray("spark.methods");
	}
	
	@Override
	public boolean validateClass(String clsName) {		
		return clsName.contains(CLASS_NAME);
	}

	@Override
	public boolean validateMethod(String methodName) {
		if(sparkMethods != null) {
			return Arrays.stream(sparkMethods).parallel().anyMatch(methodName::contains);
		} else {
			return false;
		}
	}

	@Override
	public boolean isValid(String clsName, String methodName) {		
		return validateClass(clsName) && validateMethod(methodName);
	}	
}
