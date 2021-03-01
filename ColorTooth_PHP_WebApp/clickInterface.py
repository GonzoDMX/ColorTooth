#!/usr/bin/env python

"""
	Andrew O'Shei - 20001943
	University Paris 8 L3 Informatique MIME
	Projet: ColorTooth - ESP32, ColorClick
	1 Mars 2021
"""


import serial
import time
import csv
import traceback
import datetime
import requests
import signal
import sys
import os.path
import json

port = '/dev/ttyUSB0'
baud = 115200

press = 0

global serCOM

def initSerial(ser):
	if(ser.isOpen() == False):
		ser.open()
	#message = "1"
	if ser.in_waiting == 0:
		ser.write("<CONNECT>".encode())
	response = ser.readline()
	print("Connect: " + str(response))
	if response == b'<CONNECT>\r\n':
		print("Serial Connected")
		return True
	return False

def termSerial():
	try:
		serCOM = serial.Serial(port, baud, timeout=3)
		#IMPORTANT must disable dtr or ESP32 resets or crashes each time		
		serCOM.rts = 0
		serCOM.dtr = 0
		if serCOM.in_waiting == 0:
			serCOM.write("<DISCONN>".encode())
			time.sleep(0.5)
		response = serCOM.readline()
		print("Disconnect: " + str(response))
		if response == b'<DISCONN>\r\n':
			print("Serial Disconnected")
		else:
			print("Hard Disconnect")
		if(serCOM.isOpen() == True):
			serCOM.close()
	except Exception:
		print("Error, serial device is no longer connected.")
	
	
#Convert Hex String to integer values
def convertHex(value):
	msb = ord(value[0]) - 48
	lsb = ord(value[1]) - 48
	if msb > 9:
		msb -= 7
	if lsb > 9:
		lsb -= 7
	return (msb * 16) + lsb
	
#Finds the Hue, Saturation, Value of argb input
def argb2hsv(a, r, g, b):
	v = 0.0
	a = a/255.0	
	r, g, b = (r/255.0)*a, (g/255.0)*a, (b/255.0)*a
	mx = max(r, g, b)
	mn = min(r, g, b)
	df = mx-mn
	if mx == mn:
		h = 0
	elif mx == r:
		h = (60 * ((g-b)/df) + 360) % 360
	elif mx == g:
		h = (60 * ((b-r)/df) + 120) % 360
	elif mx == b:
		h = (60 * ((r-g)/df) + 240) % 360
	if mx == 0:
		s = 0
	else:
		s = (df/mx)*100
		v = mx*100
	return h, s, v


#Find the name of the Color
def findColor(alpha, red, green, blue):
	colorName = "Black"
	hsv = argb2hsv(alpha, red, green, blue)
	#Check if Color is black
	if hsv[2] > 15.0:
		#Check if color is White
		if hsv[1] > 20.0:
			if hsv[0] >= 0.0 and hsv[0] <= 20.0:
				colorName = "Red"
			elif hsv[0] > 20.0 and hsv[0] <= 50.0:
				colorName = "Orange"
			elif hsv[0] > 50.0 and hsv[0] <= 70.0:
				colorName = "Yellow"
			elif hsv[0] > 70.0 and hsv[0] <= 155.0:
				colorName = "Green"
			elif hsv[0] > 155.0 and hsv[0] <= 195.0:
				colorName = "Cyan"
			elif hsv[0] > 195.0 and hsv[0] <= 225.0:
				colorName = "Blue"
			elif hsv[0] > 225.0 and hsv[0] <= 255.0:
				colorName = "Indigo"
			elif hsv[0] > 255.0 and hsv[0] <= 285.0:
				colorName = "Violet"
			elif hsv[0] > 285.0 and hsv[0] <= 340.0:
				colorName = "Magenta"
			elif hsv[0] > 340.0:
				colorName = "Red"
		else:
			colorName = "White"
	return colorName

	
def ProgramMain():	
	try:
		serCOM = serial.Serial(port, baud, timeout=3)
		serCOM.flushInput()
		print("Success " + serCOM.name + " is open for business")
		time.sleep(1)
		conn = initSerial(serCOM)
		while conn:
			while serCOM.in_waiting:
				try:
					#Get data from serial and convert to string
					line = serCOM.readline()
					line = line.decode('UTF-8','ignore')
					
					#Remove carriage return and parse values
					line = line.rstrip('\r\n')
					alpha, red, green, blue = line.split(',')

					#Convert values from HexString to Int
					a = convertHex(alpha)
					r = convertHex(red)
					g = convertHex(green)
					b = convertHex(blue)
					
					#Convert values from ARGB to name of the Color
					color_name = findColor(a, r, g, b)

					#Get Current Time value			
					time_obj = datetime.datetime.now().time()
					#Format Time as String
					time_stamp = time_obj.strftime("%H:%M:%S.%f")[:-4]
					
					#Create data object
					data_entry = [color_name, a, r, g, b, time_stamp]
					sql_entry = {'colorname':color_name, 'alpha_v':a, 'red_v':r, 'green_v':g, 'blue_v':b, 'time_s':time_stamp}
					#print(data_entry)
					
					write_csv(data_entry)
					push_database(sql_entry)

				except Exception:
					traceback.print_exc()
					print("exiting")
	except Exception:
		print("Error, no serial devices available!")
		sys.exit(1)


#Push data to database
def push_database(entry):
	r = requests.post("http://localhost/insertEntry.php", data=entry)
	#Show what the server responded with
	#print(r.text)	


#Append data to CSV file
def write_csv(data):
	file_exists = os.path.isfile("test_data.csv")
	header = ['ColorName', 'Clear', 'Red', 'Green', 'Blue', 'TimeStamp']
	with open("test_data.csv","a") as f:
		writer = csv.writer(f, delimiter=",", lineterminator='\n')
		if not file_exists:
			writer.writerow(header)
		writer.writerow(data)
		f.close()
	
def exit_gracefully(signum, frame):
	signal.signal(signal.SIGINT, original_sigint)
	termSerial()
	sys.exit(0)


def clear_table():
	r = requests.post("http://localhost/clearTable.php")
	#Show what the server responded with
	#print(r.text)


def clear_csv():
	if os.path.exists("test_data.csv"):
		print("Removing file")
		os.remove("test_data.csv")

def killhandle(signum, frame):
	''' This will close connections cleanly '''
	termSerial()
	line = "SIGTERM detected, shutting down"
	syslog.syslog(syslog.LOG_INFO, line)
	sys.exit(0)
    	

if __name__ == '__main__':
	original_sigint = signal.getsignal(signal.SIGINT)
	signal.signal(signal.SIGINT, exit_gracefully)
	signal.signal(signal.SIGTERM, killhandle)
	clear_csv()
	clear_table()
	ProgramMain()	



