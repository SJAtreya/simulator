package com.gto.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.gto.iot.simulator.TimeseriesSimulator;
import com.gto.iot.stream.TripData;

@SpringBootApplication
public class SimulatorApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
        //new SimulatorApplication().testOnce();
    }

	private void testOnce() {
		// TODO Auto-generated method stub
		TripData data = new TripData();
		data.setVin("TN07BX4393");
		data.setBattery(10.5);
		data.setStartLocation("DLF IT PARK CHENNAI");
		data.setEndLocation("THORAIPPAKKAM CHENNAI");
		data.setFuel(15);
		new TimeseriesSimulator(data).run();;
	}
}
