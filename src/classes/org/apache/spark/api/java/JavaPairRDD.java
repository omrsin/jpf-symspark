package org.apache.spark.api.java;

import java.util.List;

import org.apache.spark.api.java.function.Function2;

import scala.Tuple2;

public class JavaPairRDD<K, V> {

	private List<Tuple2<K, V>> list_t;
	
	public JavaPairRDD(List<Tuple2<K, V>> list_t) {
		this.list_t = list_t;
	}
	
	public JavaPairRDD<K, V> reduceByKey(Function2<V, V, V> f) {
		return this;
	}
	
	public void saveAsTextFile(String file){}
}
