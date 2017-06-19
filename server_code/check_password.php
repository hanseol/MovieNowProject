<?php

require_once './includes/DBOperations.php';

//rescode 1 : 아이디와 비밀번호가 맞지 않습니다
//rescode 2 : 아이디와 비밀번호를 모두 적어주세요

$response=array();
  if ($_SERVER['REQUEST_METHOD']=='POST')//POST 방식으로 접근했을 경우
  {
    if (isset($_POST['userID']) && isset($_POST['userPW']))
    {
      $db=new DBOperations();
      $userID=trim($_POST['userID']);
      $userPW=trim($_POST['userPW']);
      if ($db->checkPassword($userID, $userPW))
      {
        //정상 처리
        $response['success']=true;
        $response['rescode']=1;
      }//userLogin end
      else {
        //아이디와 비밀번호 틀림
        $response['success']=false;
        $response['rescode']=0;
      }//userLogin else end
    }//isset end
  }//REQUEST_METHOD end
  else {
    //올바른 요청이 아님
    $response['success']=false;
    $response['rescode']=2;
  }

echo json_encode($response);
?>
