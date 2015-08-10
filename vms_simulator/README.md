Steps to run the UI

1. Install python 2.7.X and make sure that python.exe is part of the PATH variable.
2. Install pip for python
3. open command prompt
4. run the following commands:
 a) pip install tornado
 b) pip install django
 c) pip install pillow
 
5. go to vms_simulator directory
6. run the following commands:
 a) python manage.py makemigrations
 b) python manage.py migrate
7. To run the UI
 a) open cmd
 b) cd vms_simulator/sim_event_handler
 c) python sim_event_handler.py
 d) open cmd
 e) cd vms_simulator
 f) manage.py runserver