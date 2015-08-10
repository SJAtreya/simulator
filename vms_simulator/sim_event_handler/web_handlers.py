import tornado.web
from tornado.escape import json_encode,json_decode
import tornado.websocket
from tornado.httpclient import AsyncHTTPClient
import time
import json
import os
from tornado import gen
from tornado import template


class BaseHandler(tornado.web.RequestHandler):
    def __init__(self, application, request, **kwargs):
        super(BaseHandler, self).__init__(application, request, **kwargs)
        self.http_client = AsyncHTTPClient()
        
    def get_current_user(self):
        user_json = self.get_secure_cookie("user")
        if not user_json: return None
        return json_decode(user_json)


class MainHandler(BaseHandler):
    def get(self):
        self.render('index.html')


class AsyncRequestHandler(BaseHandler):
    def handleRequest(self,response):
        self.write(response.body)

class TripFetchHandler(AsyncRequestHandler):
    @gen.coroutine
    def get(self):
        yield self.http_client.fetch('http://localhost:8000/simulator/trip/',self.handleRequest)


class EventFetchHandler(AsyncRequestHandler):
    @gen.coroutine
    def get(self):
        yield self.http_client.fetch('http://localhost:8000/simulator/event/',self.handleRequest)    

class VehicleFetchHandler(AsyncRequestHandler):
    @gen.coroutine
    def get(self):
        yield self.http_client.fetch('http://localhost:8000/simulator/vehicle/',self.handleRequest)

class DriverFetchHandler(AsyncRequestHandler):
    @gen.coroutine
    def get(self):
        yield self.http_client.fetch('http://localhost:8000/simulator/driver/',self.handleRequest)


class UpdateHandler(tornado.websocket.WebSocketHandler):
    def open(self, *args):
        clients = self.settings['clients']
        clients.append(self)

    def on_close(self):
        clients = self.settings['clients']
  	clients.remove(self)

    def send_update(self,message):
        self.write_message(message)

class SimulationSaveHandler(BaseHandler):
    @gen.coroutine
    def post(self):
        yield self.http_client.fetch('http://localhost:8000/simulator/simulate/',self.handleRequest,method='POST',headers=None,body=self.request.body)

    def handleRequest(self,response):
        self.write(response.body)


