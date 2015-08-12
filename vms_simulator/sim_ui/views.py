from django.shortcuts import render
from django.http import HttpResponse
from django.core import serializers
from django.views.decorators.csrf import csrf_exempt
import json
import requests

from .models import Trip,Vehicle,Driver,Event

# Create your views here.
def trips(request):
    uri='http://localhost:8000/media'
    trips = [ {'start_address':trip.start_address,'lat_lng':get_lat_lon(trip.start_address),'end_address':trip.end_address,\
               'driver_name':trip.driver.name,'driver_photo':uri+unicode(trip.driver.photo)[1:],'vehicle_photo':uri+unicode(trip.vehicle.photo)[1:],\
               'vehicle_number':trip.vehicle.number,'num_passengers':trip.num_passengers} \
              for trip in Trip.objects.filter(completed=False).select_related('driver').select_related('vehicle')]
    return HttpResponse(json.dumps(trips),content_type='application/json')

def get_lat_lon(address):
    geocode = requests.get('https://maps.googleapis.com/maps/api/geocode/json?address='+address)
    ret_val={'lat':0.0,'lng':0.0}
    if geocode.status_code == 200:
        geojson = geocode.json()
        if 'ZERO_RESULTS' <> geojson['status']:
            lat_lng = geojson['results'][0]['geometry']['location']
            ret_val = {'lat':lat_lng['lat'],'lng':lat_lng['lng']}
    return ret_val


def vehicles(request):
    return HttpResponse(serializers.serialize('json',Vehicle.objects.all()),content_type='application/json')

def drivers(request):
    return HttpResponse(serializers.serialize('json',Driver.objects.all()),content_type='application/json')

@csrf_exempt
def simulate(request):
    simulations = save_simulations(request.POST)
    start_simulation(simulations)
    return HttpResponse(status=201)

def save_simulations(simulation_data):
    num_trips = int(simulation_data['max_trips'])
    ret_val = []
    for i in [ repr(i) for i in range(num_trips)]:
        vehicle=Vehicle.objects.get(number=simulation_data['vehicle_number_'+i])
        driver=Driver.objects.get(name=simulation_data['driver_name_'+i])
        trip = Trip(start_address=simulation_data['start_address_'+i],end_address=simulation_data['end_address_'+i],\
                    vehicle=vehicle,\
                    driver=driver,num_passengers=1)
        trip.save()
        ret_val.append({'vin':vehicle.number,'startLocation':simulation_data['start_address_'+i],\
                        'endLocation':simulation_data['end_address_'+i],'fuel':simulation_data['fuel_'+i],'battery':simulation_data['battery_'+i],\
                        'driveSpeed':'SLOW' if driver.driving_type == '1' else 'MEDIUM' if driver.driving_type == '2' else 'FAST' })
    return ret_val


def start_simulation(simulations):
    print 'Starting Simulation: ',simulations
    ret_val = requests.post('http://172.25.235.146:8080/api/simulate/trip',data=json.dumps(simulations),headers={'Content-Type':'application/json'})
    print ret_val

@csrf_exempt
def event(request):
    ret_val = []
    if request.method == 'POST':
        evt_values = json.loads(request.body)
        trip = Trip.objects.get(vehicle__number=evt_values['vehicle_number'],completed=False)
        event = Event(evt_type=evt_values['event_type'],desc=evt_values['event_description'],lat=evt_values['lat'],lon=evt_values['lon'],\
                      vehicle=trip.vehicle,driver=trip.driver,\
                      status=evt_values['status'])
        event.save()
    elif request.method == 'GET':
        ret_val = [{'type':event.evt_type,'desc':event.desc,'vehicle_number':event.vehicle.number,'driver_name':event.driver.name,'lat':event.lat,'lon':event.lon}\
                   for event in Event.objects.all()]
    return HttpResponse(json.dumps(ret_val),content_type='application/json')

@csrf_exempt
def accident(request):
    ret_val = requests.patch('http://172.25.235.146:8080/api/simulate/accident',data=request.body,headers={'Content-Type':'application/json'})
    return HttpResponse(json.dumps({}),content_type='application/json')
