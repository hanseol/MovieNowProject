<?php

require_once './includes/DBOperations.php';

// $registerSucc="회원가입이 정상적으로 완료되었습니다";
// $registerFail="회원가입에 실패했습니다. 다시 시도해 주세요";
// $fillAllParam="입력 포맷을 전부 채워주세요";
// $invalidRequest="잘못된 요청입니다";

$registerSucc="사용할 수 있는 아이디입니다"; //응답 코드 1번
$registerFail="이미 사용중인 아이디입니다"; //응답 코드 2번
$fillAllParam="아이디를 입력하세요";//응답 코드 3번
$invalidRequest="유효하지 않은 요청입니다"; //응답 코드 4번

$response=array();
  if ($_SERVER['REQUEST_METHOD']=='POST')//POST 방식으로 접근했을 경우
  {
    $db=new DBOperations();
    //$result=$db->createUser($userID, $userPW, 'example');

    if (isset($_POST['userID']))
    {
      $userID=trim($_POST['userID']);

      $result=$db->isUserExist($userID);
      if (!$result)
      {
        $response['sucess']=true;
        $response['rescode']=1;
      }
      else {
        $response['sucess']=false;
        $response['rescode']=2;
      }
    }
    else {
      $response['sucess']=false;
      $response['rescode']=3;
    }
  }
  else {
    $response['sucess']=false;
    $response['rescode']=4;
  }

  echo json_encode($response);
?>
