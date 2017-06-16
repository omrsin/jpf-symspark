package org.apache.spark.api.java;

import java.util.Arrays;
import java.util.List;
import org.apache.spark.SparkConf;

/**
 * Model class that mocks the behavior of org.apache.spark.api.java.JavaSparkContext in Spark 2.0.2
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 * @see <a href=http://spark.apache.org/docs/2.0.2/api/java/org/apache/spark/api/java/JavaSparkContext.html>Spark's JavaSparkContext</a>
 *
 */
public class JavaSparkContext {	
	public JavaSparkContext(SparkConf conf){}
	public void stop() {}	
	public void close() {}
	public <T> JavaRDD<T> parallelize(List<T> list) {		
		return new JavaRDD<T>(list);
	}
	public JavaRDD<String> textFile(String file) {
		return new JavaRDD<String>(Arrays.asList(""));
	}
}