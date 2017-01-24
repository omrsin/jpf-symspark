package org.apache.spark.api.java.function;

public interface Function<T1, R> {
	
	R call(T1 v1) throws Exception;
}