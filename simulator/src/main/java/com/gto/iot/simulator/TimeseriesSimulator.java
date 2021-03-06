package com.gto.iot.simulator;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.gto.iot.stream.DriveSpeed;
import com.gto.iot.stream.Location;
import com.gto.iot.stream.TripData;
import com.gto.iot.util.TcpClient;


public class TimeseriesSimulator implements Runnable {

	private volatile TripData timeseries;

	private volatile ArrayList<Map> stepData;

	private volatile Location lastKnownLocation;

	private boolean running = true;
	
	private volatile double initLat = 0;
	
	private volatile double initLng = 0;
	
	private static final double R = 6372.8; // In kilometers

	private volatile AtomicInteger totalDistance = new AtomicInteger(0);
	
	private static DecimalFormat decimalFormat = new DecimalFormat("####.####");
	
	public TimeseriesSimulator(TripData ts) {
		timeseries = ts;
		try {
			stepData = RouteFinder.getStepDataForTrip(
					timeseries.getStartLocation(), timeseries.getEndLocation());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		List<Location> locationList = null;
		
		initLat =(double) ((Map) stepData.get(0).get("start_location")).get("lat");
		initLng =(double) ((Map) stepData.get(0).get("start_location")).get("lng");
		for (Map step : stepData) {
			System.out.println("Starting to simulate step:" + step +", for a driver of speed:"+timeseries.getDriveSpeed());
			totalDistance.addAndGet((int) ((Map) step.get("distance")).get("value"));
			if (running) {
				locationList = findAllPointsInBetween(step,
						timeseries.getDriveSpeed());
				System.out.println("Identified Locations as :" + locationList);
				try {
					simulateStep(timeseries, step, locationList);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					timeseries.setSpeed(-50);
					createTimeseriesMessage(timeseries, lastKnownLocation);
					terminate();
				}
			}
		}
		// Send completion - C,vin,lat,lng,fuel,battery,distanceTravelled
		triggerCompletion();
	}

	private void triggerCompletion() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("C");
		buffer.append(",");
		buffer.append(timeseries.getVin());
		buffer.append(",");
		buffer.append(lastKnownLocation.getLat()); 
		buffer.append(",");
		buffer.append(lastKnownLocation.getLon());  
		buffer.append(",");
		buffer.append(timeseries.getBattery());
		buffer.append(",");
		buffer.append(timeseries.getFuel());
		buffer.append(",");
		buffer.append(totalDistance.get()/1000); // Distance have to give
		try {
			TcpClient.sendMessage(buffer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void simulateStep(TripData tripData, Map step,
			List<Location> locationList) throws InterruptedException {
		// TODO Auto-generated method stub
		double distanceInKM = 0;
		double durationInHr = 0;
		double avgSpeedRequired = 0;
		distanceInKM = ((int) ((Map) step.get("distance")).get("value")) / 1000d;
		durationInHr = ((int) ((Map) step.get("duration")).get("value")) / 3600d;
		if (durationInHr==0) {
			// This step need not be simulated
			return;
		}
		avgSpeedRequired = (distanceInKM / durationInHr)*timeseries.getDriveSpeed().getValue();
		// TODO - Now we should simulate
		tripData.setSpeed(avgSpeedRequired);
		if (locationList.size() > 0) {
			do {
				Location location = locationList.get(0);
				createTimeseriesMessage(tripData, location);
				lastKnownLocation = location;
				locationList.remove(0);
				Thread.sleep(1000);
			} while (locationList.size() > 0 && running);
		}
	}

	private synchronized void createTimeseriesMessage(TripData tripData, Location location) {
		// TODO Auto-generated method stub
		double stepDistance = haversine(initLat, initLng, location.getLat(), location.getLon());

		StringBuffer tcpData = new StringBuffer();
		tcpData.append("T");
		tcpData.append(",");
		tcpData.append(tripData.getVin());
		if (location != null) {
			tcpData.append(",");
			tcpData.append(location.getLat());
			tcpData.append(",");
			tcpData.append(location.getLon());
		}
		tcpData.append(",");
		tcpData.append(System.currentTimeMillis());
		tcpData.append(",");
		tcpData.append(tripData.getSpeed());
		tcpData.append(",");
		tcpData.append(tripData.getBattery());
		tcpData.append(",");
		tcpData.append(tripData.getFuel());
		tcpData.append(",");
		tcpData.append(1);
		tcpData.append(",");
		tcpData.append(decimalFormat.format(stepDistance));
		// Append distance
		System.out.println(tcpData.toString());
		try {
			TcpClient.sendMessage(tcpData.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Location> findAllPointsInBetween(Map ts,
			DriveSpeed driveSpeed) {
		List<Location> locationList = Collections.synchronizedList(new ArrayList<Location>());
		DecimalFormat decimalFormat = new DecimalFormat("##.###########");
		double startLat = (double) ((Map) ts.get("start_location")).get("lat");
		double endLat = (double) ((Map) ts.get("end_location")).get("lat");
		double startLon = (double) ((Map) ts.get("start_location")).get("lng");
		double endLon = (double) ((Map) ts.get("end_location")).get("lng");
		int durationInSeconds = (int) ((Map) ts.get("duration")).get("value")
				/ driveSpeed.getValue();
		Location startLoc = new Location(startLat, startLon);
		Location endLoc = new Location(endLat, endLon);
		locationList.add(startLoc);
		locationList.add(endLoc);
		do {
			findAllPointsBetweenTwoLatLng(locationList);	
		}while (locationList.size()<durationInSeconds);
		
		return locationList;
	}

	public void terminate() {
		running = false;
	}
	
	int counter =0;
	
	private void findAllPointsBetweenTwoLatLng(List<Location> locationList) {
		Location startLoc, endLoc, middleLat;
		ListIterator<Location> locationIterator = locationList.listIterator();
		int counter = (locationList.size()*2)-1;
		while (locationIterator.hasNext()) {
			 startLoc = locationIterator.next();
			 endLoc = locationIterator.next();
			 middleLat = new Location((startLoc.getLat()+endLoc.getLat())/2, (startLoc.getLon()+endLoc.getLon())/2);
			 locationIterator.previous();
			 locationIterator.add(middleLat);
			 if (counter == locationList.size() ) {
				 return;
			 }
		}
	}
	
	private double haversine(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
	
//	public static void main(String[] args) {
//		Location startLat = new Location(13.006682, 80.247369);
//		Location endLat = new Location(12.948709, 80.240389);
//		ArrayList<Location> list = new ArrayList<Location>();
//		list.add(startLat);
//		list.add(endLat);
//		System.out.println(list);
//		for (int counter=0; counter< 10; counter++) {
//			findAllPointsBetweenTwoLatLng(list);
//			System.out.println(list);	
//		}
//	}
}