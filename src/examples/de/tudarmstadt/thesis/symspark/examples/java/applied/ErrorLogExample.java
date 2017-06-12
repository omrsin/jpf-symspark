package de.tudarmstadt.thesis.symspark.examples.java.applied;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Example with a plausible use case. It aims so demonstrate how
 * analyzing symbolic strings can be useful in a real life example
 *
 * The idea is to process an application log that registers different
 * kinds of behavior. The idea is to filter out only the errors and 
 * warnings and group them according the module where they occurred.
 * 	
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class ErrorLogExample {
	
	private static final String ERROR = "ERR";
	private static final String WARNING = "WAR";
	private static final String DATABASE = "database";
	private static final String SERVER = "server";
	private static final String APPLICATION = "application";
	
	/*
	 * The structure of the data is as follows:
	 * LogLevel ModuleAffected AdditionalInformation	 
	 */
	private static final List<String> logEntries = Arrays.asList(
			"ERR database information1",
			"ERR server information2",
			"WAR database information3",
			"ACC database information4"
	);
	
	public static void main(String[] args) {
		System.out.println("Spark Java Error Log Test");
		System.out.println("Expected analysis result [\"ERR  \", \" \", \"ERR server\", \"WAR\", \"ERR database\", \"ERR application\"]");
		System.out.println("Expected execution result [1, 2, 1]");
		
		SparkConf conf = new SparkConf()
				.setAppName("JavaErrorLog")
				.setMaster("local");
		
		JavaSparkContext spark = new JavaSparkContext(conf);
				
		JavaRDD<String> logRDD = spark.parallelize(logEntries);
		logRDD.filter(entry -> entry.startsWith(ERROR) || entry.startsWith(WARNING))		
		.map(entry -> {			
			String rest = entry.substring(4);			
			if(rest.contains(DATABASE)) return 1;
			else if(rest.contains(SERVER)) return 2;
			else if(rest.contains(APPLICATION)) return 3;
			else return 4;
			
		});	
		
		spark.stop();
		spark.close();	
	}
}
