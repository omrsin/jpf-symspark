package de.tudarmstadt.thesis.symspark.strategies;

import java.util.Optional;

import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class MapStrategy extends AbstractMethodStrategy implements MethodStrategy {
	
	public MapStrategy(Optional<MethodStrategy> optional) {
		optional.ifPresent(methodStrategy -> {
			this.inputExpression = methodStrategy.getSingleOutputExpression();			
		});
	}	

	@Override
	public void postProcessing(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		if(exitedMethod.getName().contains("call")) {
			if(currentThread.getCallerStackFrame().getPC() instanceof INVOKEVIRTUAL || 
			   exitedMethod.getClassName().contains("$$Lambda")) {				
				outputExpressions.add((Expression) currentThread.getModifiableTopFrame().getSlotAttr(2));
			}
		}		
	}
}
