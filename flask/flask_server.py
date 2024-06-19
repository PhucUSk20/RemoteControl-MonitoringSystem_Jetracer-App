#!/usr/bin/env python
from importlib import import_module
import os
from flask import Flask, render_template, Response
import cv2
import ipywidgets
import threading
from jetracer.nvidia_racecar import NvidiaRacecar
from jetcam.csi_camera import CSICamera
import traitlets
from IPython.display import display
from ipywidgets import Layout, Button, Box
import ipywidgets.widgets as widgets
from utils import preprocess
from jetcam.utils import bgr8_to_jpeg

camera = CSICamera(width=500, height=500, capture_fps=65)
camera.running = True

app = Flask(__name__)

# After gen is called infinite loop will take frame by frame from camera class,
# and yeild it to the client in jpeg data form
def gen():
    """Video streaming generator function."""
    while True:
        frame = camera.value
        if frame is not None:
            # Resize the frame to be twice as big
            frame = cv2.resize(frame, (frame.shape[1] * 2, frame.shape[0] * 2))

            # Encode the resized frame into JPEG format
            frame_bytes = cv2.imencode('.jpg', frame)[1].tobytes()
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')
        else:
            print("Error: Empty frame received.")

# When client is forwarded to /video_feed, it will call 'gen' function
# with imported camera class as a paremeter
@app.route('/video_feed')
def video_feed():
    """Video streaming route. Put this in the src attribute of an img tag."""
    return Response(gen(), mimetype='multipart/x-mixed-replace; boundary=frame')

# When client demands root, index.html will forward it to video feed
@app.route('/')
def index():
    """Seurveillance camera monitoring"""
    return render_template('index.html')

@app.route('/one')
def hello_one():
    return 'hello one'

@app.route('/image')
def image():
    frame = camera.value
    if frame is not None:
        frame_bytes = cv2.imencode('.jpg', frame)[1].tobytes()
        return Response(frame_bytes, mimetype='image/jpeg')
    else:
        # Handle case when the frame is empty
        return "Error: Empty frame received."

if __name__ == '__main__':
    app.run(host='0.0.0.0',port='5000', threaded=True, debug=False, use_reloader=False)



