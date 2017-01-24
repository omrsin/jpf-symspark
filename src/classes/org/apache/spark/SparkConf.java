package org.apache.spark;

public class SparkConf {
	
	public SparkConf setAppName(String name) {
		System.out.println("SparkConf.setAppName");
		return this;
	}
	
	public SparkConf setMaster(String master) {
		System.out.println("SparkConf.setMaster");
		return this;
	}
}