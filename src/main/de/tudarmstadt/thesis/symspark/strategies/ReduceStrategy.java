package de.tudarmstadt.thesis.symspark.strategies;

import java.util.Optional;

import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class ReduceStrategy extends AbstractMethodStrategy implements MethodStrategy {
	
	public ReduceStrategy(Optional<MethodStrategy> optional) {
		optional.ifPresent(methodStrategy -> {
			this.inputExpression = methodStrategy.getSingleOutputExpression();			
		});
	}
	
	@Override
	public void preProcessing(ThreadInfo currentThread, Instruction ins) {
		//TODO: This validation could and should be done by the validator
		if(ins instanceof INVOKEVIRTUAL && ((INVOKEVIRTUAL)ins).getInvokedMethodName().contains("call")) {
			if(inputExpression == null) {
				inputExpression = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(2);
			}
			currentThread.getModifiableTopFrame().setLocalAttr(2, inputExpression);
		} else if(ins instanceof INVOKESTATIC && ((INVOKESTATIC)ins).getInvokedMethodName().contains("lambda")) {
			if(inputExpression == null) {
				inputExpression = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(1);
			}
			currentThread.getModifiableTopFrame().setLocalAttr(1, inputExpression);
		}
	}

	@Override
	public void postProcessing(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		if(exitedMethod.getName().contains("call")) {
			if(currentThread.getCallerStackFrame().getPC() instanceof INVOKEVIRTUAL || 
			   exitedMethod.getClassName().contains("$$Lambda")) {				
				currentThread.breakTransition(true);
				endStateForced = true;
			}
		}		
	}
}
