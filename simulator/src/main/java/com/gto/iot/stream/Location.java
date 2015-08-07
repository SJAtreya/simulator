package com.gto.iot.stream;

public class Location {
	private double lat;
	private double lon;
	private long ts;

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public Location(double d, double e) {
		lat = d;
		lon = e;
	}

	public Location(String d, String e) {
		// TODO Auto-generated constructor stub
		lat = Double.valueOf(d);
		lon = Double.valueOf(e);
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	@Override
	public String toString() {
		return "["+lat + "," + lon+"]";
	}

}