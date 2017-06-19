<?php

$dbc= mysqli_connect('localhost', 'root', '133nplab', 'moona')
   or die('Error Connecting to MySQL server.');

   mysqli_query($dbc, "set names utf8;");

   $ID = $_GET['no'];
   $query = "select story,director,actor from m_cur_infor where no='$ID'";

   $result = mysqli_query($dbc, $query)
         or die('Error Querying database.');

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
