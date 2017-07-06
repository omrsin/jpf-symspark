package de.tudarmstadt.thesis.symspark.visitors;

import java.util.Stack;

import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils.VarType;
import gov.nasa.jpf.symbc.numeric.BinaryLinearIntegerExpression;
import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

public class CloneExpressionVisitor extends ConstraintExpressionVisitor {
	
	protected Expression expression;	
	protected Stack<Integer> depthStack;
	protected Stack<Expression> expressionStack;

	public CloneExpressionVisitor() {
		this.depthStack = new Stack<Integer>();
		this.expressionStack = new Stack<Expression>();
	}
	
	@Override
	public void preVisit(BinaryLinearIntegerExpression expr) {
		depthStack.push(2);
	}
	
	@Override
	public void postVisit(BinaryLinearIntegerExpression expr) {
		IntegerExpression r = (IntegerExpression)expressionStack.pop();
		IntegerExpression l = (IntegerExpression)expressionStack.pop();
		BinaryLinearIntegerExpression binExpr = new BinaryLinearIntegerExpression(l, expr.getOp(), r);
		process(binExpr);
	}
	
	@Override
	public void postVisit(SymbolicInteger expr) {
		String name = expr.getName().split("_[0-9]*_SYM.*")[0];
		SymbolicInteger symInt = new SymbolicInteger(BytecodeUtils.varName(name, VarType.INT));
		process(symInt);
	}
	
	@Override
	public void postVisit(IntegerConstant expr) {		
		process(expr);
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	protected void process(Expression expr) {
		if(depthStack.isEmpty()) {			
			expression = expr;
		} else {
			Integer depth = depthStack.pop();
			if(depth > 1) depthStack.push(depth-1);
			expressionStack.push(expr);
		}		
	}	
}
