package de.tudarmstadt.thesis.symspark.examples.java.map;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

/**
 * Example with a single map transformation implemented
 * as an anonymous function.
 * Expected analysis result [-1000000, 6]
 * Expected execution result [3, 6, 9, 9, 12]
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SingleIntegerMapExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Single Integer Map Test");
		System.out.println("Expected analysis result [-1000000, 6]");
		System.out.println("Expected execution result [3, 6, 9, 9, 12]");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		
		numbers.map(new Function<Integer, Integer>() {
			@Override
			public Integer call(Integer v1) throws Exception {
				if(v1 > 5) return v1*2;
				else return v1*3;
			}			
		});
		
		spark.stop();
		spark.close();
	}
}
