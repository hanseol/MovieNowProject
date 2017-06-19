<?php

//영화정보 저장하는 배열(고정값들 저장)
$movieListArr=array();
$movieNameArr=array();

//영화정보 저장하는 배열 -각 유저들에 대해 실시
$userMovieArr=array();

//사용자의 추천 기록을 저장하는 배열
$recMovieArr=array();

//사용자의 장르 가중치를 저장하는 배열
$genWeightArr=array();

//가중치 배열 중 추천 기준을 넘어선 장르들을 저장하는 배열(추천 후보 배열)
$candiGenArr=array();

//가중치 배열 중 추천 기록에 없는 정말 최종 장르들을 저장하는 배열(최종 추천 후보 배열)
$finalGenArr=array();

//추천 기준치 = 10
define("recomStandard",10);

$i=0;
$today="오늘은 ";
$recGenre;
$str1="영화인 ";
$recMovie;
$str2=" 어떠세요?";

$userToken;

//db 연결
require_once '/var/www/html/moona/send_push.php';
include_once '/var/www/html/moona/includes/DBConnect.php';
$connect=new DBConnect();
$con=$connect->connect();

if (mysqli_connect_errno()){
  echo "Failed to connect with database".mysqli_connect_err();
}

$con->query("set session character_set_connection=utf8;");

$con->query("set session character_set_results=utf8;");

$con->query("set session character_set_client=utf8;");


//영화 리스트 추출(완료)
//$result = $con->query("select * from movie_list");
$result = $con->query("select * from movie_list");
if ($result) {
  //레코드 출력
  while ($row = $result->fetch_object()) {
      $nameindex="name".$i;
      $genindex="genre".$i;
      $movieListArr[$nameindex]=$row->movie_name;
      $movieNameArr[$row->movie_name]=$row->movie_name;
      $movieListArr[$genindex]=$row->movie_en_genre;
      $i++;
      echo $movieListArr[$nameindex].'<br/>'.$movieListArr[$genindex].'<br/>';
  }

  // 메모리 정리
  //$result->free();
}

