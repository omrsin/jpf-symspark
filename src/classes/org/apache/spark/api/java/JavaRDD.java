package org.apache.spark.api.java;

import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

public class JavaRDD<T> {
	
	private List<T> list_t;
		
	public JavaRDD(List<T> list_t) {	
		this.list_t = list_t;
	}

	public JavaRDD<T> filter(Function<T,Boolean> f) {
		System.out.println("JavaRDD.filter");
		try {
			f.call(list_t.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public <R> JavaRDD<R> map(Function<T, R> f) {
		System.out.println("JavaRDD.map");
		try {
			f.call(list_t.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		} return (JavaRDD<R>) this;
	}
	
	public T reduce(Function2<T, T, T> f) {
		System.out.println("JavaRDD.reduce");
		try {
			f.call(list_t.get(0), list_t.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list_t.get(0);		
	}
	
	public List<T> collect() {
		System.out.println("JavaRDD.collect");
		return list_t;
	}
}