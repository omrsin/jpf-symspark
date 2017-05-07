package de.tudarmstadt.thesis.symspark.listeners;

import java.util.Optional;

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

public class FilterStrategy extends AbstractMethodStrategy {

	public FilterStrategy(Optional<MethodStrategy> optional) {		
		optional.ifPresent(methodStrategy -> {
			this.expression = methodStrategy.getExpression();			
		});
	}

	@Override
	public void preProcessing(ThreadInfo currentThread, Instruction ins) {
		//TODO: This validation could and should be done by the validator
		if(ins instanceof INVOKEVIRTUAL && ((INVOKEVIRTUAL)ins).getInvokedMethodName().contains("call")) {
			if(expression == null) {
				expression = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(1);
			}
			currentThread.getModifiableTopFrame().setLocalAttr(1, expression);			
		} else if(ins instanceof INVOKESTATIC && ((INVOKESTATIC)ins).getInvokedMethodName().contains("lambda")) {
			if(expression == null) {
				expression = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(0);
			}
			currentThread.getModifiableTopFrame().setLocalAttr(0, expression);
		}
	}

	@Override
	public void postProcessing(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		SparkMethod sparkMethod = SparkMethod.getSparkMethod(exitedMethod.getName());
		if(sparkMethod == SparkMethod.FILTER) {
			Optional<PCChoiceGenerator> option = PCChoiceGeneratorUtils.getPCChoiceGenerator(vm.getChoiceGenerator()); 
			option.ifPresent(cg -> {
				if(cg.getNextChoice() == 1) currentThread.breakTransition(true);
			});		
		}
	}
}
