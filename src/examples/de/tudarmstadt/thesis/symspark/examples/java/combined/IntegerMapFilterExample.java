package de.tudarmstadt.thesis.symspark.examples.java.combined;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

/**
 * Example with two transformations implemented: map and then a filter
 * as anonymous functions.
 * Expected analysis result [-1000000, 3, 7]
 * Expected execution result [9, 9, 12]
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class IntegerMapFilterExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Multiple Integer Filter Test");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		
		numbers.map(new Function<Integer, Integer>() {
			@Override
			public Integer call(Integer v1) throws Exception {
				if(v1 > 5) return v1;
				else return v1*3;
			}			
		})
		.filter(new Function<Integer, Boolean>() {			
			@Override
			public Boolean call(Integer v1) throws Exception {				
				return v1 > 6;
			}
		});
	}
}
