<?php

require_once './includes/DBOperations.php';

//rescode 1 : 아이디와 비밀번호가 맞지 않습니다
//rescode 2 : 아이디와 비밀번호를 모두 적어주세요

$response=array();
  if ($_SERVER['REQUEST_METHOD']=='POST')//POST 방식으로 접근했을 경우
  {
    if (isset($_POST['userID']))
    {
      $db=new DBOperations();
      $userID=trim($_POST['userID']);
      if ($db->userLogout($userID))
      {
        $response['success']=true;
      }//userLogout end
      else {
        $response['success']=false;
      }//userLogout else end
    }//isset end
  }//REQUEST_METHOD end

echo json_encode($response);
?>
