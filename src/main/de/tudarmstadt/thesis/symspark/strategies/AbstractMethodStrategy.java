package de.tudarmstadt.thesis.symspark.strategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tudarmstadt.thesis.symspark.choice.SparkMultipleOutputChoiceGenerator;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;

public abstract class AbstractMethodStrategy implements MethodStrategy {
	protected List<Expression> outputExpressions = new ArrayList<Expression>();	
	protected Expression inputExpression = null;
	protected boolean endStateForced = false;
	
	@Override
	public void preProcessing(ThreadInfo currentThread, Instruction ins) {
		if(ins instanceof INVOKEVIRTUAL && ((INVOKEVIRTUAL)ins).getInvokedMethodName().contains("call")) {
			prepare(currentThread, 1);
		} else if(ins instanceof INVOKESTATIC && ((INVOKESTATIC)ins).getInvokedMethodName().contains("lambda")) {
			prepare(currentThread, 0);
		}
	}
	
	@Override
	public boolean isIterative() {
		return false;
	}

	public Expression getInputExpression() {
		return inputExpression;
	}

	public void setInputExpression(Expression expression) {
		this.inputExpression = expression;
	}
	
	public List<Expression> getOutputExpressions() {
		return this.outputExpressions;
	}

	public boolean isEndStateForced() {
		return endStateForced;
	}	
	
	public boolean hasMultipleOutputExpressions() {
		return outputExpressions.size() > 1;
	}
	
	public Expression getSingleOutputExpression() {
		if(outputExpressions.size() == 0) return null;
		return outputExpressions.get(0);
	}
	
	/**
	 * This method overrides all the existing output expressions.
	 * The idea of this method is to use it when a backtracking event
	 * of the {@link SparkMultipleOutputChoiceGenerator} occurred, so
	 * the output value of the current MethodStrategy is set correctly
	 */
	public void setSingleOutputExpression(Expression expression) {
		outputExpressions = Arrays.asList(expression); 	
	}
	
	private void prepare(ThreadInfo currentThread, int index) {
		if(inputExpression == null) {
			inputExpression = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(index);
		}
		currentThread.getModifiableTopFrame().setLocalAttr(index, inputExpression);
	}
}
