package org.apache.spark.api.java;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

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
		List<R> list_r = new ArrayList<R>();		 
		try {
			list_r.add(f.call(list_t.get(0)));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return new JavaRDD<R>(list_r);
	}
	
	public <U> JavaRDD<U> flatMap(FlatMapFunction<T,U> f) {
		List<U> list_u = new ArrayList<U>();
		try {
			Iterator<U> it = f.call(list_t.get(0));
			while(it.hasNext()) {
				list_u.add(it.next());
			}						
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JavaRDD<U>(list_u);
	}
	
	public T reduce(Function2<T, T, T> f) {		
		try {
			f.call(list_t.get(0), list_t.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list_t.get(0);		
	}
	
	public <K2,V2> JavaPairRDD<K2, V2> mapToPair(PairFunction<T, K2, V2> f) {
		List<Tuple2<K2, V2>> list_r = new ArrayList<>();
		try {
			f.call(list_t.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JavaPairRDD<>(list_r);
	}
	
	public List<T> collect() {		
		return list_t;
	}
}