package de.tudarmstadt.thesis.symspark.strategies;

import java.util.Optional;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import de.tudarmstadt.thesis.symspark.util.PCChoiceGeneratorUtils;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class FilterStrategy extends AbstractMethodStrategy {

	public FilterStrategy(Optional<MethodStrategy> optional) {		
		optional.ifPresent(methodStrategy -> {
			this.inputExpression = methodStrategy.getSingleOutputExpression();			
		});
	}

	@Override
	public void postProcessing(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		SparkMethod sparkMethod = SparkMethod.getSparkMethod(exitedMethod.getName());
		if(sparkMethod == SparkMethod.FILTER) {
			outputExpressions.add(inputExpression);
			Optional<PCChoiceGenerator> option = PCChoiceGeneratorUtils.getPCChoiceGenerator(vm.getChoiceGenerator()); 
			option.ifPresent(cg -> {
				if(cg.getNextChoice() == 1) {
					currentThread.breakTransition(true);
					endStateForced = true;
				}				
			});		
		}
	}
}
