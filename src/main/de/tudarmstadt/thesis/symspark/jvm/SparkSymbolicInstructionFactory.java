package de.tudarmstadt.thesis.symspark.jvm;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.tudarmstadt.thesis.symspark.jvm.bytecode.INVOKEVIRTUAL;
import de.tudarmstadt.thesis.symspark.jvm.validators.JavaSparkValidator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidator;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.vm.Instruction;

/**
 * Instruction Factory in charge of recognizing instructions relevant
 * to the execution of the Spark framework
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SparkSymbolicInstructionFactory extends SymbolicInstructionFactory {
	
	private static final Logger LOGGER = JPF.getLogger(SparkSymbolicInstructionFactory.class.getName());
	private static final String CLASS = SparkSymbolicInstructionFactory.class.getSimpleName()+": ";
	
	private SparkValidator validator;

	public SparkSymbolicInstructionFactory(Config conf) {
		super(conf);
		//TODO: Decide which validator to use based on a property
		this.validator = new JavaSparkValidator(conf);
		LOGGER.log(Level.INFO, CLASS + "Using SparkSymbolicInstructionFactory");		
	}

	@Override
	public Instruction invokevirtual(String clsName, String methodName, String methodSignature) {
		if(validator.isValid(clsName, methodName)) {
			return new INVOKEVIRTUAL(clsName, methodName, methodSignature);
		}		
		return super.invokevirtual(clsName, methodName, methodSignature);
	}
}
