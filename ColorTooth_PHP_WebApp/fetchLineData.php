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
    $query = "SELECT * FROM (SELECT time_s, alpha_v, red_v, green_v, blue_v, id FROM ColorSerial ORDER BY id DESC LIMIT 100) AS ColorSerial ORDER BY id ASC";
    $result = $con->query($query);

	// Declare Root Array
	$table = array();
	// Declare Column Header
	$table['cols'] = array(
		array('label'=>'Time', 'type'=>'string'),
		array('label'=>'Alpha', 'type'=>'number'),
		array('label'=>'Red', 'type'=>'number'),
		array('label'=>'Green', 'type'=>'number'),
		array('label'=>'Blue', 'type'=>'number')
	);
	
	// Declare Data Array
	$rows = array();
	
	if(mysqli_num_rows($result)==0) {
		$temp_array = array();
		$temp_array[]=array('v'=>'');
		$temp_array[]=array('v'=>0);  
		$temp_array[]=array('v'=>0);
		$temp_array[]=array('v'=>0);  
		$temp_array[]=array('v'=>0);  
		$rows[] = array('c'=>$temp_array);
	} else {
		if(mysqli_num_rows($result)<100) {
			for($i = 1; $i <= 100 - mysqli_num_rows($result); $i++) {
				$temp_array = array();
				$temp_array[]=array('v'=>'');
				$temp_array[]=array('v'=>0);  
				$temp_array[]=array('v'=>0);
				$temp_array[]=array('v'=>0);  
				$temp_array[]=array('v'=>0);  
				$rows[] = array('c'=>$temp_array);
			}
		}
		// Retrieve and format Data from Database Query result
		while($row = mysqli_fetch_assoc($result)) {
			$temp_array = array();
			$temp_array[]=array('v'=>(string)$row['id']);
			$temp_array[]=array('v'=>(int)$row['alpha_v']);  
			$temp_array[]=array('v'=>(int)$row['red_v']);
			$temp_array[]=array('v'=>(int)$row['green_v']);  
			$temp_array[]=array('v'=>(int)$row['blue_v']);  
		  	$rows[] = array('c'=>$temp_array);
		}
	}
	// Write data to root table
	$table['rows']=$rows;
	// Encode Root table to JSON format and ship it
	echo json_encode($table);
	
?>  
