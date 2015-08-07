package com.gto.iot.stream;

import java.util.Map;

public class TripData {

	private String vin;
	
	private double speed;
	
	private String startLocation;
	
	private String endLocation;
	
	private double fuel;
	
	private double battery;
	
	private DriveSpeed driveSpeed = DriveSpeed.MEDIUM;
	
	public DriveSpeed getDriveSpeed() {
		return driveSpeed;
	}

	public void setDriveSpeed(DriveSpeed driveSpeed) {
		this.driveSpeed = driveSpeed;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public String getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(String startLocation) {
		this.startLocation = startLocation;
	}

	public String getEndLocation() {
		return endLocation;
	}

	public void setEndLocation(String endLocation) {
		this.endLocation = endLocation;
	}

	public double getFuel() {
		return fuel;
	}

	public void setFuel(double fuel) {
		this.fuel = fuel;
	}

	public double getBattery() {
		return battery;
	}

	public void setBattery(double battery) {
		this.battery = battery;
	}
	
}
