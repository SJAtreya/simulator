"""vms_simulator URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.8/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Add an import:  from blog import urls as blog_urls
    2. Add a URL to urlpatterns:  url(r'^blog/', include(blog_urls))
"""
from django.conf.urls import include, url
from . import views

urlpatterns = [
    url(r'^trip/$', views.trips,name='trips'),
    url(r'^vehicle/$',views.vehicles,name='vehicles'),
    url(r'^driver/$',views.drivers,name='drivers'),
    url(r'^simulate/$',views.simulate,name='simulate'),
    url(r'^event/$',views.event,name='event'),
    url(r'^accident/$',views.accident,name='accident'),
]
