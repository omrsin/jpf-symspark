package de.tudarmstadt.thesis.symspark.examples.java.combined;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Example with a flatMap and a filter transformation implemented
 * as an anonymous function.
 * Expected analysis result [-16, 3, 4, 6, -1000000]
 * Expected execution result [20]
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class IntegerFlatMapFilterExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Single Integer FlatMap Test");
		System.out.println("Expected analysis result [-16, 3, 4, 6, -1000000]");
		System.out.println("Expected execution result [20]");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		
		numbers.flatMap(t -> {				
			if(t > 2) return Arrays.asList(t*5, t*3).iterator();
			else return Arrays.asList(t*-1).iterator();				
		})
		.filter(x -> x > 16);
		
		spark.stop();
		spark.close();
	}
}
