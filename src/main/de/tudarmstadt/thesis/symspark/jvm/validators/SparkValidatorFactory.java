package de.tudarmstadt.thesis.symspark.jvm.validators;

import gov.nasa.jpf.Config;

/**
 * Factory that creates the right validator depending on the
 * configuration parameters passed by the user. It defaults 
 * to the JavaSparkValidator if no validator is specified.
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SparkValidatorFactory {
	
	public static SparkValidator getValidator(Config conf) {
		String validator = conf.getString("spark.validator");
		if (validator == null) {
			validator = "java";
		}
		return createValidator(validator, conf);
	}

	private static SparkValidator createValidator(String validator, Config conf) {
		switch (validator) {
		case "java":
			return new JavaSparkValidator(conf);
		default:
			break;
		}
		return null;
	}

}
