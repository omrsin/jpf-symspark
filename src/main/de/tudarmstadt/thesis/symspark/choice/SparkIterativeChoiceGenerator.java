package de.tudarmstadt.thesis.symspark.choice;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;

/**
 * This choice generator aims to keep track of those spark actions that
 * iterate on the dataset. For example, a reduce action iterates over 
 * the whole RDD dataset to produce a single value.
 * The interval of the integer options is not really relevant. It is only
 * used to keep track of how many iterations will be executed.
 * Also, it keeps track of the original single input of the operation
 * (the value that is not accumulated) as the initial symbolic expression
 * and also it keeps track of the accumulated symbolic expression so far.
 * The latter is updated in the post-processing of the iterative action.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SparkIterativeChoiceGenerator extends ChoiceGeneratorBase<Expression> {

	private Expression inputExpression;
	private List<Expression> accumulatedExpressions;
	private int iterations;
	private int executedIterations;
	private int currentIndex;
	private int depthIndex;
	private boolean hasEntryNode;
	private boolean isFirstTimeIn;
	
	public SparkIterativeChoiceGenerator(String id, int iterations) {
		super(id);
		this.accumulatedExpressions = new ArrayList<Expression>();
		this.iterations = iterations;
		this.executedIterations = 0;
		this.depthIndex = 0;
		this.currentIndex = 0;
		this.depthIndex = 0;
		this.hasEntryNode = false;
		this.isFirstTimeIn = true;
	}
	
	@Override
	public Expression getNextChoice() {
		if(currentIndex < accumulatedExpressions.size()) {
			return accumulatedExpressions.get(currentIndex);
		}
		return null;
		
	}

	@Override
	public Class<Expression> getChoiceType() {		
		return Expression.class;
	}

	@Override
	public boolean hasMoreChoices() {		
		return iterations != executedIterations;
	}

	@Override
	public void advance() {
		currentIndex++;
		if(currentIndex == depthIndex) {
			executedIterations++;
			depthIndex = accumulatedExpressions.size();
		}
	}

	@Override
	public void reset() {}

	@Override
	public int getTotalNumberOfChoices() {		
		return accumulatedExpressions.size();
	}

	@Override
	public int getProcessedNumberOfChoices() {		
		return currentIndex;
	}
	
	public Expression getInputExpression() {
		return inputExpression;
	}
	
	public void setInputExpression(Expression expression) {
		this.inputExpression = expression;
	}
	
	public void addAccumulatedExpression(Expression expression) {		
		accumulatedExpressions.add(expression);
		if(isFirstTimeIn) {
			depthIndex++;
		}		
	}
	
	public boolean hasEntryNode() {
		return hasEntryNode;
	}
	
	public void addEntryNode(Expression expression) {
		this.inputExpression = expression;
		addAccumulatedExpression(expression);
		hasEntryNode = true;
	}
}
