package de.tudarmstadt.thesis.symspark.choice;

import java.util.List;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.vm.choice.IntIntervalGenerator;

/**
 * This choice generator aims to keep track of those spark actions that
 * produce multiple output (for example, flatMap). These output expressions
 * are registered in a choice generator at the moment the action is done.
 * It uses an integer interval representing representing the elements in 
 * the attached list.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SparkMultipleOutputChoiceGenerator extends IntIntervalGenerator {

	private List<Expression> expressions;
	
	public SparkMultipleOutputChoiceGenerator(String id, List<Expression> expressions) {
		super(id, 0, expressions.size()-1);
		this.expressions = expressions;
	}
	
	public Expression getNextExpression() {		
		return expressions.get(next);		
	}
}
