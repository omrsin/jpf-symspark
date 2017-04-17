package de.tudarmstadt.thesis.symspark.util;

import java.util.Optional;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;

public class PCChoiceGeneratorUtils {
	
	public static Optional<PathCondition> getPathCondition(ChoiceGenerator<?> cg) {
		Optional<PCChoiceGenerator> option = getPCChoiceGenerator(cg);
		return option.map(ccg -> ccg.getCurrentPC());
	}
	
	public static Optional<PCChoiceGenerator> getPCChoiceGenerator(ChoiceGenerator<?> cg) {
		if (!(cg instanceof PCChoiceGenerator)){
			if (cg == null) return Optional.empty();
			PCChoiceGenerator pccg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
			return Optional.ofNullable(pccg);
		} else {			
			return Optional.of((PCChoiceGenerator)cg);
		}		
	}
}
