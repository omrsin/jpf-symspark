package de.tudarmstadt.thesis.symspark.listeners;

import java.util.Optional;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;

public abstract class AbstractMethodStrategy implements MethodStrategy {	
	protected Expression expr = null;	

	public Expression getExpression() {
		return expr;
	}

	public void setExpression(Expression expr) {
		this.expr = expr;
	}	
}
