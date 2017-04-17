package de.tudarmstadt.thesis.symspark.listeners;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class PathConditionListener extends PropertyListenerAdapter implements PublisherExtension {
	
	private static final Logger LOGGER = JPF.getLogger(PathConditionListener.class.getName());
	private static final String CLASS = PathConditionListener.class.getSimpleName()+": ";	
	
	private MethodSequenceCoordinator coordinator;
	
	public PathConditionListener(Config conf, JPF jpf) {
		LOGGER.log(Level.INFO, CLASS + "Using PathConditionListener");		
		coordinator = new MethodSequenceCoordinator(conf);		
		
		jpf.addPublisherExtension(ConsolePublisher.class, this);
	}

	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
		coordinator.detectSparkInstruction(currentThread, executedInstruction);			
	}
	
	@Override
	public void stateBacktracked(Search search) {
		coordinator.processSolution(search.getVM());		
	}
	
	@Override
	public void methodExited(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
		coordinator.processPathCondition(vm, currentThread, exitedMethod);		
	}
	
	@Override
	public void propertyViolated(Search search) {
		//TODO: Do something if a PathCondition is unsatisfiable
		System.out.println("Property violated");
	}

	@Override
	public void publishFinished(Publisher publisher) {
		PrintWriter pw = publisher.getOut();
		publisher.publishTopicStart("PathConditionListener Test");
		for (String method: coordinator.getMethods()) {
			pw.println("method: "+ method);
		}		
//		pw.println(coordinator.getExpression());
		pw.println(coordinator.getValues());
	}	

	@Override
	public void choiceGeneratorRegistered(VM vm, ChoiceGenerator<?> nextCG, ThreadInfo currentThread, Instruction executedInstruction) {
		System.out.println("CG registered: "+nextCG.getClass().getSimpleName());
	}

	@Override
	public void choiceGeneratorSet(VM vm, ChoiceGenerator<?> newCG) {
		System.out.println("CG set: "+newCG.getClass().getSimpleName());
	}

	@Override
	public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
		System.out.println("CG advanced: "+currentCG.getClass().getSimpleName());
	}

	@Override
	public void choiceGeneratorProcessed(VM vm, ChoiceGenerator<?> processedCG) {
		System.out.println("CG processed: "+processedCG.getClass().getSimpleName());
	}	
}
