package de.tudarmstadt.thesis.symspark.examples.java.filter;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

/**
 * Example with multiple filter transformations implemented
 * as lambda anonymous functions.
 * Expected analysis result [-1000000, 3, 7]
 * Expected execution result [3, 3, 4]
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class MultipleIntegerLambdaFilterExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Multiple Integer Lambda Filter Test");
		System.out.println("Expected analysis result [-1000000, 3, 7]");
		System.out.println("Expected execution result [3, 3, 4]");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		
		numbers.filter(v1 -> v1 > 2)
		   	   .filter(v1 -> v1 <= 6);
	}
}
