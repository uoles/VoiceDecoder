#!/usr/bin/python3
import sys
import logging
import json
import threading

from pymodbus.constants import Endian
from pymodbus.payload import BinaryPayloadDecoder
from pymodbus.payload import BinaryPayloadBuilder
from pymodbus.client.serial import ModbusSerialClient as ModbusClient

from vosk import Model, KaldiRecognizer
import pyaudio

logging.basicConfig()
log = logging.getLogger()
log.setLevel(logging.INFO)

bad_words = []
infile = open('/home/forester/sowa/bad_words.txt','r')
for line in infile:
    bad_words.append(line.replace('\n', ''))
infile.close()

print('Загружаем справочник:')
print(bad_words)

# load Vosk library
model = Model(r"/home/forester/sowa/vosk-model-small-ru-0.22")
# model = Model(r"e:\Sourses\Python\sowa\vosk-model-small-ru-0.22")
recognizer = KaldiRecognizer(model, 16000)
mic = pyaudio.PyAudio()
stream = mic.open(format=pyaudio.paInt16, channels=1, rate=16000, input_device_index=1, input=True, frames_per_buffer=16000)
stream.start_stream()

# create connection (boot mode is 9600)
client = ModbusClient(method='rtu', port='/dev/ttyACM0', baudrate=230000, timeout=1.5)
client.connect()

idslave = 0x01
lastCommand = ''
isWingDown = True

def get_command():
    try:
        data = stream.read(4096)
        if len(data) != 0:
            inputStr = ""
            if recognizer.AcceptWaveform(data):
                inputStr = recognizer.Result()
            else:
                inputStr = recognizer.PartialResult()

            command = json.loads(inputStr)
            partial = command.get("partial")
            result = ""
            if (partial):
                result = command["partial"]
                
            return result
    except OSError:
        pass

def send_value(newValue):
    print('send new value: ' + str(newValue) + '\n')
    client.write_register(address=0x0000, value=newValue, slave=idslave)
    
def sowa_wing_up():
    send_value(100)
    isWingDown = False

def sowa_wing_down():
    send_value(0)
    isWingDown = True
    lastCommand = ''
    
def process_command(value):
    if value in bad_words:
        print('command: ' + str(value))
        sowa_wing_up()
        threading.Timer(5.0, sowa_wing_down).start()
    else:
        pass
        
def on_quit():
    print('\nПрограмма завершена')
    stream.stop_stream()
    stream.close()
    mic.terminate()
    sys.exit()

sowa_wing_down()        
while True:
    try:
        command = get_command()
        if command and len(command) != 0:
            print('\ninput: ' + command)
            if command != lastCommand and isWingDown:
                process_command(command)
                lastCommand = command
    except KeyboardInterrupt:   # ctrl+c
        on_quit()
        raise SystemExit
    
