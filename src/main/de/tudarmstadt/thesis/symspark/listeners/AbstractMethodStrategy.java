package de.tudarmstadt.thesis.symspark.listeners;

import gov.nasa.jpf.symbc.numeric.Expression;

public abstract class AbstractMethodStrategy implements MethodStrategy {	
	protected Expression expression = null;		

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}		
}
