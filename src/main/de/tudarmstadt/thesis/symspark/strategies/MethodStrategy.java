package de.tudarmstadt.thesis.symspark.strategies;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public interface MethodStrategy {	
	public void preProcessing(ThreadInfo currentThread, Instruction ins);	
	public void postProcessing(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod);
		
	public Expression getInputExpression();
	public void setInputExpression(Expression expression);
	public boolean isEndStateForced();	
	public boolean hasMultipleOutputExpressions();
	public Expression getSingleOutputExpression();
	public void setSingleOutputExpression(Expression expression);
	public boolean isIterative();
}
