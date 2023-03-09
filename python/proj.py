from vosk import Model, KaldiRecognizer
import os
import pyaudio

model = Model(r"\..\vosk-model-small-ru-0.22") # полный путь к модели
rec = KaldiRecognizer(model, 16000)
p = pyaudio.PyAudio()
stream = p.open(
    format=pyaudio.paInt16, 
    channels=1, 
    rate=16000, 
    input=True, 
    frames_per_buffer=16000
)
stream.start_stream()

while True:
    data = stream.read(4000)
    if len(data) == 0:
        break

    print(rec.Result() if rec.AcceptWaveform(data) else rec.PartialResult())

print(rec.FinalResult())