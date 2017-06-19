<?php

function curl_get_content($url){
  $ch = curl_init();
  curl_setopt($ch, CURLOPT_URL, $url);
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
  $content = curl_exec($ch);
  curl_close($ch);
  return $content;
}

class M_time{
	public $name;  //영화이름
	public $code;	//영화코드
	public $time_num;//영화시간 개수
	public $time;	 //시간표(문자열 배열)
	public $reserv;	//빠른예매 url주소
}

function get_naver_time($url,$theater_no){
	//$url = "http://movie.naver.com/movie/bi/ti/running.nhn?code=15";
	$return = curl_get_content($url);

	//영화 이름($name,$code) 패턴
	$pattern = '/<a href="\/movie\/bi\/mi\/basic\.nhn\?code=(\d+)" onClick="[^"]+">(.*)<\/a>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	$theaterM_num = $num;
	echo $num."<br>";

	for($i=0;$i<$theaterM_num;$i++){
		$m_time[$i] = new M_time;	//객체생성
		$m_time[$i]->name = iconv("EUC-KR","UTF-8", $out[$i][2]);
		//$m_time[$i]->name = preg_replace("/\s+/", "", $m_time[$i]->name);
		$m_time[$i]->code = $out[$i][1];
	}
	//시간표 다 뽑아오는 필터
	$pattern = '/<a href="javascript:reservation\(\'([0-9]+)\', \'([0-9]+)\', \'([0-9]+)\', \'([0-9]+)\', \'([0-9]+)\', \'([0-9]+)\'[^"]+" [^>]+>(.*)<\/a>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);	//$num :해당 영화관의 시간표 총개수

	//시간표 클래스에 영화별로 저장하는 코드
	$m_c=0;//영화 카운트
	$t_c=0;//시간표 카운트
	$m_time[0]->time_num = 0;

	for($i=0;$i<$num;$i++){
		if($m_time[$m_c]->code == $out[$i][1]){
			$m_time[$m_c]->time[$t_c] = $out[$i][7];	//시간표 입력
			//*************************************************************************빠른 예매 rul 받아오기*****************************************************************************************************************************************************************************************************************
			$m_time[$m_c]->reserv[$t_c] = "movie.naver.com/movie/reservation/index.nhn?rmcode=".$out[$i][1]."_".$out[$i][2]."&gcode=".$out[$i][3]."&tcode=".$out[$i][4]."&date=".$out[$i][5]."&time=".$out[$i][6];
			$t_c++;					//시간표 카운트 증가
			$m_time[$m_c]->time_num = $t_c;	//영화별 시간표개수 저장
		}
		else{
			$m_c++;				//다음 영화로 바꿈
			$m_time[$m_c]->time_num = 0;
			$t_c = 0;	//시간표 카운트 초기화
			$i--;		//영화코드와 다른 시간표 다시 검사
		}
	}
	global $conn;

	$count=1;
	for($j=0;$j<$theaterM_num;$j++){
		echo $m_time[$j]->name."(".$m_time[$j]->time_num.")"."->";
		for($i=0;$i<$m_time[$j]->time_num;$i++){
			echo $m_time[$j]->time[$i]." | ";

			$time_no = "$theater_no"."$count";
			$sql = "INSERT INTO m_time(time_no,movie,theater_no,time,reservation_url)
			VALUES(\"$time_no\",\"{$m_time[$j]->name}\",$theater_no,\"{$m_time[$j]->time[$i]}\",\"{$m_time[$j]->reserv[$i]}\")";

			if($conn->query($sql) ===TRUE){
			  echo "New record created successfully"."<br>";
			} else {
			    echo "Error: " . $sql . "<br>" . $conn->error."<br>";
			}
			$count++;
		}
		echo "<br>";
	}
}

function get_cgv_time($url,$theater_no){
	//$url = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx?areacode=01&theatercode=0060";
	$return = curl_get_content($url);

	//영화이름 패턴(마지막 2개는 버림)
	$pattern = '/<strong>[^ ]+(\D*)<\/strong>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	$theaterM_num = $num-2;	//영화관에서 상영중인 영화 갯수

	for($i=0;$i<$theaterM_num;$i++){
		$m_time[$i] = new M_time;	//객체생성
		$m_time[$i]->name = trim($out[$i][1]);
	}

	//시간표 패턴
	$pattern = '/<a href="(\/ticket\/\?MOVIE_CD=[0-9]+&MOVIE_CD_GROUP=[0-9]+[^"]+)"[^>]+>[^>]+>(.*)<\/a>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);	//$num:해당영화관의 시간표 총개수

	echo $num."<br>";

	for($i=0;$i<$num;$i++){
		//영화 코드,상영 시간 추출 패턴
		$pattern = '/MOVIE_CD_GROUP=(\d+)&[^<]+<em>(.*)<\/em>/U';
		$n = preg_match($pattern,$out[$i][0],$f_out[$i]); //final_out
	}

	$m_c=0;//영화 카운트
	$t_c=0;//시간표 카운트
	$m_code= $f_out[0][1];	//영화 코드
	$m_time[0]->time_num = 0;

	for($i=0;$i<$num;$i++){
		if($m_code == $f_out[$i][1]){
			$m_time[$m_c]->time[$t_c] = $f_out[$i][2];	//시간표 입력
//*************************************************************************빠른 예매 rul 받아오기*******************************************************************
			$m_time[$m_c]->reserv[$t_c] = "www.cgv.co.kr".$out[$i][1];
			$t_c++;					//시간표 카운트 증가
			$m_time[$m_c]->time_num = $t_c;	//영화별 시간표개수 저장
		}
		else{
			$m_c++;				//다음 영화로 바꿈
			$m_code =$f_out[$i][1];		//영화 코드 바꿈
			$m_time[$m_c]->time_num = 0;	//영화별 시간표 개수 초기화
			$t_c = 0;	//시간표 카운트 초기화
			$i--;		//영화코드와 다른 시간표 다시 검사
		}
	}

	global $conn;
	$count=1;

	for($j=0;$j<$theaterM_num;$j++){
		echo $m_time[$j]->name."(".$m_time[$j]->time_num.")"."->";
		for($i=0;$i<$m_time[$j]->time_num;$i++){
			echo $m_time[$j]->time[$i]." | ";

			$time_no = "$theater_no"."$count";
			$sql = "INSERT INTO m_time(time_no, movie, theater_no, time, reservation_url)
			VALUES(\"$time_no\",\"{$m_time[$j]->name}\",$theater_no,\"{$m_time[$j]->time[$i]}\",\"{$m_time[$j]->reserv[$i]}\")";

			if($conn->query($sql) ===TRUE){
			  echo "New record created successfully"."<br>";
			} else {
			    echo "Error: " . $sql . "<br>" . $conn->error."<br>";
			}
			$count++;

		}
		echo "<br>";
	}
}

$conn= mysqli_connect('202.31.200.123', 'root', '133nplab', 'moona')
   or die('Error Connecting to MySQL server.');

   mysqli_query($conn, "set names utf8;");

   $query = "select theater_no, url,theater from m_theater";

   $result = mysqli_query($conn, $query)
         or die( $conn->error);

   for($i=0;$i<$result->num_rows;$i++){

	$row[$i] = $result->fetch_array(MYSQLI_NUM);
	echo $row[$i][0].")".$row[$i][1]."<br>";

	if($row[$i][0] < 2000){
		//echo "CGV parsing <br>";
		get_cgv_time($row[$i][1], $row[$i][0]);	//(영화관 url, 영화관 번호)
	}
	else
	 if($row[$i][0]>2000){
		//echo "lotte,mega parsing <br>";
		get_naver_time($row[$i][1], $row[$i][0]);
	}
    }

mysqli_close($conn);


?>
