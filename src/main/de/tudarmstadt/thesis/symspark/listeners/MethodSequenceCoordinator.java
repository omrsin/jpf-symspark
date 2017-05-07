package de.tudarmstadt.thesis.symspark.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidatorFactory;
import de.tudarmstadt.thesis.symspark.util.PCChoiceGeneratorUtils;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
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
	
	private boolean isFirstBacktrack;
	private String lastMethod;
	
	public MethodSequenceCoordinator(Config conf) {
		methods = new ArrayList<String>();
		values = new HashSet<Integer>();
		validator = SparkValidatorFactory.getValidator(conf);
		inputExpression = null;
		isFirstBacktrack = true;
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
//		Config conf = currentThread.getVM().getConfig();
//		List<String> validMethods =	Arrays.asList(Optional.ofNullable(conf.getStringArray("symbolic.method")).orElse(new String[0]));
		if(validator.isSparkMethod(instruction)) {			
			prepareSparkMethod(instruction);
		} else if(validator.isInternalMethod(instruction)){
			methodStrategy.preProcessing(currentThread, instruction);
			if(inputExpression == null) {
				inputExpression = methodStrategy.getExpression();
			}
		}	
	}	
	
	//TODO: Now this is not only processing the solution, it is backtracking the state of the method strategy also, change the name to explain better what this method does
	public void processSolution(VM vm) {
		Optional<PCChoiceGenerator> option = PCChoiceGeneratorUtils.getPCChoiceGenerator(vm.getChoiceGenerator());
		option.ifPresent(pccg -> {
			if(isFirstBacktrack) {
				lastMethod = pccg.getMethodName();				
				isFirstBacktrack = false;
			}
			//TODO: This is done to restore the right method strategy. Not very clean at least by the looks
			prepareSparkMethod(pccg.getThreadInfo().getCallerStackFrame().getPrevious().getPrevious().getPC());
			if(lastMethod.equals(pccg.getMethodName())) {
				pccg.getCurrentPC().solve();
				values.add(((SymbolicInteger) inputExpression).solution);
			}						
		});
	}
	
	//TODO: This method should validate first if the invoked method is relevant to the strategy and discard those who are not
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
		option.map(SparkMethod::getSparkMethod)
			.ifPresent(sparkMethod -> {
				switchMethodStrategy(sparkMethod);
				methods.add(sparkMethod.name());
			});		
	}
	
	/**
	 * Switches the methodStrategy to one that is able to deal with the upcoming 
	 * spark method.
	 * 
	 * @param method Name of the spark method that was detected.
	 */
	//TODO: Consider extracting the handling of the methodStrategy to another class something like a Factory maybe but that chooses from a list of already instantiated methodStrategy objects.
	private void switchMethodStrategy(SparkMethod sparkMethod) {
		switch (sparkMethod) {
		case FILTER:
			methodStrategy = new FilterStrategy(Optional.ofNullable(methodStrategy));
			break;
		case MAP:
			methodStrategy = new MapStrategy(Optional.ofNullable(methodStrategy));
			break;
		default:
			break;
		}		
	}	
}
