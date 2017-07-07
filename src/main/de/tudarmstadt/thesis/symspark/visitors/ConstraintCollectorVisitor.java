package de.tudarmstadt.thesis.symspark.visitors;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.LinearIntegerConstraint;

public class ConstraintCollectorVisitor extends ConstraintExpressionVisitor {
	
	private List<Constraint> constraints;
	
	public ConstraintCollectorVisitor() {
		this.constraints = new ArrayList<Constraint>();
	}
	
	@Override
	public void preVisit(Constraint constraint) {
		if(constraint instanceof LinearIntegerConstraint) preVisit((LinearIntegerConstraint)constraint);
	}
	
	@Override
	public void preVisit(LinearIntegerConstraint constraint) {
		constraints.add(new LinearIntegerConstraint(constraint.getLeft(), constraint.getComparator(), constraint.getRight()));
	}
	
	public List<Constraint> getConstraints() {
		return constraints;
	}

}
