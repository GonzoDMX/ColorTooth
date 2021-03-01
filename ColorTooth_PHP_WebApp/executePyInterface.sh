#!/bin/bash

#
#	Andrew O'Shei - 20001943
#	University Paris 8 L3 Informatique MIME
#	Projet: ColorTooth - ESP32, ColorClick
#	1 Mars 2021
#

# Check if clickInterface.py is running, if no launch it
if pgrep -f "clickInterface.py" > /dev/null
then
    echo "Running"
else
    echo "Stopped"
    python clickInterface.py
fi
