from django.contrib import admin

# Register your models here.
from .models import Vehicle,Driver,Trip,Event

class VehicleAdmin(admin.ModelAdmin):
    fieldsets = [
        ('Basic Details',{'fields': ['number','make','year_of_make','photo']}),
        ('Additional Details',{'fields':['mileage','distance_travelled','max_fuel_capacity','max_speed']})
    ]

class DriverAdmin(admin.ModelAdmin):
    pass


class TripAdmin(admin.ModelAdmin):
    fieldsets = [
        ('Source & Destination',{'fields': ['start_address','end_address']}),
        ('Vehicle,Driver & Other Details',{'fields':['driver','vehicle','num_passengers']}),
        ('Status',{'fields':['completed']})
    ]

class EventAdmin(admin.ModelAdmin):
    pass

admin.site.register(Vehicle,VehicleAdmin)
admin.site.register(Driver,DriverAdmin)
admin.site.register(Trip,TripAdmin)
admin.site.register(Event,EventAdmin)

