# Mind Controlled Robot

Arduino robot that is controlled via facial expressions obtained through an Emotiv Insight EEG headset.

- EEG readings are captured by the Emotiv Insight headset and broadcast to the phone via bluetooth.
- An Android app I created then uses the Emotiv SDK to interpret the EEG readings as Facial expressions.
- When certain facial expressions are detected a message is sent from the phone (Usb Host) to the Arduino Microcontroller (Usb Slave) using a simple ASCII-based communications protocol.
- After the Arduino has received a message it uses the motor shield to perform the corresponding movement.

Smile = Move Forward, Left-Eye Wink = Turn Left, Right-Eye Wink = Turn Right


Hardware:
- Emotiv Insight EEG headset
- Moto G 2nd Gen Android Phone
- USB On-The-Go cable
- USB A Male to USB-Micro B Male Cable
- Arduino UNO Microcontroller
- Adafruit Motor Shield for Arduino
- Smart Robot Car Chassis 2WD kit (Wheels, motors & base)


Project Components:

android-facial-recognition (Usb Host): Android app that used to recognise facial expressions, configure recognition thresholds and send instructions to the Arduino.

arduino-robot: Arduino Sketch that contains code for recognising instructions and controlling motors used to make the robot move.


https://www.youtube.com/watch?v=CaXOzD2TiRY

## License

Basically: just include attribution if you're using substantial portions of the code and dont sue me if something goes wrong :)

The MIT License (MIT)

Copyright (c) 2016 Aydan Bedingham

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.