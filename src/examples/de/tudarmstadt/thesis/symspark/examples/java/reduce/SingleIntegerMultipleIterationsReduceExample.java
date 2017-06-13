package de.tudarmstadt.thesis.symspark.examples.java.reduce;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

/**
 * Example with a single reduce action implemented
 * as a lambda function.
 * Expected analysis result [-1000000, 3]
 * Expected execution result [3, 3, 4]
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class SingleIntegerMultipleIterationsReduceExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Single Integer Reduce Test");
		System.out.println("Expected analysis result [-1000000, 3]");
		System.out.println("Expected execution result 16");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		
		numbers.reduce(new Function2<Integer, Integer, Integer>() {			
			@Override
			public Integer call(Integer v1, Integer v2) throws Exception {
				if(v1 > 8) return v1+v2+1;
				else return v1+v2;				
			}
		});
		
		spark.stop();
		spark.close();
	}
}
