package de.tudarmstadt.thesis.symspark.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.tudarmstadt.thesis.symspark.choice.SparkMultipleOutputChoiceGenerator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkMethod;
import de.tudarmstadt.thesis.symspark.util.PCChoiceGeneratorUtils;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.choice.IntIntervalGenerator;

public class FlatMapStrategy extends AbstractMethodStrategy {
	
//	private List<Expression> outputExpressions;
	private boolean exitedCall;

	public FlatMapStrategy(Optional<MethodStrategy> optional) {
//		this.outputExpressions = new ArrayList<Expression>();
		this.exitedCall = false;
		optional.ifPresent(methodStrategy -> {
			this.inputExpression = methodStrategy.getSingleOutputExpression();
//			this.inputExpression = methodStrategy.getInputExpression();		
		});
	}	

	@Override
	public void postProcessing(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		if(exitedMethod.getName().contains("call")) {
			exitedCall = true;
		} else if(exitedCall && exitedMethod.getName().contains("next")) {
			outputExpressions.add((Expression)currentThread.getModifiableTopFrame().getSlotAttr(2));			
		} else if(exitedMethod.getName().contains("flatMap")) {
			if(hasMultipleOutputExpressions()) {
				List<Expression> swapList = new ArrayList<Expression>(outputExpressions);				
				// Removes the first option because it will be already explored in the subsequent execution
				swapList.remove(0);				
				// Adds a null value in the end because for some reason the last option was not explored correctly. This creates an extra dummy option that enables the correct execution with the last expression				
				swapList.add(swapList.get(swapList.size()-1));
				// Register CG to explore all the options
				SparkMultipleOutputChoiceGenerator cg = new SparkMultipleOutputChoiceGenerator("TestFlatMap", swapList);				
				vm.getSystemState().setNextChoiceGenerator(cg);
			}
		}
	}
}
