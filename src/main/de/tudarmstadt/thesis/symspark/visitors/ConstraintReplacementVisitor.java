package de.tudarmstadt.thesis.symspark.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.LinearIntegerConstraint;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

public class ConstraintReplacementVisitor extends CloneExpressionVisitor {
	
	private SymbolicInteger initialExpression;
	private IntegerExpression replacementExpression;
	private List<Constraint> constraints;
	
	public ConstraintReplacementVisitor(SymbolicInteger initialExpression, IntegerExpression replacementExpression) {
		this.initialExpression = initialExpression;
		this.replacementExpression = replacementExpression;
		this.constraints = new ArrayList<Constraint>();
	}
	
	@Override
	public void preVisit(Constraint constraint) {
		if(constraint instanceof LinearIntegerConstraint) preVisit((LinearIntegerConstraint)constraint);
	}
	
	@Override
	public void postVisit(Constraint constraint) {
		if(constraint instanceof LinearIntegerConstraint) postVisit((LinearIntegerConstraint)constraint);
	}
	
	@Override
	public void preVisit(LinearIntegerConstraint constraint) {
		depthStack.push(2);
	}
	
	@Override
	public void postVisit(LinearIntegerConstraint constraint) {
		IntegerExpression r = (IntegerExpression)expressionStack.pop();
		IntegerExpression l = (IntegerExpression)expressionStack.pop();
		LinearIntegerConstraint linConst = new LinearIntegerConstraint(l, constraint.getComparator(), r);
		constraints.add(linConst);		
	}
	
	@Override
	public void postVisit(SymbolicInteger expr) {
		if(expr.equals(initialExpression)) {
			process(replacementExpression);
		} else {
			process(expr);
		}
	}
	
	public List<Constraint> getConstraints() {
		return constraints;
	}
}