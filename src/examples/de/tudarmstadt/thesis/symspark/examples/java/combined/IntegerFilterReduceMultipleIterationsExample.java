package de.tudarmstadt.thesis.symspark.examples.java.combined;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Example with a filter transformation and a reduce action 
 * implemented as labda functions.
 * Expected analysis result [3, 4, -1000000]
 * Expected execution result 11
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class IntegerFilterReduceMultipleIterationsExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Integer Filter Reduce with Multiple Iterations Test");
		System.out.println("Expected analysis result [3, 4, -1000000]");
		System.out.println("Expected execution result 11");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		
		numbers.filter(v1 -> v1 > 2)
			   .filter(v1 -> v1 <= 100)
			   .reduce((v1, v2) -> {
				   if(v1 > 3) return v1+v2+1;
				   else return v1+v2;
			   });
		
		spark.stop();
		spark.close();
	}
}
