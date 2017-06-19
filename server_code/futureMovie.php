<?php

$dbc= mysqli_connect('localhost', 'root', '133nplab', 'moona')
	or die('Error Connecting to MySQL server.');

	mysqli_query($dbc, "set names utf8;");

	$query = "select * from m_exp_infor order by r_date asc";


	$result = mysqli_query($dbc, $query)
			or die( $dbc->error);

	$json= array();

	if(mysqli_num_rows($result)){
		while($row=mysqli_fetch_assoc($result)){
			$json['list'][]= $row;
		}
		mysqli_free_result($result);
	}

	echo json_encode($json);

	mysqli_close($dbc);

?>
