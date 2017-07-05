package de.tudarmstadt.thesis.symspark.visitors;

import java.util.Stack;

import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils.VarType;
import gov.nasa.jpf.symbc.numeric.BinaryLinearIntegerExpression;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.symbc.numeric.visitors.CollectVariableVisitor;

public class ExpressionClonerVisitor extends CollectVariableVisitor {
	
	Expression expression;
	Stack<Integer> depthStack = new Stack<Integer>();
	Stack<Expression> expressionStack = new Stack<Expression>();

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
	
	private void process(Expression expr) {
		if(depthStack.isEmpty()) {			
			expression = expr;
		} else {
			Integer depth = depthStack.pop();
			if(depth > 1) depthStack.push(depth-1);
			expressionStack.push(expr);
		}		
	}	
}
