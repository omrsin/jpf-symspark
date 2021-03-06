package de.tudarmstadt.thesis.symspark.examples.java.map;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

public class SeveralIntegerMapExample {
	public static void main(String[] args) {
		System.out.println("Spark Java Several Integer Map Test");
		System.out.println("Expected analysis result [-1000000, 1, 6]");
		System.out.println("Expected execution result [1, 4, 6, 6, 8]");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		
		numbers.map(new Function<Integer, Integer>() {
			@Override
			public Integer call(Integer v1) throws Exception {
				if(v1 > 5) return v1*3;
				else return v1*2;
			}			
		})
		.map(new Function<Integer, Integer>() {
			@Override
			public Integer call(Integer v1) throws Exception {
				//TODO: Careful when returning non symbolic values
				if(v1 == 2) return v1*4;
				else return v1+10;				
			}			
		})
		.map(new Function<Integer, Integer>() {
			@Override
			public Integer call(Integer v1) throws Exception {
				if(v1 > 24) return v1;
				else return v1+4;
			}
		})
		.filter(x -> x > 10);
		
		spark.stop();
		spark.close();
	}

}
