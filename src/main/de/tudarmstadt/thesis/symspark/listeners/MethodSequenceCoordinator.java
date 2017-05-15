package de.tudarmstadt.thesis.symspark.listeners;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidatorFactory;
import de.tudarmstadt.thesis.symspark.util.PCChoiceGeneratorUtils;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class MethodSequenceCoordinator {
	
	private final SparkValidator validator;
	private MethodStrategy methodStrategy;	
	private Set<Integer> values;
	private Expression inputExpression;
	
	private boolean endStateReached = false;
	
	public MethodSequenceCoordinator(Config conf) {
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
			setMethodStrategy(instruction);
		} else if(validator.isInternalMethod(instruction)){
			methodStrategy.preProcessing(currentThread, instruction);
			if(inputExpression == null) {
				inputExpression = methodStrategy.getExpression();
			}
		}	
	}	
	
	public void processSolution(VM vm) {		
		Optional<PCChoiceGenerator> option = PCChoiceGeneratorUtils.getPCChoiceGenerator(vm.getChoiceGenerator());
		option.ifPresent(pccg -> {
			// This is done to restore the right method strategy.
			setMethodStrategy(pccg.getThreadInfo().getCallerStackFrame().getPrevious().getPrevious().getPC());
			if(endStateReached) {
				if(pccg.getCurrentPC().solve()) {
					values.add(((SymbolicInteger) inputExpression).solution);
				} else {
					//TODO: Do something if a PathCondition is unsatisfiable
					System.out.println("No solution: trigger broken property");
				}
				endStateReached = false;				
			}						
		});
	}
	
	public void percolateToNextMethod(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		if(methodStrategy != null) {
			methodStrategy.postProcessing(vm, currentThread, exitedMethod);
			endStateReached = methodStrategy.isEndStateForced();
		}
	}
			
	public Set<Integer> getValues() {
		return values;
	}
	
	public void setEndStateReached(boolean endStateReached){
		this.endStateReached = endStateReached;
	}
	
	// Private methods
	
	/**
	 * Invoked when a Spark method is detected (Those that are part of the RDD interface, like
	 * filter, map, etc). The coordinator prepares for the symbolic execution of the respective
	 * higher order function passed to the detected method.
	 * 
	 * @param instruction The JVM instruction that invokes the Spark method.
	 */
	private void setMethodStrategy(Instruction instruction) {
		Optional<String> option = validator.getSparkMethod(instruction); 
		option.map(SparkMethod::getSparkMethod)
			.ifPresent(sparkMethod -> {
				methodStrategy = MethodStrategyFactory.switchMethodStrategy(sparkMethod, methodStrategy);
			});		
	}	
	
	 /**
	  * This internal static class aims to switch among already instantiated strategies
	  * with their expression updated to the correct value accordingly
	  * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
	  *
	  */
	private static class MethodStrategyFactory {
		static FilterStrategy filterStrategy =  new FilterStrategy(Optional.empty());
		static MapStrategy mapStrategy = new MapStrategy(Optional.empty());

		/**
		 * Switches the method strategy to one that is able to deal with the upcoming 
		 * spark method.
		 * @param sparkMethod Upcoming supported Spark method.
		 * @param methodStrategy Current active method strategy.
		 * @return Matching method strategy for the upcoming Spark method. 
		 */
		public static MethodStrategy switchMethodStrategy(SparkMethod sparkMethod, MethodStrategy methodStrategy) {			
			switch (sparkMethod) {
			case FILTER:				
				return updateMethodStrategyExpression(filterStrategy, Optional.ofNullable(methodStrategy)) ;
			case MAP:				
				return updateMethodStrategyExpression(mapStrategy, Optional.ofNullable(methodStrategy));
			default:
				throw new IllegalArgumentException("Invalid SparkMethod. No suitable strategy found");				
			}
		}
		
		/**
		 * Updates the expression of the selected method strategy.
		 * @param methodStrategy Selected method strategy.
		 * @param option Optional value with the current method strategy. Empty if no method 
		 * strategy has been set up yet.
		 * @return
		 */
		private static MethodStrategy updateMethodStrategyExpression(MethodStrategy methodStrategy, Optional<MethodStrategy> option) {
			option.ifPresent(ms -> methodStrategy.setExpression(ms.getExpression()));
			return methodStrategy;
		}
	}
}
