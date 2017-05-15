package de.tudarmstadt.thesis.symspark.listeners;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public interface MethodStrategy {	
	public void preProcessing(ThreadInfo currentThread, Instruction ins);	
	public void postProcessing(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod);
		
	public Expression getExpression();
	public void setExpression(Expression expression);
	public boolean isEndStateForced();
	public void setEndStateForced(boolean endStateForced);	
}
