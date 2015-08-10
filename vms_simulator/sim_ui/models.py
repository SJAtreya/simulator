from django.db import models

# Create your models here.
class Vehicle(models.Model):
    number=models.CharField(max_length=20,verbose_name="Vehicle Number",unique=True)
    make_choices =(
        ("Maruti 800","Maruti 800"),
        ("Fiat Uno","Fiat Uno"),
        ("Maruti Wagon-R","Maruti Wagon-R"),
        ("Tata Indica","Tata Indica")
    )
    make=models.CharField(max_length=20,verbose_name="Make of the car",choices=make_choices)
    year_of_make=models.CharField(max_length=4,verbose_name="Car Model Year of Make")
    mileage=models.FloatField(verbose_name="Mileage",help_text="In kilometres per litre")
    distance_travelled=models.FloatField(verbose_name="Distance travelled",default=0.0)
    max_fuel_capacity=models.FloatField(verbose_name="Maximum Fuel Capacity",help_text="In litres")
    max_speed=models.IntegerField(verbose_name="Maximum Speed",help_text="To the nearest kmph")
    photo=models.ImageField(verbose_name='Photo',help_text='Upload a photo of the car',null=True,blank=True)
    current_fuel_reserve=models.FloatField(verbose_name="Current Fuel Reserve",help_text='In litres',null=True,blank=True,editable=False)

    def __unicode__(self):
        return self.number
    
class Driver(models.Model):
    name=models.CharField(max_length=200)
    age=models.IntegerField(help_text="To the nearest year")
    driving_experience=models.IntegerField(verbose_name='Years of experience', help_text="To the nearest year")
    driving_type_choices=(
        ('1','Cautious'),
        ('2','Safe'),
        ('3','Rash'),
    )
    driving_type=models.CharField(max_length=1,verbose_name="Driving Type",choices=driving_type_choices)
    photo=models.ImageField(verbose_name='Photo',help_text='Upload a photo of the driver',null=True,blank=True)

    def __unicode__(self):
        return self.name

class Trip(models.Model):
    start_address=models.TextField(max_length=2000,verbose_name='Start Address')
    end_address=models.TextField(max_length=2000,verbose_name='End Address')
    driver = models.ForeignKey(Driver)
    vehicle = models.ForeignKey(Vehicle)
    passenger_choices = tuple([(i,i) for i in range(8)])
    num_passengers = models.IntegerField(verbose_name='Number of Passengers',choices=passenger_choices)
    start_time = models.DateTimeField(verbose_name='Start Time',null=True,blank=True,editable=False)
    end_time = models.DateTimeField(verbose_name='End Time',null=True,blank=True,editable=False)
    distance_covered=models.FloatField(null=True,blank=True,editable=False)
    completed=models.BooleanField(default=False)

    def __unicode__(self):
        return self.driver.name +' ' + self.vehicle.number

class Event(models.Model):
    evt_type=models.CharField(max_length=100,verbose_name='Type')
    desc=models.CharField(max_length=100,verbose_name='Description')
    lat=models.FloatField(verbose_name='Latitude',blank=True,null=True)
    lon=models.FloatField(verbose_name='Longitude',blank=True,null=True)
    vehicle=models.ForeignKey(Vehicle,null=True,blank=True)
    driver=models.ForeignKey(Driver,null=True,blank=True)
    status=models.CharField(max_length=30)
    addl_data=models.TextField(max_length=2000)


    def __unicode__(self):
        return self.evt_type 
    
