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
    $conn = new mysqli($servername, $username, $password, $dbname);

	// Check connection status
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }
    else {
        //echo "Connected!";
    }

	// Get Data from MySQL Database
	$query = "SELECT * FROM ColorSerial ORDER BY id DESC";
	$result = $conn->query($query);

	// Declare Root Array
	$table = array();
	// Declare Column Header
	$table['cols'] = array(
		array('type'=>'number', 'label'=>'ID'),
		array('label'=>'Color', 'type'=>'string'),
		array('label'=>'Alpha', 'type'=>'number'),
		array('label'=>'Red', 'type'=>'number'),
		array('label'=>'Green', 'type'=>'number'),
		array('label'=>'Blue', 'type'=>'number'),
		array('label'=>'Time', 'type'=>'string')
	);

	// Declare Data Array
	$rows = array();
	// Retrieve and format Data from Database Query result
	while($row = mysqli_fetch_assoc($result)) {
		$temp_array = array();
		$temp_array[]=array('v'=>(int)$row['id']);  
		$temp_array[]=array('v'=>(string)$row['colorname']);  
		$temp_array[]=array('v'=>(int)$row['alpha_v']);  
		$temp_array[]=array('v'=>(int)$row['red_v']);
		$temp_array[]=array('v'=>(int)$row['green_v']);  
		$temp_array[]=array('v'=>(int)$row['blue_v']);  
		$temp_array[]=array('v'=>(string)$row['time_s']);  
	  	$rows[] = array('c'=>$temp_array);
	}
	// Add empty rows to avoid table dynamically resizing on web page
	if(mysqli_num_rows($result)<21) {
		for($i = 1; $i <= 21 - mysqli_num_rows($result); $i++) {
			$temp_array = array();
			$temp_array[]=array('v'=>null);
			$temp_array[]=array('v'=>'');
			$temp_array[]=array('v'=>null);  
			$temp_array[]=array('v'=>null);
			$temp_array[]=array('v'=>null);  
			$temp_array[]=array('v'=>null);
			$temp_array[]=array('v'=>'');  
			$rows[] = array('c'=>$temp_array);
		}
	}
	// Write data to root table
	$table['rows']=$rows;
	// Encode Root table to JSON format and ship it
	echo json_encode($table);
	
	$conn->close();
	
?>  
