<?php

function curl_get_content($url){
  $ch = curl_init();
  curl_setopt($ch, CURLOPT_URL, $url);
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
  $content = curl_exec($ch);
  curl_close($ch);
  return $content;
}

function get_movie_list($M_url,$curexp){
	//$M_url = "http://movie.naver.com/movie/running/current.nhn";	//영화 리스트 주소 매개변수로 바꿈

	$return = curl_get_content($M_url);	//페이지 전체 받아오기

	//영화 이름, 상세정보url 추출 패턴
	$pattern = '/<a href="(\/movie\/bi\/mi\/basic\.nhn\?code=\d+)">(.*)<\/a>/U';
	//out[][0]=태그 전체, [1]=영화이름, [2]=주소(http://movie.naver.com 붙이기)
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	$M_num = ($num-30)/2;	//영화 개수

	echo $M_num."<br>";
	//0~29까지는 상단의 30개 영화 리스트라서 제외
	for($i=0;$i<($M_num);$i++){	//영화 개수 만큼 상세정보 url추출
		$infor_url[$i]="http://movie.naver.com".$out[$i*2+30][1];

		//영화 상세정보 추출하는 함수 가 올 자리^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		fill_movie_infor($infor_url[$i],$i,$curexp);
		echo $i.")".$infor_url[$i]."<br>";
	}
}

function fill_movie_infor($infor_url,$no,$curexp){
	//영화 별 url 주소
	//$infor_url = "http://movie.naver.com/movie/bi/mi/basic.nhn?code=120160";

	$return = curl_get_content($infor_url);
	//echo $return;

	//영화명($m_name) 필터
	$pattern = '/<a href="\.\/basic\.nhn\?code=\d+">(.*)<\/a>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	$m_name = $out[0][1];
	echo $m_name."<br>";

	//썸네일($thumbnail) 필터
	$pattern = '/false;\"\>\<img src=\"(http:\/\/movie\d?\.phinf\.naver\.net\/[^\?]+)\?/';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	$thumbnail = $out[0][1];
	echo $thumbnail."<br>";

	//관람객($grade), 네티즌 평점 필터
	$pattern = '/<em class="num\d">(.*)<\/em>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	$grade = $out[0][1]+ ($out[1][1]*0.1)+ ($out[2][1]*0.01);
	$netizen = $out[3][1]+ ($out[4][1]*0.1)+ ($out[5][1]*0.01);

	echo $grade."<br>";
	echo $netizen."<br>";

	//평점 참여자 수($audience_num) 필터
	//$pattern = '/<div class="ly_count" id="actualPointCountBasic" style="display:none">(.*)<\/div>/U';
	//$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	//echo $num."??";
	//$audience_num = $out[0][2];
	//echo $audience_num."<br>";

	//장르($genre) 필터
	$pattern = '/<a href="\/movie\/sdb\/browsing\/bmovie\.nhn\?genre=\d+">(.*)<\/a>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);
	//num/2:장르갯수 , out[][1]:장르

	$genre_num = $num/2;

	for($i=0;$i<($genre_num);$i++){
		$genre .= $out[$i][1];
		if($i<($genre_num-1)){
			$genre .=",";
		}
	}
	echo $genre."<br>";

	//런타임($runtime) 필터
	$pattern = '/<span>(.*) <\/span>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);
	//out[0][1]:런타임
	$runtime = $out[0][1];
	echo $runtime."<br>";

	//개봉일($r_date) 필터
	$pattern = '/<a href="\/movie\/sdb\/browsing\/bmovie\.nhn\?open=(\d{8})">(.*)<\/a>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	//////20170101->2017.01.01로
	$r_date = $out[0][1][0].$out[0][1][1].$out[0][1][2].$out[0][1][3]."-".$out[0][1][4].$out[0][1][5]."-".$out[0][1][6].$out[0][1][7];
	echo $r_date."<br>";

	//감독($director) 및 배우($actor) 필터
	$pattern = '/<p>(<a href="\/movie\/bi\/pi\/basic\.nhn\?code=\d+">(.*)<\/a>.*)<\/p>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	//감독들
	$pattern = '/<a href="\/movie\/bi\/pi\/basic\.nhn\?code=\d+">(.*)<\/a>/U';
	$director_num = preg_match_all($pattern,$out[0][1],$out1,PREG_SET_ORDER);

	for($i=0;$i<$director_num;$i++){
		$director .= $out1[$i][1];
		if($i<($director_num-1)){
			$director .=",";
		}
	}
	echo $director."<br>";
	//배우들
	$pattern = '/<a href="\/movie\/bi\/pi\/basic\.nhn\?code=\d+">(.*)<\/a>/U';
	$actor_num = preg_match_all($pattern,$out[1][1],$out2,PREG_SET_ORDER);

	for($i=0;$i<$actor_num;$i++){
		$actor .= $out2[$i][1];
		if($i<($actor_num-1)){
			$actor .=",";
		}
	}
	echo $actor."<br>";

	//관람가($rating) 필터
	$pattern = '/<a href="\/movie\/sdb\/browsing\/bmovie\.nhn\?grade=\d+">(.*)<\/a>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	$rating = $out[0][1];

	echo $rating."<br>";

	//줄거리($story) 필터
	$pattern = '/<p class="con_tx">(.*)<\/p>/U';
	$num = preg_match_all($pattern,$return,$out,PREG_SET_ORDER);

	$story = $out[0][1];
	$story = preg_replace('/\"|<br>\&nbsp;/',"",$story);
	echo $story."<br>";

	//////////////데이터베이스 연결하는 구문////////////////////
	global $sql;
	global $conn;

	if($curexp=="cur"){	//현재상영작

		$sql = "INSERT INTO m_cur_infor(no,name, thumbnail, grade, genre, run_time, r_date, director, actor, rating, story)
		VALUES($no,\"$m_name\",\"$thumbnail\",\"$grade\",\"$genre\",\"$runtime\",\"$r_date\",\"$director\",\"$actor\",\"$rating\",\"$story\")";

		if($conn->query($sql) ===TRUE){
		  echo "New record created successfully";
		} else {
		    echo "Error: " . $sql . "<br>" . $conn->error;
		}
	}
	 if($curexp=="exp"){	//개봉예정작
		$sql = "INSERT INTO m_exp_infor(no,name, thumbnail, genre, run_time, r_date, director, actor, rating, story)
		VALUES($no,\"$m_name\",\"$thumbnail\",\"$genre\",\"$runtime\",\"$r_date\",\"$director\",\"$actor\",\"$rating\",\"$story\")";

		if($conn->query($sql) ===TRUE){
		  echo "New record created successfully";
		} else {
		    echo "Error: " . $sql . "<br>" . $conn->error;
		}
	}


}

$hostname = "202.31.200.123"; //아아피 혹은 도메인이름
$username= "root";   //아이디 (root)
$password = "133nplab"; //root 비번
$dbname = "moona";   //데이터베이스 이름 중 하나
$conn = new mysqli($hostname, $username, $password, $dbname);

mysqli_query($conn, "set names utf8;");

if(mysqli_connect_errno()){
    printf("실패");
    exit();
}

$url1 = "http://movie.naver.com/movie/running/current.nhn";
$url2 = "http://movie.naver.com/movie/running/premovie.nhn";

get_movie_list($url1,"cur");
get_movie_list($url2,"exp");

$conn->close();


?>
