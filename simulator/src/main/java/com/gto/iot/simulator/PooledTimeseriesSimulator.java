package com.gto.iot.simulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.gto.iot.stream.TripData;

public class PooledTimeseriesSimulator {

	private static ExecutorService executor;

	private static Map<String, Future<Runnable>> taskMap = new HashMap<String, Future<Runnable>>();

	
	public static void initializePool(int poolSize) {
		executor = Executors.newFixedThreadPool(poolSize);
	}

	public static void runInPool(List<TripData> tripDataList) {
		if (!isPoolInAction()) {
			initializePool(tripDataList.size());
			for (TripData tripData : tripDataList) {
				Runnable runnable = new TimeseriesSimulator(tripData);
				Future<Runnable> futureTask = (Future<Runnable>) executor
						.submit(runnable);
				taskMap.put(tripData.getVin(), futureTask);
			}
		}
		else {
			throw new RuntimeException("A simulation already in progress!");
		}
	}

	public static boolean isPoolInAction() {
		if (executor==null || executor.isShutdown()) {
			return false;	
		}
		return true;
	}
	
	public static void invokeAccident(String vin) {
		if (taskMap.containsKey(vin)) {
			taskMap.get(vin).cancel(true);
			
		}
	}
}
