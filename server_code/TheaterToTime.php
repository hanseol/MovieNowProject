<?php
   $dbc= mysqli_connect('localhost', 'root', '133nplab', 'moona')
      or die($dbc->error);

   mysqli_query($dbc, "set names euckr;");
  //사용자 입력받는부분

  // mysqli_query($dbc, "set session character_set_connection=utf8;");
  // mysqli_query($dbc, "set session character_set_results=utf8;");
  // mysqli_query($dbc, "set session character_set_client=utf8;");
$myfile = fopen("newfile.txt", "w") or die("Unable to open file!");
   $ADDR =$_GET['addr'];
   $NAME =$_GET['name'];

   fwrite($myfile, '처음 addr : '.$ADDR);
   fwrite($myfile,'처음 name : '. $NAME);
   if(mb_detect_encoding($ADDR) != "UTF-8") {
     $ADDR = iconv("EUC-KR", "UTF-8", $ADDR);
    }
    if(mb_detect_encoding($NAME) != "UTF-8") {
      $NAME = iconv("EUC-KR", "UTF-8", $NAME);
     }
     if(mb_detect_encoding($ADDR) != "UTF-8") {
       $NAME = iconv("EUC-KR", "UTF-8", $NAME);
      }

   $addr = iconv("UTF-8","EUC-KR",$ADDR);
   $name = iconv("UTF-8","EUC-KR",$NAME);

   fwrite($myfile, '나중 addr : '.$addr);
fwrite($myfile, '나중 name : '.$name);

   fclose($myfile);

 //$addr = iconv("UTF-8","EUC-KR",'서울 강동구 성내동 549-1'); //cgv
//  $addr = iconv("UTF-8","EUC-KR",'서울 광진구 자양동 227-7');
//  $name = iconv("UTF-8","EUC-KR",'미이라');

     $query = "SELECT theater_no FROM m_theater WHERE addr='$addr'";
     $theater= mysqli_query($dbc, $query)
         or die($dbc->error);


  for($i=0;$i<$theater->num_rows;$i++){

     $row[$i] = $theater->fetch_array(MYSQLI_NUM);
   }
   $temp=$row[0][0];
   //echo iconv("EUC-KR", "UTF-8", $row[0][1]);


   $now_time = date("H:i");
     $query2 = "SELECT time,reservation_url ,theater_no FROM m_time WHERE theater_no='$temp' AND movie='$name' AND time >'$now_time'";
     //여기부터 안댐댐
     $result = mysqli_query($dbc, $query2)
          or die($dbc->error);

   $json= array();

   if(mysqli_num_rows($result)){
      while($row=mysqli_fetch_assoc($result)){
        //$json['theater']=$theater_name;
        //print_r($row);
         $json['list'][]= $row;
      }
      mysqli_free_result($result);
   }
   echo json_encode($json,JSON_UNESCAPED_UNICODE);

    mysqli_close($dbc);
?>
