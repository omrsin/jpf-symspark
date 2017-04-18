package de.tudarmstadt.thesis.symspark.jvm.validators;

public enum SparkMethod {
	FILTER, MAP, REDUCE;
	
	public static SparkMethod getSparkMethod(String methodName) {
		switch (methodName) {
		case "filter":
			return FILTER;
		case "map":
			return MAP;
		default:
			return null;
		}
	}
}
