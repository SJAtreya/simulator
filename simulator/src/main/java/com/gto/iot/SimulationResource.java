package com.gto.iot;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gto.iot.simulator.PooledTimeseriesSimulator;
import com.gto.iot.stream.TripData;

@RestController
public class SimulationResource {

	@RequestMapping(value="/api/simulate/trip",method=RequestMethod.POST)
	public ResponseEntity<HttpStatus> simulate(@RequestBody List<TripData> tripList) {
		PooledTimeseriesSimulator.runInPool(tripList);
		return new ResponseEntity<HttpStatus>(HttpStatus.OK); 
	}
	
	@RequestMapping(value="/api/simulate/accident/{vin}", method=RequestMethod.PATCH)
	public void causeAccident(@PathVariable String vin) {
		PooledTimeseriesSimulator.invokeAccident(vin);
	}
}
