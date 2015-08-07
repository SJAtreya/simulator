package com.gto.iot.stream;

public enum DriveSpeed {

	SLOW(1), MEDIUM(2), FAST(3);
	
	private int value;
	
	DriveSpeed(int value) {
		this.setValue(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
