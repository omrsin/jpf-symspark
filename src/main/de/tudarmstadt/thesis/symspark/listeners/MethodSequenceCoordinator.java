package de.tudarmstadt.thesis.symspark.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidatorFactory;
import de.tudarmstadt.thesis.symspark.util.PCChoiceGeneratorUtils;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class MethodSequenceCoordinator {
	
	private final SparkValidator validator;
	private MethodStrategy methodStrategy;
	private List<String> methods;
	private Set<Integer> values;
	private Expression inputExpression;
	
	public MethodSequenceCoordinator(Config conf) {
		methods = new ArrayList<String>();
		values = new HashSet<Integer>();
		validator = SparkValidatorFactory.getValidator(conf);
		inputExpression = null;
	}
	
	/**
	 * Process the execution of a Spark method. In the first case, whenever one of the Spark
	 * methods is called, it prepares accordingly, setting up the right methodStrategy. Next,
	 * when the higher order function passed to the Spark method is executed, the pre-processing
	 * of the symbolic execution is taken care of.
	 * @param currentThread
	 * @param instruction Executed instruction detected in the listener. Should be INVOKEVIRTUAL
	 */
	public void detectSparkInstruction(ThreadInfo currentThread, Instruction instruction) {
		if(validator.isSparkMethod(instruction)) {
			prepareSparkMethod(instruction);
		} else if(validator.isInternalMethod(instruction)){
			methodStrategy.preProcessing(currentThread, instruction);
			if(inputExpression == null) {
				inputExpression = methodStrategy.getExpression();
			}
		}	
	}	
	
	public void processSolution(VM vm) {
		Optional<PathCondition> option = PCChoiceGeneratorUtils.getPathCondition(vm.getChoiceGenerator()); 
		option.ifPresent(pc -> {
			pc.solve();			
			values.add(((SymbolicInteger) inputExpression).solution);
		});
	}
	
	public void percolateToNextMethod(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		if(methodStrategy != null) {
			methodStrategy.postProcessing(vm, currentThread, exitedMethod);
		}
	}
			
	public List<String> getMethods() {
		return methods;
	}
	
	public Set<Integer> getValues() {
		return values;
	}
	
	// Private methods
	
	/**
	 * Invoked when a Spark method is detected (Those that are part of the RDD interface, like
	 * filter, map, etc). The coordinator prepares for the symbolic execution of the respective
	 * higher order function passed to the detected method.
	 * 
	 * @param instruction The JVM instruction that invokes the Spark method.
	 */
	private void prepareSparkMethod(Instruction instruction) {
		Optional<String> option = validator.getSparkMethod(instruction); 
		option.ifPresent(this::switchMethodStrategy);		
		methods.add(((INVOKEVIRTUAL) instruction).getInvokedMethod().getName());
	}
	
	/**
	 * Switches the methodStrategy to one that is able to deal with the upcoming 
	 * spark method.
	 * 
	 * @param method Name of the spark method that was detected.
	 */
	//TODO: Consider extracting the handling of the methodStrategy to another class something like a Factory maybe but that chooses from a list of already instantiated methodStrategy objects.
	private void switchMethodStrategy(String method) {
		switch (method) {
		case "filter":
			methodStrategy = new FilterStrategy(Optional.ofNullable(methodStrategy));
			break;
		case "map":
			methodStrategy = new MapStrategy(Optional.ofNullable(methodStrategy));
			break;
		default:
			break;
		}		
	}	
}
