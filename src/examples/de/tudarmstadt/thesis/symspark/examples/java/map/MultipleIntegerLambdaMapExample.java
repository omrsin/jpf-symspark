package de.tudarmstadt.thesis.symspark.examples.java.map;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Example with multiple map transformations implemented
 * as lambda functions.
 * Expected analysis result [-1000000, 1, 6]
 * Expected execution result [1, 4, 6, 6, 8]
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class MultipleIntegerLambdaMapExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Multiple Integer Lambda Map Test");
		System.out.println("Expected analysis result [-1000000, 1, 6]");
		System.out.println("Expected execution result [1, 4, 6, 6, 8]");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		

		numbers.map(v1 -> {if(v1 > 5) return v1*3; else return v1*2;})
			   .map(v1 -> {if(v1 == 2) return 1; else return v1; });
	}
}
