package de.tudarmstadt.thesis.symspark.jvm;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.tudarmstadt.thesis.symspark.jvm.bytecode.INVOKEVIRTUAL;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidatorFactory;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.vm.Instruction;

/**
 * Instruction Factory in charge of recognizing instructions relevant
 * to the execution of the Spark framework.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SparkSymbolicInstructionFactory extends SymbolicInstructionFactory {
	
	private static final Logger LOGGER = JPF.getLogger(SparkSymbolicInstructionFactory.class.getName());
	private static final String CLASS = SparkSymbolicInstructionFactory.class.getSimpleName()+": ";	

	private SparkValidator validator;

	public SparkSymbolicInstructionFactory(Config conf) {
		super(conf);
		this.validator = SparkValidatorFactory.getValidator(conf);				
		LOGGER.log(Level.INFO, CLASS + "Using SparkSymbolicInstructionFactory");		
	}

	@Override
	public Instruction invokevirtual(String clsName, String methodName, String methodSignature) {
		if(validator.isSparkMethod(clsName, methodName)) {
			return new INVOKEVIRTUAL(clsName, methodName, methodSignature);
		}		
		return super.invokevirtual(clsName, methodName, methodSignature);
	}	
}
