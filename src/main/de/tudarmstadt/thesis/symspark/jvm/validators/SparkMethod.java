package de.tudarmstadt.thesis.symspark.jvm.validators;

public enum SparkMethod {
	FILTER, MAP, FLATMAP, REDUCE;
	
	public static SparkMethod getSparkMethod(String methodName) {
		switch (methodName.toLowerCase()) {
		case "filter":
			return FILTER;
		case "map":
			return MAP;
		case "flatmap":
			return FLATMAP;
		default:
			return null;
		}
	}
}
