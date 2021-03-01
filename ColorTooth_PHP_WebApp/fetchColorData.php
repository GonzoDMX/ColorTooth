<?php

/*
*	Andrew O'Shei - 20001943
*	University Paris 8 L3 Informatique MIME
*	Projet: ColorTooth - ESP32, ColorClick
*	1 Mars 2021
*/

	// Database User Credentials
	$servername = "localhost";
	$username = "colorclick";
	$password = "accessiseverything";
	$dbname = "Color_DB";

	// Establish Connection to MySQL Database
	$con = new mysqli($servername, $username, $password, $dbname);

	// Check connection status
	if ($con->connect_error) {
		die("Connection failed: " . $con->connect_error);
	}
	else
	{
		//echo ("Connect Successfully");
	}

	// Get Data from MySQL Database
	$query = "SELECT colorname, alpha_v, red_v, green_v, blue_v FROM ColorSerial ORDER BY id DESC LIMIT 1";
	$result = $con->query($query);

	$color = null;
	// Retrieve and format Data from Database Query result
	if(mysqli_num_rows($result) > 0) {
		$row = mysqli_fetch_assoc($result);
		$color=$row['colorname'];
		
		$alpha = $row['alpha_v'] / 255;
		$red = intval($row['red_v'] * $alpha);
		$green = intval($row['green_v'] * $alpha);
		$blue = intval($row['blue_v'] * $alpha);
		
		echo "rgb({$red},{$green},{$blue})";
	}

?>  