echo '<br/><br/><br/>';
//사용자 목록 조회
$result=$con->query("select * from user");
if ($result) {
  //레코드 출력
  while ($row = $result->fetch_object()) {

      $username=$row->id;
      $userToken=$row->user_token;

      //기존 유저의 정보들을 담은 배열 초기화
      unset($recMovieArr);
      unset($genWeightArr);
      unset($candiGenArr);
      unset($finalGenArr);
      unset($userMovieArr);

      $recMovieArr=array();
      $genWeightArr=array();
      $candiGenArr=array();
      $finalGenArr=array();
      $userMovieArr=array();

      if ($row->recommend_on==0)
      {
        echo $username."님은 현재 추천 기능을 사용하지 않습니다<br><br>";
        continue;
      }
      else if ($row->isloggedin==0)
      {
        echo $username."님은 현재 로그인 상태가 아닙니다<br><br>";
        continue;
      }

      $i=0;
      //해당 사용자의 추천 내역 검색
      $recHistoryQuery="select * from recommend_list where user_id='".$username."'";
      $recHistoryResult=$con->query($recHistoryQuery);
      if ($recHistoryResult)
      {
        while($recHistoryRow = $recHistoryResult->fetch_object())
        {
          $nameindex="name".$i;
          $recMovieArr[$recHistoryRow->movie_name]=$recHistoryRow->movie_name;
          $i++;
        }
      }// if ($recHistoryResult) end
      else {
        echo "사용자의 추천 기록 검색 중 오류 발생";
      }

      //추천 내역과 전날 박스오피스 내역에서 동일한 영화 삭제

      // foreach($movieNameArr as $x => $x_value) {
      //   echo $x.' : '. $x_value.'<br/>';
      // }
      // echo '<br>';
      // foreach($recMovieArr as $x => $x_value) {
      //   echo $x.' : '. $x_value.'<br/>';
      // }
      // echo '<br>';
      //

      $arr=$movieNameArr;
      if (count($recMovieArr)!=0)
      {
        $arr=array_diff_key($movieNameArr,$recMovieArr);
      }

      // foreach($arr as $x => $x_value) {
      //   echo $x.' : '. $x_value.'<br/>';
      // }
      // echo '<br>';
      //추천할 영화가 하나도 없다면(이미 박스오피스의 영화들이 이미 추천된 것들이라면) 넘어감
      if (count($arr)==0)
      {
        echo $username."님은 추천할 영화가 없습니다<br><br>";
        continue;
      }

      //해당 유저의 선호 장르 검색하여 가중치 배열에 저장(선호 장르는 10의 가중치를 갖고 있음)
      $prefQuery="select * from preference_genre where user_id='".$username."'";
      $prefResult=$con->query($prefQuery);
      if ($prefResult)
      {
        while($prefRow = $prefResult->fetch_object())
        {

          $genWeightArr['SF']=$prefRow->SF;
          $genWeightArr['crime']=$prefRow->crime;
          $genWeightArr['action']=$prefRow->action;
          $genWeightArr['melo_romance']=$prefRow->melo_romance;
          $genWeightArr['drama']=$prefRow->drama;
          $genWeightArr['comedy']=$prefRow->comedy;
          $genWeightArr['animation']=$prefRow->animation;
          $genWeightArr['fantasy']=$prefRow->fantasy;
          $genWeightArr['adventure']=$prefRow->adventure;
          $genWeightArr['thriller']=$prefRow->thriller;
          $genWeightArr['family']=$prefRow->family;
          $genWeightArr['documentary']=$prefRow->documentary;
          $genWeightArr['war']=$prefRow->war;
          $genWeightArr['mystery']=$prefRow->mystery;
          $genWeightArr['adult']=$prefRow->adult;
          $genWeightArr['horror']=$prefRow->horror;
          $genWeightArr['musical']=$prefRow->musical;
          $genWeightArr['history']=$prefRow->history;
          $genWeightArr['western']=$prefRow->western;
          $genWeightArr['etc']=$prefRow->etc;

          // echo $genWeightArr['SF']."<br>";
          // echo $genWeightArr['crime']."<br>";
          // echo $genWeightArr['action']."<br>";
          // echo $genWeightArr['melo_romance']."<br>";
          // echo $genWeightArr['drama']."<br>";
          // echo $genWeightArr['comedy']."<br>";
          // echo $genWeightArr['animation']."<br>";
          // echo $genWeightArr['fantasy']."<br>";
          // echo $genWeightArr['adventure']."<br>";
          // echo $genWeightArr['thriller']."<br>";
          // echo $genWeightArr['family']."<br>";
          // echo $genWeightArr['documentary']."<br>";
          // echo $genWeightArr['war']."<br>";
          // echo $genWeightArr['mystery']."<br>";
          // echo $genWeightArr['adult']."<br>";
          // echo $genWeightArr['horror']."<br>";
          // echo $genWeightArr['musical']."<br>";
          // echo $genWeightArr['history']."<br>";
          // echo $genWeightArr['etc']."<br>";

        }
      } // if ($prefResult) end
      else {
        echo "사용자의 선호 장르 추출 중 오류 발생";
      }


      //해당 유저의 예매 기록 검색해서 가중치 배열에 저장(예매 기록은 일단 1의 가중치를 준다) - 완료=====================================

      $reservQuery="select * from reserved_genre where user_id='".$username."'";
      $reservResult=$con->query($reservQuery);
      if ($reservResult)
      {
        while($reservRow = $reservResult->fetch_object())
        {

          $genWeightArr['SF']+=($reservRow->SF)*2;
          $genWeightArr['crime']+=($reservRow->crime)*2;
          $genWeightArr['action']+=($reservRow->action)*2;
          $genWeightArr['melo_romance']+=($reservRow->melo_romance)*2;
          $genWeightArr['drama']+=($reservRow->drama)*2;
          $genWeightArr['comedy']+=($reservRow->comedy)*2;
          $genWeightArr['animation']+=($reservRow->animation)*2;
          $genWeightArr['fantasy']+=($reservRow->fantasy)*2;
          $genWeightArr['adventure']+=($reservRow->adventure)*2;
          $genWeightArr['thriller']+=($reservRow->thriller)*2;
          $genWeightArr['family']+=($reservRow->family)*2;
          $genWeightArr['documentary']+=($reservRow->documentary)*2;
          $genWeightArr['war']+=($reservRow->war)*2;
          $genWeightArr['mystery']+=($reservRow->mystery)*2;
          $genWeightArr['adult']+=($reservRow->adult)*2;
          $genWeightArr['horror']+=($reservRow->horror)*2;
          $genWeightArr['musical']+=($reservRow->musical)*2;
          $genWeightArr['history']+=($reservRow->history)*2;
          $genWeightArr['western']+=($reservRow->western)*2;
          $genWeightArr['etc']+=($reservRow->etc)*2;

          // echo $genWeightArr['SF']."<br>";
          // echo $genWeightArr['crime']."<br>";
          // echo $genWeightArr['action']."<br>";
          // echo $genWeightArr['melo_romance']."<br>";
          // echo $genWeightArr['drama']."<br>";
          // echo $genWeightArr['comedy']."<br>";
          // echo $genWeightArr['animation']."<br>";
          // echo $genWeightArr['fantasy']."<br>";
          // echo $genWeightArr['adventure']."<br>";
          // echo $genWeightArr['thriller']."<br>";
          // echo $genWeightArr['family']."<br>";
          // echo $genWeightArr['documentary']."<br>";
          // echo $genWeightArr['war']."<br>";
          // echo $genWeightArr['mystery']."<br>";
          // echo $genWeightArr['adult']."<br>";
          // echo $genWeightArr['horror']."<br>";
          // echo $genWeightArr['musical']."<br>";
          // echo $genWeightArr['history']."<br>";
          // echo $genWeightArr['etc']."<br>";

        }
      }// if ($reservResult) end
      else {
        echo "사용자의 예매 기록 추출 중 오류 발생";
      }

      //해당 유저의 예매 기록 검색해서 가중치 배열에 저장 끝=====================================
      //echo '<br><br><br>';
      //배열을 가중치 값으로 정렬
      arsort($genWeightArr);
      //추천 기준 넘어가는 가중치를 별도의 배열에 저장(완료)
      foreach($genWeightArr as $x => $x_value) {
        //echo $x.' : '. $x_value.'<br>';
        if ($x_value>=recomStandard)
          $candiGenArr[$x]=$x;
      }

      // foreach($candiGenArr as $x => $x_value) {
      //   echo $x.' : '. $x_value.'<br>';
      //   // if ($x_value>=recomStandard)
      //   //   $candiGenArr[$x]=$x;
      // }

      //만약 추천할 장르가 하나도 없으면 넘어감
      if (count($candiGenArr)==0)
      {
        echo $username."님은 추천할 장르가 없습니다<br><br>";
        continue;
      }

      // foreach($candiGenArr as $x => $x_value) {
      //   echo $x.' : '. $x_value.'<br/>';
      // }
      // echo '<br>';


      //영화 이름만 있는 배열에다가 장르도 다시 추가
      $count=0;
      foreach($arr as $x => $x_value) {
        for ($i=0; $i<count($movieListArr); $i++)
        {
          $nameindex="name".$i;
          $genindex="genre".$i;
          if ($x_value==$movieListArr[$nameindex])
          {
            //echo "Ehrrkxek!";
            $countNameIdx="name".$count;
            $countGenIdx="genre".$count;
            $userMovieArr[$countNameIdx]=$x_value;
            $userMovieArr[$countGenIdx]=$movieListArr[$genindex];
            $count++;
          }
        }
      }// foreach($arr as $x => $x_value) end

      $count=0;
      $isSelected=0;
      foreach ($candiGenArr as $x => $x_value)
      {
        for ($i=0; $i<count($userMovieArr)/2; $i++)
        {
          $countNameIdx="name".$i;
          $countGenIdx="genre".$i;
          if ($userMovieArr[$countGenIdx]==$candiGenArr[$x])
          {
            $recGenre=$userMovieArr[$countGenIdx];
            $recMovie=$userMovieArr[$countNameIdx];
            $isSelected=1;
            break;
          }
        }
        if ($isSelected)
          break;
      }

      if ($isSelected==0)
      {
        echo $username."님은 추천할 영화가 없습니다<br><br>";
        continue;
      }
      echo $username.' : '.$recGenre.' '. $recMovie."<br><br>";

      //장르 변환하기

      switch($recGenre)
      {
        case "crime":
        $recGenre="범죄";
        break;

        case "action":
        $recGenre="액션";
        break;

        case "melo_romance":
        $recGenre="멜로/로맨스";
        break;

        case "drama":
        $recGenre="드라마";
        break;

        case "comedy":
        $recGenre="코미디";
        break;

        case "animation":
        $recGenre="애니메이션";
        break;

        case "fantasy":
        $recGenre="판타지";
        break;

        case "adventure":
        $recGenre="어드벤처";
        break;

        case "thriller":
        $recGenre="스릴러";
        break;

        case "family":
        $recGenre="가족";
        break;

        case "documentary":
        $recGenre="다큐멘터리";
        break;

        case "war":
        $recGenre="전쟁";
        break;

        case "mystery":
        $recGenre="미스터리";
        break;

        case "adult":
        $recGenre="성인";
        break;

        case "horror":
        $recGenre="공포/호러";
        break;

        case "musical":
        $recGenre="뮤지컬";
        break;

        case "history":
        $recGenre="역사";
        break;

        case "western":
        $recGenre="서부";
        break;


        case "etc":
        $recGenre="기타";
        break;
      }

      $finalString=$today.$recGenre." ".$str1."'".$recMovie."'".$str2;

      //요거 쓰면 됨
      //echo $userToken."userID : ".$username;
      //echo $userToken;
      send_push($userToken, $finalString);
      echo "<br><br>";

      //이후 추천 내역을 데이터베이스에 저장

      $recomUpdateQuery="insert into recommend_list values('".$username."', '".$recMovie."', 1)";
      $recomUpdateResult=$con->query($recomUpdateQuery);
      if ($recomUpdateResult)
      {
        echo $username."님의 추천 목록에 ".$recMovie."가 정상적으로 추가되었습니다";
      }// if ($recHistoryResult) end
      else {
        echo "사용자의 추천 내역 업데이트 중 오류 발생";
      }
  }//while ($row = $result->fetch_object()) { end
}
// 접속 종료
$con->close();


?>
