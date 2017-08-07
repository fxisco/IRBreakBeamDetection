# IR Break Beam Detection

This projects demostrates the functionality of I.R. break beams.
It uses Android things library to detect and open peripherals.

## Devices used

- I.R. Break Beam Sensor - 3mm version from Adafruit
- Raspberry Pi 3
- Solderless breadboard
- Male Female jumper wires

## Description

Infrared (IR) break-beam sensors are a simple way to detect motion. They work by having an emitter side that sends out a beam of human-invisible IR light, then a receiver across the way which is sensitive to that same light. When something passes between the two, and its not transparent to IR, then the 'beam is broken' and the receiver will let you know.

The 3mm IR version works up to 25cm / 10". You can power it from 3.3V or 5V, but 5V will get you better range.

## Prerequisites

1. Feed the solderless breadboard with the 3.3v or 5v output from the Raspberry Pi.
2. Power the transmitter. Connect the black wire to ground and the red wire directly to 3.3V or 5V power.
3. Connect the receiver Connect the black wire to ground, the red wire to 3.3V or 5V.
4. Connect the receiver white wire to the digital input(GPIO's on Raspberry Pi).

**IMPORTANT**

The receiver is open collector which means that you do need a pull up resistor. 

Raspberry Pi 3 has internal pull-up (3.3v) resistor configured by default on the following pins:
- BCM4
- BCM5
- BCM6

In this project we used the ***BCM4*** to avoid using a resistor.