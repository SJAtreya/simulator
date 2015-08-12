import socket
import tornado
import random
import os


from tornado import gen
from tornado.ioloop import IOLoop
from tornado import iostream
from tornado.tcpserver import TCPServer
from tornado.httpclient import AsyncHTTPClient
import json
import web_handlers


clients = []
    

class SimpleTcpReader(object):
    client_id = 0
 
    def __init__(self, stream):
        SimpleTcpReader.client_id += 1
        self.id = SimpleTcpReader.client_id
        self.stream = stream
        self.http_client = AsyncHTTPClient()
        self.stream.socket.setsockopt(
            socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
        self.stream.set_close_callback(self.on_disconnect)
 
 
    @gen.coroutine
    def on_disconnect(self):
        self.log("disconnected")
        yield []
 
    @gen.coroutine
    def dispatch_client(self):
        while True:
            line = yield self.stream.read_until(b'\n')
            decoded_line = line.decode('utf-8').strip()
            self.log('got |%s|' % decoded_line)
            for client in clients:
                client.send_update(decoded_line)
            print decoded_line[0]
            if decoded_line[0] == 'E':
                event_params = decoded_line.split(',')
                json_val = json.dumps({'event_type':event_params[9],'event_description':event_params[8],'vehicle_number':event_params[1],\
                                                   'lat':event_params[2],'lon':event_params[3],'status':event_params[10]})
                print json_val
                yield self.http_client.fetch('http://localhost:8000/simulator/event/',self.handle_request,method='POST',headers={'Content-Type':'application/json'},\
                                             body=json_val)
 
    @gen.coroutine
    def on_connect(self):
        raddr = 'closed'
        try:
            raddr = '%s:%d' % self.stream.socket.getpeername()
        except Exception:
            pass
        self.log('new, %s' % raddr)
 
        yield self.dispatch_client()
 
    def log(self, msg, *args, **kwargs):
        print('[connection %d] %s' % (self.id, msg.format(*args, **kwargs)))

        

    def handle_request(self,response):
        pass
 
 
class EventHandler(TCPServer):
 
    @gen.coroutine
    def handle_stream(self, stream, address):
        """
        Called for each new connection, stream.socket is
        a reference to socket object
        """
        connection = SimpleTcpReader(stream)
        yield connection.on_connect()


def main():
    server = EventHandler()
    server.listen(8889)
    app = tornado.web.Application(
            [ (r"/", web_handlers.MainHandler),
              (r"/trip", web_handlers.TripFetchHandler),
              (r"/vehicle", web_handlers.VehicleFetchHandler),
              (r"/driver", web_handlers.DriverFetchHandler),
              (r"/updates", web_handlers.UpdateHandler),
              (r"/simulate",web_handlers.SimulationSaveHandler),
              (r"/event", web_handlers.EventFetchHandler),
              (r"/simulate/event", web_handlers.SimulateEventHandler),
              
              ],
            cookie_secret="cook_{}".format(random.randint(1,1000000)),
            login_url="/auth/login",
            template_path=os.path.join(os.path.dirname(__file__), "templates"),
            static_path=os.path.join(os.path.dirname(__file__), "static"),
            xsrf_cookies=False,
            debug=True,
            users=dict(),
            client_users=dict(),
            clients=clients
        )
    app.listen(8800)
    print 'Listening at ports:',8800,8889
    IOLoop.instance().start()

if __name__ == "__main__":
    main()
