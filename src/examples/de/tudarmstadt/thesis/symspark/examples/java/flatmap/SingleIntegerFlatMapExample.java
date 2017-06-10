package de.tudarmstadt.thesis.symspark.examples.java.flatmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;

/**
 * Example with a single filter transformation implemented
 * as an anonymous function.
 * Expected analysis result [-1000000, 3]
 * Expected execution result [3, 3, 4]
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SingleIntegerFlatMapExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Single Integer FlatMap Test");
//		System.out.println("Expected analysis result [-1000000, 3]");
//		System.out.println("Expected execution result [3, 3, 4]");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		
		numbers.flatMap(new FlatMapFunction<Integer, Integer>() {
			@Override
			public Iterator<Integer> call(Integer t) throws Exception {				
				if(t > 2) return Arrays.asList(t*5, t*2, t*3).iterator();
				else return Arrays.asList(t*4).iterator();				
			}
		});
		
		spark.stop();
		spark.close();
	}
}
