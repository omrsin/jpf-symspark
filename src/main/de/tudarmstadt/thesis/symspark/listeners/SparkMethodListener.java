package de.tudarmstadt.thesis.symspark.listeners;

import java.io.PrintWriter;
import java.util.List;
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

public class SparkMethodListener extends PropertyListenerAdapter implements PublisherExtension {
	
	private static final Logger LOGGER = JPF.getLogger(SparkMethodListener.class.getName());
	private static final String CLASS = SparkMethodListener.class.getSimpleName()+": ";	
	
	private MethodSequenceCoordinator coordinator;
	
	public SparkMethodListener(Config conf, JPF jpf) {
		LOGGER.log(Level.INFO, CLASS + "Using SparkMethodListener");		
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
	public void stateAdvanced(Search search) {
		if (search.isEndState()) {			
			coordinator.setEndStateReached(true);
		}
	}
	
	@Override
	public void publishFinished(Publisher publisher) {
		PrintWriter pw = publisher.getOut();
		publisher.publishTopicStart("PathConditionListener");		
		if(!coordinator.getSolutions().isEmpty()) {
			pw.println("Regular Dataset:");
			pw.println(coordinator.getSolutions());
		}
		if(!coordinator.getIterativeSolutions().isEmpty()) {
			pw.println("Iterative Dataset: ");
			for(List<String> iterativeSolutions : coordinator.getIterativeSolutions()) {
				pw.println(iterativeSolutions);
			}
		}		
	}		
}
