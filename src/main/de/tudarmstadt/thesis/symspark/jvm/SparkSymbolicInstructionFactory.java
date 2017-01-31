package de.tudarmstadt.thesis.symspark.jvm;

import de.tudarmstadt.thesis.symspark.jvm.bytecode.INVOKEVIRTUAL;
import de.tudarmstadt.thesis.symspark.jvm.validators.JavaSparkValidator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidator;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.vm.Instruction;

/**
 * Instruction Factory in charge of recognizing instructions relevant
 * to the execution of the Spark framework
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SparkSymbolicInstructionFactory extends SymbolicInstructionFactory {
	
	private SparkValidator validator;

	public SparkSymbolicInstructionFactory(Config conf) {
		super(conf);
		this.validator = new JavaSparkValidator();
		System.out.println("[OE] Using SparkSymbolicInstructionFactory");
	}

	@Override
	public Instruction invokevirtual(String clsName, String methodName, String methodSignature) {
		if(validator.isValid(clsName, methodName)) {
			return new INVOKEVIRTUAL(clsName, methodName, methodSignature);
		}		
		return super.invokevirtual(clsName, methodName, methodSignature);
	}
}
