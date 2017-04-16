package de.tudarmstadt.thesis.symspark.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidator;
import de.tudarmstadt.thesis.symspark.jvm.validators.SparkValidatorFactory;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class MethodSequenceCoordinator {
	
	private final SparkValidator validator;
	private List<String> methods;
	private Set<Integer> values;
	private boolean insFlag = false;
	private Expression exp = null;
	
	public MethodSequenceCoordinator(Config conf) {
		methods = new ArrayList<String>();
		values = new HashSet<Integer>();
		validator = SparkValidatorFactory.getValidator(conf);		
	}
	
	public void processInstruction(ThreadInfo currentThread, Instruction ins) {
		if(validator.isValid(ins)) {
			insFlag = true;
			methods.add(((INVOKEVIRTUAL) ins).getInvokedMethod().getName());
		} else if(insFlag && ins instanceof INVOKEVIRTUAL && ((INVOKEVIRTUAL)ins).getInvokedMethodName().contains("call")) {
			if(exp == null) {
				exp = (Expression) currentThread.getModifiableTopFrame().getLocalAttr(1);
			}
			currentThread.getModifiableTopFrame().setLocalAttr(1, exp);						
			insFlag = false;
		}	
	}
	
	public void processSolution(VM vm) {
		Optional<PathCondition> option = getPathCondition(vm.getChoiceGenerator()); 
		option.ifPresent(pc -> {
			pc.solve();
			values.add(((SymbolicInteger) exp).solution);
		});
	}
	
	public void processPathCondition(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		if(exitedMethod.getName().contains("filter")) {
			Optional<PCChoiceGenerator> option = getPCChoiceGenerator(vm.getChoiceGenerator()); 
			option.ifPresent(cg -> {
				if(cg.getNextChoice() == 1) currentThread.breakTransition(true);
			});		
		}		
	}
			
	public List<String> getMethods() {
		return methods;
	}
	
	public Expression getExpression() {
		return exp;
	}
	
	public Set<Integer> getValues() {
		return values;
	}
	
	private Optional<PathCondition> getPathCondition(ChoiceGenerator<?> cg) {
		Optional<PCChoiceGenerator> option = getPCChoiceGenerator(cg);
		return option.map(ccg -> ccg.getCurrentPC());
	}
	
	private Optional<PCChoiceGenerator> getPCChoiceGenerator(ChoiceGenerator<?> cg) {
		if (!(cg instanceof PCChoiceGenerator)){
			if (cg == null) return Optional.empty();
			PCChoiceGenerator pccg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
			return Optional.ofNullable(pccg);
		} else {			
			return Optional.of((PCChoiceGenerator)cg);
		}		
	}
}
