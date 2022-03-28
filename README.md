# ColorTooth
ColorTooh is a demonstration application including a full pipeline for transferring and displaying data collected from multiple low-power wireless sensor devices. The wireless sensor devices include the TCS3471 color sensor connected to an ESP32 dev board and the HC-SR04 ultrasound sensor connected to an EasyPIC v7. Data collected by each is displayed on their integrated 7-Segment displays. The data is also transmitted via bluetooth to an Android app, which logs the data to a SQLite database and displays a plot of the data over time as it is logged. Data is also simultaneously transmitted via usb-serial to a PC where it is also stored to a MySQL database. A PHP-based werb application is then used to plot data dynamically as it is added to the database.

<p align="center">
  <img style="width: 80%; height: 80%;" src="https://github.com/GonzoDMX/ColorTooth/blob/main/assets/BlockDiagram_OSHEI.jpg">
</p>

<p align="center">
  <img style="width: 80%; height: 80%;" src="https://github.com/GonzoDMX/ColorTooth/blob/main/assets/schematic.png">
</p>
