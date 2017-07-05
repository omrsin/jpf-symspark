package de.tudarmstadt.thesis.symspark.strategies;

import java.util.Optional;

import de.tudarmstadt.thesis.symspark.choice.SparkIterativeChoiceGenerator;
import de.tudarmstadt.thesis.symspark.visitors.ExpressionClonerVisitor;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.symbc.numeric.Expression;
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
			ExpressionClonerVisitor cloner = new ExpressionClonerVisitor();
			SparkIterativeChoiceGenerator cg = currentThread.getVM().getLastChoiceGeneratorOfType(SparkIterativeChoiceGenerator.class);
			if(cg.isFirstTime()) {
				cg.setInputExpression(inputExpression);
				inputExpression.accept(cloner);
				accumulatedExpression = cloner.getExpression();
			} else {
				cg.getInputExpression().accept(cloner);
				inputExpression = cloner.getExpression();
				accumulatedExpression = cg.getOutputExpression();				
			}
			currentThread.getModifiableTopFrame().setLocalAttr(index-1, accumulatedExpression);			
		}
		currentThread.getModifiableTopFrame().setLocalAttr(index, inputExpression);
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
				cg = new SparkIterativeChoiceGenerator("ReduceCG", iterations);
				currentThread.getVM().getSystemState().setNextChoiceGenerator(cg);
			}
		}
	}
	
	private void updateIterativeChoiceGenerator(ThreadInfo currentThread) {
		if(hasMultipleIterations()) {
			SparkIterativeChoiceGenerator cg = currentThread.getVM().getLastChoiceGeneratorOfType(SparkIterativeChoiceGenerator.class);
			cg.setOutputExpression((Expression) currentThread.getModifiableTopFrame().getSlotAttr(3));
		}		
	}
	
	private boolean hasMultipleIterations() {
		return iterations > 0;
	}
}
