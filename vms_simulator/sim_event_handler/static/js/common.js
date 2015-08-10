
var maps = [];
var markers = [];
var vehicles = {};
var lastTimestamps = [];
var fleetMap = null;
var allTrips = [];

function fetchTripData(handler) {
	$.ajax({
	  dataType: "json",
	  url: '/trip',
	  complete: function(jqXhr,statusText) {
		var trips = eval('({"trips":'+jqXhr.responseText.replace("u'","")+'})');
		handler(trips);
	  }
	});
};

function getTitle(trip) {
	return trip.vehicle_number + ' ' + trip.driver_name;
};

function showFleet(trips) {
	var myHtml = fleetTemplate(trips);
	$('#mainContainer').html(myHtml);
	var myLatLng = new google.maps.LatLng(13.031506, 80.090164);
	var mapOptions = {
		center: myLatLng,
		zoom: 12,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	}
	maps = [];
	markers = [];
	fleetMap = new google.maps.Map(document.getElementById('fleetMap'), mapOptions);
	allTrips = trips.trips;
	lastTimestamps = [];
	for (var i = 0; i < trips.trips.length;++i) {
		var markerLatLng = new google.maps.LatLng(trips.trips[i].lat_lng.lat, trips.trips[i].lat_lng.lng);
		vehicles[trips.trips[i].vehicle_number]=i;
		var marker = new google.maps.Marker({
		  position: markerLatLng,
		  map: fleetMap,
		  icon: '/static/images/car.png',
		  title: getTitle(trips.trips[i])
		});
		markers.push(marker);
		lastTimestamps.push(new Date().getTime());
	}
};

function showTrips(trips) {
	var myHtml = tripTemplate(trips);
	$('#mainContainer').html(myHtml);
	
	var i=0;
	maps = [];
	markers = [];
	allTrips = trips.trips;
	lastTimestamps = [];
	$('.mapCanvas').each(function() {
		var myLatLng = new google.maps.LatLng(trips.trips[i].lat_lng.lat, trips.trips[i].lat_lng.lng);
		vehicles[trips.trips[i].vehicle_number]=i;
		var mapOptions = {
		  center: myLatLng,
		  zoom: 20,
		  mapTypeId: google.maps.MapTypeId.ROADMAP
		}
		

		var map = new google.maps.Map(document.getElementById($(this).attr('id')), mapOptions);
		var marker = new google.maps.Marker({
		  position: myLatLng,
		  map: map,
		  icon: '/static/images/car.png',
		  title: getTitle(trips.trips[i])
		});
		
		maps.push(map);
		markers.push(marker);
		lastTimestamps.push(new Date().getTime());
		++i;
	});

};

function showEvents(events) {
	var myHtml = eventTemplate(events);
	$('#mainContainer').html(myHtml);
};

function fetchEventData(handler) {
	$.ajax({
	  dataType: "json",
	  url: '/event',
	  complete: function(jqXhr,statusText) {
		var events = eval('({"events":'+jqXhr.responseText.replace("u'","")+'})');
		handler(events);
	  }
	});
};

function listenForUpdates() {
	var connection  = new WebSocket('ws://localhost:8800/updates');
	connection.onopen = function() {
		console.log('Socket opened');
	};
	connection.onclose = function() {
		console.log('Socket closed');
	};
	
	connection.onmessage = function(message) {
		console.log('Received message: ',message.data);
		var splitVals = message.data.split(',');
		if (splitVals[0]=='T') {
			// Time slice - Change lat long.
			var vehicleNumber = splitVals[1];
			try {
				var myLatLng = new google.maps.LatLng(parseFloat(splitVals[2]), parseFloat(splitVals[3]));
				var vehicleIndex = vehicles[vehicleNumber];
				markers[vehicleIndex].setPosition(myLatLng);
				var title = getTitle(allTrips[vehicleIndex]);
				markers[vehicleIndex].setTitle(title + '\n Distance Travelled: '+splitVals[splitVals.length-1] + ' KM');
				lastTimestamps[vehicleIndex] =  new Date().getTime();
				markers[vehicleIndex].setIcon('/static/images/car.png');
				if (window.location.hash == '#monitor/trip') {
					maps[vehicleIndex].setCenter(myLatLng);
				}
			}
			catch(obj) {
				console.log(obj);
			}
		}
		else if (splitVals[0]=='E') {
			$('#alertContainer').html('<div class="alert alert-danger alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button> Vehicle: ' + splitVals[1] +'.<b> ' + splitVals[8] 
			+' .</b> ' + splitVals[11]+'</div>');
		}
	};
};

