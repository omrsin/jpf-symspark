package de.tudarmstadt.thesis.symspark.jvm.validators;

/**
 * This interface represents the contract enforced to identify valid
 * Spark classes and methods to be analyzed. The main idea of such an
 * interface is to allow the possibility of validating different types
 * of implementations of the Spark framework. For example, the Java
 * or the Scala APIs.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public interface SparkValidator {
	String[] SPARK_METHODS = {"filter", "map", "reduce"};
	
	public boolean validateClass(String clsName);
	public boolean validateMethod(String methodName);
	public boolean isValid(String clsName, String methodName);
}
