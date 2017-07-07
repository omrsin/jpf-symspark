package de.tudarmstadt.thesis.symspark.listeners;

import java.util.Optional;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import de.tudarmstadt.thesis.symspark.strategies.FilterStrategy;
import de.tudarmstadt.thesis.symspark.strategies.FlatMapStrategy;
import de.tudarmstadt.thesis.symspark.strategies.IterativeReduceStrategy;
import de.tudarmstadt.thesis.symspark.strategies.MapStrategy;
import de.tudarmstadt.thesis.symspark.strategies.MethodStrategy;
import de.tudarmstadt.thesis.symspark.strategies.ReduceStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.vm.ThreadInfo;

/**
  * This class aims to switch among already instantiated strategies
  * with their expression updated to the correct value accordingly
  * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
  *
  */
public class MethodStrategyFactory {
	
	public static MethodStrategy switchMethodStrategy(SparkMethod sparkMethod, MethodStrategy methodStrategy, ThreadInfo currentThread, Expression initialSymbolicVariable) {			
		switch (sparkMethod) {
		case FILTER:
			return new FilterStrategy(Optional.ofNullable(methodStrategy));
		case MAP:
			return new MapStrategy(Optional.ofNullable(methodStrategy));
		case FLATMAP:
			return new FlatMapStrategy(Optional.ofNullable(methodStrategy));
		case REDUCE:
			Config conf = currentThread.getVM().getConfig();
			int iterations = conf.getInt("spark.reduce.iterations");
			if(iterations > 0) {
				return new IterativeReduceStrategy(Optional.ofNullable(methodStrategy), iterations, currentThread, initialSymbolicVariable);
			} else {
				return new ReduceStrategy(Optional.ofNullable(methodStrategy));
			}
			
		default:
			throw new IllegalArgumentException("Invalid SparkMethod. No suitable strategy found");				
		}
	}		
}