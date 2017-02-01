package org.apache.spark.api.java;

import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

/**
 * Model class that mocks the behavior of {@literal org.apache.spark.api.java.JavaRDD<T>} in Spark 2.0.2
 * @author Omar Erminy (omar.erminy.ugueto@gmail.com)
 * @see <a href=http://spark.apache.org/docs/2.0.2/api/java/org/apache/spark/api/java/JavaRDD.html>Spark's JavaRDD</a>
 *
 */
public class JavaRDD<T> {
	
	private List<T> list_t;
		
	public JavaRDD(List<T> list_t) {	
		this.list_t = list_t;
	}

	public JavaRDD<T> filter(Function<T,Boolean> f) {		
		try {
			f.call(list_t.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public <R> JavaRDD<R> map(Function<T, R> f) {		
		try {
			f.call(list_t.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		} return (JavaRDD<R>) this;
	}
	
	public T reduce(Function2<T, T, T> f) {		
		try {
			f.call(list_t.get(0), list_t.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list_t.get(0);		
	}
	
	public List<T> collect() {		
		return list_t;
	}
}