package org.apache.spark.api.java;

import java.util.List;

import org.apache.spark.SparkConf;

public class JavaSparkContext {
	
	public JavaSparkContext(SparkConf conf){
		System.out.println("JavaSparkContext.new");
	}
	
	public <T> JavaRDD<T> parallelize(List<T> list) {
		System.out.println("JavaSparkContext.parallelize");
		return new JavaRDD<T>(list);
	}

	public void stop() {	
		System.out.println("JavaSparkContext.stop");
	}
	
	public void close() {	
		System.out.println("JavaSparkContext.close");
	}
}