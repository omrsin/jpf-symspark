package de.tudarmstadt.thesis.symspark.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

public class RootExpressionVisitor extends ConstraintExpressionVisitor {
	
	private Set<Expression> expressions;
	
	public RootExpressionVisitor() {
		this.expressions = new TreeSet<Expression>();
	}
	
	@Override
	public void postVisit(SymbolicInteger expr) {
		expressions.add(expr);
	}
	
	public Set<Expression> getExpressions() {
		return expressions;
	}
}
