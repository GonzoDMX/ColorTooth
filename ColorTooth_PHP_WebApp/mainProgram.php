<?php

/*
*	Andrew O'Shei - 20001943
*	University Paris 8 L3 Informatique MIME
*	Projet: ColorTooth - ESP32, ColorClick
*	1 Mars 2021
*/

	if(isset($_POST['start_action'])) {
		$command = escapeshellcmd('./executePyInterface.sh');
		shell_exec($command);
	}
	
	if(isset($_POST['stop_action'])) {
		$command = escapeshellcmd('pkill -f ClickInterface.py');
		shell_exec($command);
	}

?>

<!DOCTYPE html>
<html>
<head>
    <title>Color Click</title>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script type="text/javascript">
    
    
    	var noDataJSON = {
			cols: [
				{label: 'Time', type: 'string'},
				{label: 'Alpha', type: 'number'},
				{label: 'Red', type: 'number'},
				{label: 'Green', type: 'number'},
				{label: 'Blue', type: 'number'}
			],
    		rows: [
    			{c:[{v:"0"},{v:0}, {v:0}, {v:0}, {v:0}]}
    		]
    	};
    
    	//BUILD THE LINE PLOT HERE
		google.charts.load('current', {packages:['line']
		}).then(function() {
			getLineData();
		});
        google.charts.setOnLoadCallback(drawChart);
        
        var lineChart = null;
        var lineData;
        var lineOptions = {
			chart: {
				title: 'Color Click Sensor Data',
				subtitle: 'Awww yeah!'
			},
			width: 1250,
			height: 500,
			vAxis: { 
              title: "Value", 
              viewWindowMode:'explicit',
              viewWindow:{
                max:255,
                min:0
            	}
            },
			series: {
				0: {color: '#000000' },
				1: {color: '#E21E43' },
				2: {color: '#36B518' },
				3: {color: '#1E43E2' },
			}
		};
        
		function getLineData() {
			$.ajax({
				type: 'POST',
				url: "http://localhost/fetchLineData.php",
				dataType:"json",
				success: lineFeedback
			}).responseText;
		}

		function lineFeedback(result){
			drawChart(result);
		}

        
        function drawChart(result){

			lineData = new google.visualization.DataTable(result);
			if(lineChart == null) {
            	lineChart = new google.charts.Line(document.getElementById('linechart'));
            }
            lineChart.draw(lineData, google.charts.Line.convertOptions(lineOptions));
        }

	//BUILD THE TABLE HERE
	google.charts.load('current', {packages:['table']
	}).then(function() {
		getTableData();
	});
	google.charts.setOnLoadCallback(drawTable);

	var table;
	var tableData;
	var tableOptions = {
		showRowNumber: false,
		width: 600,
		height: 425,
	}
	
	function getTableData() {
		$.ajax({
			type: 'POST',
        	url: "http://localhost/fetchTableData.php",
        	dataType:"json",
        	success: tableFeedback
        }).responseText;
	}

	function tableFeedback(result){
		drawTable(result);
	}

	function drawTable(result) {
	
		tableData = new google.visualization.DataTable(result);
		table = new google.visualization.Table(document.getElementById('table_div'));
		table.draw(tableData, tableOptions);
	}
	
	
	var tableUpdate = null;
	var lineUpdate = null;
	var colorUpdate = null;
	function startAction(){
		tableUpdate = setInterval(getTableData, 250);
		lineUpdate = setInterval(getLineData, 250);
		colorUpdate = setInterval(getColorData, 250);
		$.ajax({
		    url: 'http://localhost/mainProgram.php',
		    type: 'post',
		    data: { "start_action": "1"},
		    success: function(response) { console.log(response); }
		});
		return false;
    }
    
    function stopAction(){
    	clearInterval(tableUpdate);
    	clearInterval(lineUpdate);
    	clearInterval(colorUpdate);
		$.ajax({
		    url: 'http://localhost/mainProgram.php',
		    type: 'post',
		    data: { "stop_action": "1"},
		    success: function(response) { console.log(response); }
		});
		return false;
    }
    
    function getColorData() {
		$.ajax({
			type: 'POST',
        	url: "http://localhost/fetchColorData.php",
        	success: colorFeedback
        }).responseText;
	}
    
    function colorFeedback(response) {
    	document.getElementById('curr-color').style.backgroundColor = response;
    }
	
	
    </script>
    <style>
		.google-visualization-table-td {
			text-align: center !important;
		}
		#parent {
			display: flex;
		}
		p {
			color: gray;
			font-family: 'Roboto', sans;
			font-size: 150%;
		}
		#sub_parent {
			border: 3px solid grey;
			padding: 10px;
		}
		#linechart {
			border: 3px solid grey;
			padding: 10px;
		}
		#button-holder {
			display: flex;
  			justify-content: center;
			border: 3px solid grey;
			padding: 10px;
			width: 1850;
		}
		input[type=submit] {
			padding:50px 150px;
			background:#ccc;
			border:0 none;
			cursor:pointer;
			border-radius: 15px;
			margin-left :5px 
		}
		.box {
			height: 200px;
			width: 500px;
			margin-bottom: 15px;
			border: 1px solid black;
		}

		.current-color {
			background-color: black;
		}
		.input-color {
			display: flex;
			justify-content: center;
			border: 3px solid grey;
			padding: 10px;
			text-align:center;
		}
    </style>
</head>
<body>
	<div id="parent">
		<div id="sub_parent">
			<p>Database Table</p>
			<div id="table_div"></div>
		</div>
		<div id="linechart"></div>
	</div>
	<div id="buttoncontainer">
		<div id="button-holder">
			<form onsubmit="return startAction()"> 
				<input type="submit" name="start-action" value="Start"/>
			</form>
			<form onsubmit="return stopAction()">
				<input type="submit" name="stop-action" value="Stop"/>
			</form>
		</div>
	</div>
	<div id="colorcontainer">
		<div class="input-color">
			<div class='box current-color' id="curr-color" ></div>
		</div>
	</div>
</body>
</html>
