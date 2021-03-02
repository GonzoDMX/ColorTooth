<?php

/*
*	Andrew O'Shei - 20001943
*	University Paris 8 L3 Informatique MIME
*	Projet: ColorTooth - ESP32, ColorClick
*	1 Mars 2021
*/

	//Recover Arguments passed from Python Interface
	$c_name = $_POST['colorname'];
	$alpha = $_POST['alpha_v'];
	$red = $_POST['red_v'];
	$green = $_POST['green_v'];
	$blue = $_POST['blue_v'];
	$time_s = $_POST['time_s'];


	// Set DB Credentials
	$servername = "localhost";
	$username = "colorclick";
	$password = "accessiseverything";
	$dbname = "Color_DB";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	
		// Check connection status
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }
    else {
        echo "Connected!";
    }


	// MySQL Query
	$sql = "INSERT INTO ColorSerial
				(colorname, alpha_v, red_v, green_v, blue_v, time_s)
			VALUES
				('$c_name', $alpha, $red, $green, $blue, '$time_s')";

	if ($conn->query($sql) === TRUE) {
		echo "MySQL success";
	} else {
		echo "Error: " . $sql . "<br>" . $conn->error;
	}

	$conn->close();

?>
