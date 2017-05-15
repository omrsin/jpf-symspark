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
		coordinator.percolateToNextMethod(vm, currentThread, exitedMethod);		
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
		pw.println(coordinator.getValues());
	}

	@Override
	public void stateAdvanced(Search search) {
		if (search.isEndState()) {
			System.out.println("End state reached");
			coordinator.setEndStateReached(true);
		}
	}	
}
