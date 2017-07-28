package de.tudarmstadt.thesis.symspark.examples.java.evaluation;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class IterativeMapReduceCumulativeConditionExample {
	public static void main(String[] args) {		
		SparkConf conf = new SparkConf()
				.setAppName("JavaNumbers")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
		
		List<Integer> numberList = Arrays.asList(1,2,3,3,4);
		JavaRDD<Integer> numbers = spark.parallelize(numberList);
		numbers.map(v1 -> {
			if(v1 > 2) return v1 + 1;
			else return v1 +2;
		}).reduce((v1, v2) -> {
			if(v1 > 20) return v1+v2+1;
			else return v1+v2;
		});
		
		spark.stop();
		spark.close();
	}
}
