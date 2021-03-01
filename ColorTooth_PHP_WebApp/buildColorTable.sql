
/*
*	Andrew O'Shei - 20001943
*	University Paris 8 L3 Informatique MIME
*	Projet: ColorTooth - ESP32, ColorClick
*	1 Mars 2021
*/


CREATE TABLE IF NOT EXISTS ColorSerial(
  id INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  colorname CHAR(8),
  alpha_v TINYINT UNSIGNED,
  red_v TINYINT UNSIGNED,
  green_v TINYINT UNSIGNED,
  blue_v TINYINT UNSIGNED,
  time_s CHAR(11));

