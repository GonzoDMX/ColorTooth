<?php

/*
*	Andrew O'Shei - 20001943
*	University Paris 8 L3 Informatique MIME
*	Projet: ColorTooth - ESP32, ColorClick
*	1 Mars 2021
*/
	
	//Set DB User Credentials
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

	$sql = "TRUNCATE TABLE ColorSerial";

	if ($conn->query($sql) === TRUE) {
		echo "MySQL: Success";
	} else {
		echo "Error: " . $sql . "<br>" . $conn->error;
	}

	$conn->close();

?>
