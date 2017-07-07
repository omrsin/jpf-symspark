package de.tudarmstadt.thesis.symspark.strategies;

import java.util.Optional;

import de.tudarmstadt.thesis.symspark.choice.SparkIterativeChoiceGenerator;
import de.tudarmstadt.thesis.symspark.visitors.CloneExpressionVisitor;
import de.tudarmstadt.thesis.symspark.visitors.ConstraintCollectorVisitor;
import de.tudarmstadt.thesis.symspark.visitors.ConstraintReplacementVisitor;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class IterativeReduceStrategy extends AbstractMethodStrategy implements MethodStrategy {

	private int iterations;
	private Expression symbolicVariable;
	private Expression iterativeSymbolicVariable;
	private Expression accumulatedExpression;
	
	public IterativeReduceStrategy(Optional<MethodStrategy> optional, int iterations, ThreadInfo currentThread, Expression symbolicVariable) {
		optional.ifPresent(methodStrategy -> {
			this.inputExpression = methodStrategy.getSingleOutputExpression();
			if(methodStrategy instanceof IterativeReduceStrategy) 
				this.iterativeSymbolicVariable = ((IterativeReduceStrategy) methodStrategy).getIterativeSymbolicVariable();
		});
		this.iterations = iterations;
		this.symbolicVariable = symbolicVariable;		
		setIterativeChoiceGenerator(currentThread);
	}	

	@Override
	public void preProcessing(ThreadInfo currentThread, Instruction ins) {
		if(ins instanceof INVOKEVIRTUAL && ((INVOKEVIRTUAL)ins).getInvokedMethodName().contains("call")) {
			prepare(currentThread, 2);
		} else if(ins instanceof INVOKESTATIC && ((INVOKESTATIC)ins).getInvokedMethodName().contains("lambda")) {
			prepare(currentThread, 1);
		}
	}

	@Override
	public void postProcessing(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		if(exitedMethod.getName().contains("call")) {
			if(currentThread.getCallerStackFrame().getPC() instanceof INVOKEVIRTUAL || 
			   exitedMethod.getClassName().contains("$$Lambda")) {
				updateIterativeChoiceGenerator(currentThread);
				currentThread.breakTransition(true);
				endStateForced = true;
			}
		}		
	}
	
	@Override
	public boolean isIterative() {	
		return true;
	}
	
	private void prepare(ThreadInfo currentThread, int index) {
		if(inputExpression == null) {
			inputExpression = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(index);
		}		

		CloneExpressionVisitor cloner = new CloneExpressionVisitor();
		SparkIterativeChoiceGenerator cg = currentThread.getVM().getLastChoiceGeneratorOfType(SparkIterativeChoiceGenerator.class);
		if(cg.isFirstTime()) {
			cg.setInputExpression(inputExpression);
			inputExpression.accept(cloner);
			accumulatedExpression = cloner.getExpression();
			iterativeSymbolicVariable = cloner.getNewSymbolicVariable();
		} else {
			cg.getInputExpression().accept(cloner);
			inputExpression = cloner.getExpression();
			accumulatedExpression = cg.getOutputExpression();
			iterativeSymbolicVariable = cloner.getNewSymbolicVariable();				
		}
		
		currentThread.getModifiableTopFrame().setLocalAttr(index-1, accumulatedExpression);
		currentThread.getModifiableTopFrame().setLocalAttr(index, inputExpression);
	}	
	
	private void updateIterativeChoiceGenerator(ThreadInfo currentThread) {
		SparkIterativeChoiceGenerator cg = currentThread.getVM().getLastChoiceGeneratorOfType(SparkIterativeChoiceGenerator.class);
		PCChoiceGenerator pccg = currentThread.getVM().getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
		PathCondition pathCondition = null;
		if(pccg != null) {
			if(cg.getInitialConstraint() != null) {
				ConstraintReplacementVisitor replacementVisitor = 
						new ConstraintReplacementVisitor((SymbolicInteger)symbolicVariable, (IntegerExpression)iterativeSymbolicVariable);
				cg.getInitialConstraint().accept(replacementVisitor);				
				PathCondition pc = pccg.getCurrentPC();				
				for(Constraint constraint : replacementVisitor.getConstraints()) {
					pc.prependUnlessRepeated(constraint);
				}				
				pccg.setCurrentPC(pc);
			}
			
			if(cg.getActivePathCondition() != null) {
				PathCondition pc = pccg.getCurrentPC();
				ConstraintCollectorVisitor collectorVisitor = new ConstraintCollectorVisitor();
				cg.getActivePathCondition().header.accept(collectorVisitor);
				for(Constraint constraint : collectorVisitor.getConstraints()) {
					pc.prependUnlessRepeated(constraint);
				}
				pccg.setCurrentPC(pc);
			}
			pathCondition = pccg.getCurrentPC().make_copy();
		}
		cg.setOutputExpression((Expression) currentThread.getModifiableTopFrame().getSlotAttr(3), pathCondition);
	}

	private void setIterativeChoiceGenerator(ThreadInfo currentThread) {			
		// Create a new SparkIterative choice generator if there is none already
		SparkIterativeChoiceGenerator cg = currentThread.getVM().getLastChoiceGeneratorOfType(SparkIterativeChoiceGenerator.class);
		if(cg == null) {
			PCChoiceGenerator pccg = currentThread.getVM().getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
			Constraint initialConstraint = null;
			if(pccg != null) {
				initialConstraint = pccg.getCurrentPC().header;
			}
			cg = new SparkIterativeChoiceGenerator("ReduceCG", iterations, initialConstraint, symbolicVariable);
			currentThread.getVM().getSystemState().setNextChoiceGenerator(cg);
		}
	}
	
	public Expression getIterativeSymbolicVariable() {
		return iterativeSymbolicVariable;
	}
}
