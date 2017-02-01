package org.apache.spark;

/**
 * Model class that mocks the behavior of org.apache.spark.SparkConf in Spark 2.0.2
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 * @see <a href=http://spark.apache.org/docs/2.0.2/api/java/org/apache/spark/SparkConf.html>Spark's SparkConf</a>
 *
 */
public class SparkConf {
	
	public SparkConf setAppName(String name) {		
		return this;
	}
	
	public SparkConf setMaster(String master) {		
		return this;
	}
}