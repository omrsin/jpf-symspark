package de.tudarmstadt.thesis.symspark.choice;

import java.util.Stack;

import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;

/**
 * This choice generator aims to keep track of those spark actions that
 * iterate on the dataset. For example, a reduce action iterates over 
 * the whole RDD dataset to produce a single value.
 * The interval of the integer options is not really relevant. It is only
 * used to keep track of how many iterations will be executed.
 * Also, it keeps track of the accumulated output that will serve as an input
 * for the next iteration.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SparkIterativeChoiceGenerator extends ChoiceGeneratorBase<Integer> {

	private int totalIterations;
	private int currentIteration;
	private Stack<StackExpression> outputExpressions = new Stack<StackExpression>();	
	private Expression inputExpression;
	private boolean firstTime =  true;
	private Constraint initialConstraint;
	private PathCondition activePathCondition;
	
	public SparkIterativeChoiceGenerator(String id, int totalIterations, Constraint initialConstraint) {
		super(id);
		this.totalIterations = totalIterations;
		this.currentIteration = 0;
		this.initialConstraint = initialConstraint;
		this.activePathCondition = null;
	}	

	@Override
	public Integer getNextChoice() {
		return currentIteration;
	}

	@Override
	public Class<Integer> getChoiceType() {
		return Integer.class;
	}

	@Override
	public boolean hasMoreChoices() {
		return !outputExpressions.isEmpty() || currentIteration + 1 < totalIterations;
	}

	@Override
	public void advance() {}

	@Override
	public void reset() {}

	@Override
	public int getTotalNumberOfChoices() {
		return 0;
	}

	@Override
	public int getProcessedNumberOfChoices() {
		return 0;
	}
	
	public void setInputExpression(Expression inputExpression) {
		this.inputExpression = inputExpression;
	}
	
	public Expression getInputExpression() {
		return inputExpression;
	}
	
	public void setOutputExpression(Expression outputExpression, PathCondition pathCondition) {
		if(currentIteration + 1 < totalIterations) {
			outputExpressions.push(new StackExpression(outputExpression, currentIteration+1, pathCondition));
		}
		firstTime = false;
	}
	
	public Expression getOutputExpression(){
		StackExpression stackExp = outputExpressions.pop();
		currentIteration = stackExp.iteration;
		activePathCondition = stackExp.pathCondition;
		return stackExp.expression;
	}
	
	public boolean isFirstTime() {
		return firstTime;
	}
	
	public Constraint getInitialConstraint() {
		return initialConstraint;
	}
	
	public PathCondition getActivePathCondition() {
		return activePathCondition;
	}
	
	private class StackExpression {
		public Expression expression;
		public int iteration;
		public PathCondition pathCondition;
		
		public StackExpression(Expression expression, int iteration, PathCondition pathCondition) {
			this.expression = expression;
			this.iteration = iteration;
			this.pathCondition = pathCondition;
		}
		
		@Override
		public String toString() {			
			return "{\n"
					+ "iteration: "+iteration+",\n"
					+ "expression: "+expression+",\n"
					+ "pc: "+pathCondition+"\n"
					+ "}";
		}
	}	
}