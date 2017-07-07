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
		if(ins instanceof INVOKEVIRTUAL && ((INVOKEVIRTUAL)ins).getInvokedMethodName().contains("call")) {
			prepare(currentThread, 2);
		} else if(ins instanceof INVOKESTATIC && ((INVOKESTATIC)ins).getInvokedMethodName().contains("lambda")) {
			prepare(currentThread, 1);
		}
	}

	private void prepare(ThreadInfo currentThread, int index) {
		if(inputExpression == null) {
			inputExpression = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(index);
		}
		currentThread.getModifiableTopFrame().setLocalAttr(index, inputExpression);
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
