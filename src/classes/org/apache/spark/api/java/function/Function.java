package org.apache.spark.api.java.function;

/**
 * Model class that mocks the behavior of {@literal org.apache.spark.api.java.function.Function<T1,R>} in Spark 2.0.2
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 * @see <a href=http://spark.apache.org/docs/2.0.2/api/java/org/apache/spark/api/java/function/Function.html>Spark's Function</a>
 *
 */
public interface Function<T1,R> {	
	R call(T1 v1) throws Exception;
}