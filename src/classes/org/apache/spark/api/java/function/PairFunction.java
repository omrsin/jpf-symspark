package org.apache.spark.api.java.function;

import scala.Tuple2;

public interface PairFunction<T, K, V> {
	Tuple2<K, V> call(T t) throws Exception;
}
