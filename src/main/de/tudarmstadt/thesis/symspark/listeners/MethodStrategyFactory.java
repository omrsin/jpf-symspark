package de.tudarmstadt.thesis.symspark.listeners;

import java.util.Optional;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import de.tudarmstadt.thesis.symspark.strategies.FilterStrategy;
import de.tudarmstadt.thesis.symspark.strategies.FlatMapStrategy;
import de.tudarmstadt.thesis.symspark.strategies.MapStrategy;
import de.tudarmstadt.thesis.symspark.strategies.MethodStrategy;
import de.tudarmstadt.thesis.symspark.strategies.ReduceStrategy;
import gov.nasa.jpf.vm.ThreadInfo;

/**
  * This class aims to switch among already instantiated strategies
  * with their expression updated to the correct value accordingly
  * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
  *
  */
public class MethodStrategyFactory {
	
	public static MethodStrategy switchMethodStrategy(SparkMethod sparkMethod, MethodStrategy methodStrategy, ThreadInfo currentThread) {			
		switch (sparkMethod) {
		case FILTER:
			return new FilterStrategy(Optional.ofNullable(methodStrategy));
		case MAP:
			return new MapStrategy(Optional.ofNullable(methodStrategy));
		case FLATMAP:
			return new FlatMapStrategy(Optional.ofNullable(methodStrategy));
		case REDUCE:
			return new ReduceStrategy(Optional.ofNullable(methodStrategy), currentThread);
		default:
			throw new IllegalArgumentException("Invalid SparkMethod. No suitable strategy found");				
		}
	}		
}