<?php

require_once './includes/DBOperations.php';

// $registerSucc="회원가입이 정상적으로 완료되었습니다";
// $registerFail="회원가입에 실패했습니다. 다시 시도해 주세요";
// $fillAllParam="입력 포맷을 전부 채워주세요";
// $invalidRequest="잘못된 요청입니다";

$registerSucc="Registeration Success"; //응답 코드 1번
$registerFail="Registeration Failed. Please try again."; //응답 코드 2번
//응답 코드 3번. 중복된 아이디가 있습니다.
$fillAllParam="Please fill whole input format";//응답 코드 4번
$invalidRequest="Invalid Request"; //응답 코드 5번

//응답 코드 1번 (회원가입 응답 1) : 정상 처리
//응답 코드 2번 (회원가입 응답 2) :회원 정보 저장 도중 문제 발생
//응답 코드 3번 (회원가입 응답 0): 중복 아이디 존재
//응답 코드 4번 : 아이디와 패스워드, 필요한 정보들이 일부 빈 채로 전달됨
//응답 코드 5번 : 잘못된 요청(POST가 아님)
//응답 코드 6번. (회원가입 응답 3) : 선호 장르 생성 도중 문제 발생
//응답 코드 7번 (회원가입 응답 4) : 예매 기록 생성 도중 문제 발생
//응답 코드 8번(회원가입 응답 5) : 선호 장르 업데이트 도중 문제 발생
//20150506 :안드로이드 코드에서 체크박스의 장르를 변환하는 과정에서 문제가 있는 것 같음. 디비의 장르와 안드로이드 코드의 장르가 정확히 일치하는지 볼 것

$response=array();
$prefGenre=array();
  if ($_SERVER['REQUEST_METHOD']=='POST')//POST 방식으로 접근했을 경우
  {
    if (isset($_POST['userID']) && isset($_POST['recommendation'])) //입력 필드가 다 들어있는지 확인 -> 안드로이드에서 PHP로 다중변수 어떻게 넘겨주지....
    {
      $userID=trim($_POST['userID']);
      $recom=trim($_POST['recommendation']);//추천기능 사용 여부(0/1)

      //선호 장르 뽑아내기(완료)
      for ($i=0; $i<20; $i++)
      {
       $index="value".$i;
       $prefGenre[$index]=trim($_POST[$index]);
       //fwrite($myfile, $prefGenre[$index]);
      }
      // echo json_encode($prefGenre);
      $db=new DBOperations();
      $result=$db->changePrefGenre($userID, $recom, $prefGenre);
      if ($result==0)
      {
        //선호 장르 업데이트 도중 문제 발생
        $response['success']=false;
        $response['rescode']=0;
      }
      else if ($result==1){
        //정상 처리
        $response['success']=true;
        $response['rescode']=1;
      }
      else if ($result==2)
      {
        //추천 여부 변경 도중 오류 발생
        $response['success']=false;
        $response['rescode']=2;
      }
    }
  }
  else {
    $response['sucess']=false;
    $response['rescode']=3; //올바른 요청이 아님(POST방식이 아님)
  }
  echo json_encode($response);


?>
