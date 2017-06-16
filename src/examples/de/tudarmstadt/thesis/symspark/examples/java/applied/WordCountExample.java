package de.tudarmstadt.thesis.symspark.examples.java.applied;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

/**
 * This example is adapted from the simple version shown in the Spark official 
 * web site at {@link https://spark.apache.org/examples.html}.
 * 
 * It represents the typical word count example with a subtle change that illustrates
 * the application of the symbolic analysis library under development.
 * 
 * The small change consists of only counting words that begin with some common
 * prefixes in the English language.
 * 
 * To achieve this, words beginning with the expected prefixes were filtered using
 * a "filter" transformation after obtaining an RDD of all the words.
 * 	
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 *
 */
public class WordCountExample {
		
	public static void main(String[] args) {
		System.out.println("Spark Java Word Count Example");
		System.out.println("Expected analysis result [\" \", \"un\", \"in\", \"re\"]");
		
		String inputFile = args[0];
	    String outputFile = args[1];
	    // Create a Java Spark Context.
	    SparkConf conf = new SparkConf().setAppName("wordCount");
			JavaSparkContext sc = new JavaSparkContext(conf);
	    
		// Load our input data.
	    JavaRDD<String> textFile = sc.textFile(inputFile);
	    JavaPairRDD<String, Integer> counts = textFile
	    	// Split up into words.
	        .flatMap(s -> Arrays.asList(s.split(" ")).iterator())
	        // Filters words that match the expected prefixes
	        .filter(x -> x.startsWith("re") || x.startsWith("un") || x.startsWith("in"))
	        // Transform into word and count.
	        .mapToPair(word -> new Tuple2<>(word, 1))
	        .reduceByKey((a, b) -> a + b);
	    // Save the word count back out to a text file, causing evaluation.
	    counts.saveAsTextFile(outputFile);
	    
	    sc.stop();
		sc.close();
	}
}
