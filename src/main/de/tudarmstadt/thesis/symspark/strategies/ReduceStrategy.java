package de.tudarmstadt.thesis.symspark.strategies;

import java.util.Optional;

import de.tudarmstadt.thesis.symspark.choice.SparkIterativeChoiceGenerator;
import de.tudarmstadt.thesis.symspark.visitors.CloneExpressionVisitor;
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

public class ReduceStrategy extends AbstractMethodStrategy implements MethodStrategy {
	
	private int iterations;
	private Expression accumulatedExpression;
	
	public ReduceStrategy(Optional<MethodStrategy> optional, ThreadInfo currentThread) {
		optional.ifPresent(methodStrategy -> {
			this.inputExpression = methodStrategy.getSingleOutputExpression();			
		});
		this.iterations = getMultipleIterations(currentThread);
		setIterativeChoiceGenerator(currentThread);
	}	

	@Override
	public void preProcessing(ThreadInfo currentThread, Instruction ins) {		
		//TODO: This validation could and should be done by the validator
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
	
	private void prepare(ThreadInfo currentThread, int index) {
		if(inputExpression == null) {
			inputExpression = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(index);
		}
		
		if(hasMultipleIterations()) {
			CloneExpressionVisitor cloner = new CloneExpressionVisitor();
			SparkIterativeChoiceGenerator cg = currentThread.getVM().getLastChoiceGeneratorOfType(SparkIterativeChoiceGenerator.class);
			PCChoiceGenerator pccg = currentThread.getVM().getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
			if(cg.isFirstTime()) {
				cg.setInputExpression(inputExpression);
				inputExpression.accept(cloner);
				accumulatedExpression = cloner.getExpression();
				if(pccg != null) {
					updatePathCondition(cg, pccg, ((IntegerExpression)accumulatedExpression), (SymbolicInteger)inputExpression);					
				}
			} else {
				cg.getInputExpression().accept(cloner);
				inputExpression = cloner.getExpression();
				accumulatedExpression = cg.getOutputExpression();
				if(pccg != null) {
					updatePathCondition(cg, pccg, ((IntegerExpression)inputExpression), (SymbolicInteger)cg.getInputExpression());					
				}
			}
			currentThread.getModifiableTopFrame().setLocalAttr(index-1, accumulatedExpression);			
		}
		currentThread.getModifiableTopFrame().setLocalAttr(index, inputExpression);
	}
	
	private void updatePathCondition(SparkIterativeChoiceGenerator cg, PCChoiceGenerator pccg, 
			IntegerExpression replacementExpression, SymbolicInteger originalExpression) {
		
		if(cg.getActivePathCondition() != null) {
			pccg.setCurrentPC(cg.getActivePathCondition());
		}		
		
		if(cg.getInitialConstraint() != null) {
			ConstraintReplacementVisitor replacementVisitor = new ConstraintReplacementVisitor(originalExpression, replacementExpression);
			cg.getInitialConstraint().accept(replacementVisitor);
			
			PathCondition pc = pccg.getCurrentPC();
			
			for(Constraint constraint : replacementVisitor.getConstraints()) {
				pc.prependUnlessRepeated(constraint);
			}							
			pccg.setCurrentPC(pc);
		}				
	}

	private int getMultipleIterations(ThreadInfo currentThread) {
		Config conf = currentThread.getVM().getConfig();
		int iterations = conf.getInt("spark.reduce.iterations");
		return iterations;
	}

	private void setIterativeChoiceGenerator(ThreadInfo currentThread) {		
		if(hasMultipleIterations()) {			
			//Check for an already existing cg or create a new one
			SparkIterativeChoiceGenerator cg = currentThread.getVM().getLastChoiceGeneratorOfType(SparkIterativeChoiceGenerator.class);
			if(cg == null) {
				PCChoiceGenerator pccg = currentThread.getVM().getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
				Constraint initialConstraint = null;
				if(pccg != null) {
					initialConstraint = pccg.getCurrentPC().header;
				}
				cg = new SparkIterativeChoiceGenerator("ReduceCG", iterations, initialConstraint);
				currentThread.getVM().getSystemState().setNextChoiceGenerator(cg);
			}
		}
	}
	
	private void updateIterativeChoiceGenerator(ThreadInfo currentThread) {
		if(hasMultipleIterations()) {
			SparkIterativeChoiceGenerator cg = currentThread.getVM().getLastChoiceGeneratorOfType(SparkIterativeChoiceGenerator.class);
			PCChoiceGenerator pccg = currentThread.getVM().getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
			PathCondition pathCondition = null;
			if(pccg != null) {
				pathCondition = pccg.getCurrentPC().make_copy();
			}
			cg.setOutputExpression((Expression) currentThread.getModifiableTopFrame().getSlotAttr(3), pathCondition);
		}		
	}
	
	public boolean hasMultipleIterations() {
		return iterations > 0;
	}
}
