package org.apache.spark.api.java.function;

/**
 * Model class that mocks the behavior of {@literal org.apache.spark.api.java.function.FlatMapFunction<T,R>} in Spark 2.0.2
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 * @see <a href=https://spark.apache.org/docs/2.0.2/api/java/org/apache/spark/api/java/function/FlatMapFunction.html>Spark's FlatMapFunction</a>
 *
 */
public interface FlatMapFunction<T, R> {
	java.util.Iterator<R> call(T t) throws Exception;
}
