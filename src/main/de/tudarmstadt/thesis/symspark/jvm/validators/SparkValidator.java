package de.tudarmstadt.thesis.symspark.jvm.validators;

import java.util.Optional;

import gov.nasa.jpf.vm.Instruction;

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
	public boolean isSparkMethod(Instruction instruction);
	public boolean isSparkMethod(String clsName, String methodName);
	public boolean isInternalMethod(Instruction instruction);
	public boolean isInternalMethod(String clsName, String methodName);
	public Optional<String> getSparkMethod(Instruction instruction);
	public Optional<String> getSparkMethod(String clsName, String methodName);
}
