package org.apache.spark.api.java.function;

/**
 * Model class that mocks the behavior of {@literal org.apache.spark.api.java.function.Function2<T1,T2,R>} in Spark 2.0.2
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 * @see <a href=http://spark.apache.org/docs/2.0.2/api/java/org/apache/spark/api/java/function/Function2.html>Spark's Function2</a>
 *
 */
public interface Function2 <T1,T2,R> {
	R call(T1 v1, T2 v2) throws Exception;
}
