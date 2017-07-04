package de.tudarmstadt.thesis.symspark.jvm.bytecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tudarmstadt.thesis.symspark.util.SparkMethodBuilder;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

public class INVOKEVIRTUAL extends gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL {
	
	private static final Logger LOGGER = JPF.getLogger(INVOKEVIRTUAL.class.getName());
	private static final String CLASS = INVOKEVIRTUAL.class.getSimpleName()+": ";

	public INVOKEVIRTUAL(String clsName, String methodName, String methodSignature) {
		super(clsName, methodName, methodSignature);
	}

	@Override
	public Instruction execute(ThreadInfo th) {		
		LOGGER.log(Level.FINER, CLASS + "InvokedMethod: "+getInvokedMethodName());
		logInstructions();	
		
		Config conf = th.getVM().getConfig();
		List<String> methods = new ArrayList<String>();
		methods.addAll(Arrays.asList(Optional.ofNullable(conf.getStringArray("symbolic.method")).orElse(new String[0])));		
		
		List<String> methodToAnalyze = buildMethodToAnalyze(th);
		LOGGER.log(Level.INFO, CLASS + "Method to be analyzed: "+methodToAnalyze);
		methods.addAll(methodToAnalyze);	
		conf.setProperty("symbolic.method", join(methods, ";"));		

		return super.execute(th);
	}
	
	private void logInstructions() {
		MethodInfo methodInfo = getInvokedMethod();
		for(int i = 0; i < methodInfo.getInstructions().length; i++) {
			LOGGER.log(Level.FINEST, CLASS + "Instruction["+i+"]: "+methodInfo.getInstructions()[i]);			
		}		
	}

	private List<String> buildMethodToAnalyze(ThreadInfo th) {
		ElementInfo ei = (ElementInfo)getArgumentValues(th)[0];
		MethodInfo mi = getInvokedMethod();
		return new SparkMethodBuilder().build(th, ei, mi);
	}	

	private String join(List<String> list, String separator) {
		StringBuilder builder = new StringBuilder();
		for(String element : list) {
			builder.append(element+separator);
		}
		return builder.toString();
	}
}