function checkForNoUpdates() {
	var currMillis = new Date().getTime();
	console.log('Checking for no updates');
	for (var i = 0; i < lastTimestamps.length; ++i) {
		if (currMillis - lastTimestamps[i] >= 60000) {
			markers[i].setIcon('/static/images/red_car.jpg');
		}
	}
};

function createSimulationRows(uiValues) {
	var selectedtrip = $('#max_trips').val();
	var valuesToUse = {"trips":[]};
	for (var i=0; i < selectedtrip; ++i) {
		valuesToUse.trips.push(uiValues.trips[i]);
	}
	var htmlForSimulation = simulateRowTemplate(valuesToUse);
	$('#tripDetails').html(htmlForSimulation);
	
};

function setAddressAutocomplete () {
	$('.addressField').each(function() {
		var autocomplete = new google.maps.places.Autocomplete(
		/** @type {HTMLInputElement} */(document.getElementById($(this).attr('id'))),
		{ types: ['geocode'] });
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(function(position) {
			  var geolocation = new google.maps.LatLng(
				  position.coords.latitude, position.coords.longitude);
			  var circle = new google.maps.Circle({
				center: geolocation,
				radius: position.coords.accuracy
			  });
			  autocomplete.setBounds(circle.getBounds());
			});
		  }
	});
};

function validateSimulationStart() {
	var retVal = true;
	var numTrips = parseInt($('#max_trips').val());
	var fields = ["start_address","end_address","battery","fuel"];
	for (var i=0; i < numTrips;++i) {
		for (var j=0; j < fields.length;++j) {
			var fieldId = '#'+fields[j]+'_'+i;
			if ($(fieldId).val()=='') {
				retVal=false;
				$(fieldId).addClass('errorField');
			}
			else {
				$(fieldId).removeClass('errorField');
			}
		}
	}
	return retVal;
};

function startSimulation(vehicles,drivers) {
	var max_trips = vehicles.length > drivers.length ? drivers.length : vehicles.length;
	var uiValues = {max_trips:max_trips,"trips":[]};
	for (var i = 0; i < max_trips;++i) {
		uiValues.trips.push({"vehicle_number":vehicles[i].fields.number,"driver_name":drivers[i].fields.name,"fuel":vehicles[i].fields.max_fuel_capacity,"battery":100});
	}
	var htmlForSimulator = simulateTemplate(uiValues);
	$('#mainContainer').html(htmlForSimulator);
	
	$('#fillTrips').click(function() {
		createSimulationRows(uiValues);
		setAddressAutocomplete();
		$('#startSimulation').off('click').click(function() {
			if (validateSimulationStart()) {
				$.ajax({
					type:'POST',
					dataType: "json",
					data:$('#simulatorForm').serialize(),
					url: '/simulate',
					complete: function(jqXhr,statusText) {
						if (jqXhr.status == 200 || jqXhr.status == 201) {
							$('#alertContainer').html('<div class="alert alert-success alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button> Your simulation has been started.</div>');
						}
					}
				});
			}
			else {
				$('#alertContainer').html('<div class="alert alert-warning alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button> Please enter all required fields.</div>');
			}
		});
	});
};

function setupRoutes() {
	$('li').removeClass('active');
	if (location.hash == '#monitor/fleet') {
		fetchTripData(showFleet);
		$('#fleetMenu').addClass('active');
	}
	else if (location.hash == '#monitor/trip') {
		fetchTripData(showTrips);
		$('#tripMenu').addClass('active');
	}
	else if (location.hash == '#monitor/event') {
		fetchEventData(showEvents);
		$('#eventMenu').addClass('active');
	}
	else if (location.hash == '#simulate') {
		$('#simulateMenu').addClass('active');
		$.ajax({
		  dataType: "json",
		  url: '/vehicle',
		  complete: function(jqXhr,statusText) {
			var vehicles = eval('('+jqXhr.responseText.replace("u'","")+')');
			$.ajax({
			  dataType: "json",
			  url: '/driver',
			  complete: function(jqXhr,statusText) {
				var drivers = eval('('+jqXhr.responseText.replace("u'","")+')');
				startSimulation(vehicles,drivers);
			  }
			});
		  }
		});
		
	}
};

function showLoadingIcon() {
	$(document).ajaxSend(function() {
		$('#loadingIcon').show();
	});
	$(document).ajaxComplete(function() {
		$('#loadingIcon').hide();
	});
};

function setup() {
	window.onhashchange  = setupRoutes;
	window.location='#monitor';
	window.location='#monitor/fleet';
	listenForUpdates();
	showLoadingIcon();
	setInterval(checkForNoUpdates,60000);
};