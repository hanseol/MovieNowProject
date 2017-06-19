<?php
  function send_push ($token, $message)
  {
    // $i=0;
    // require_once("./includes/Constants.php");
    // $con=mysqli_connect(DB_HOST,DB_USER,DB_PASSWORD,DB_NAME);
    // // $sql="select user_token from user where id=".$username;
    // $sql="select user_token from user where id='".$username."'";
    // $result=mysqli_query($con, $sql);
    // $tokens=array();
    //
    // if (mysqli_num_rows($result)>0)
    // {
    //   while ($row=mysqli_fetch_assoc($result)) {
    //     $tokens[$i]=$row["user_token"];
    //     $i++;
    //   }
    // }



    $tokens=array();
    $tokens[]=$token;

    //mysqli_close($con);
    // $response=send_push($tokens, "오늘은 ~의 ~ 어떠세요?");
    // echo $response;


    $url='https://fcm.googleapis.com/fcm/send';

    $fields = array (
    'to'=>$token,
    'data' => array ("title" => "오늘의 영화 추천입니다!",
                              "body" => $message)
    );
    echo $fields['notification']['title'];


    //$fields['to']=json_encode($tokens);
    //$fields['data']=$message;

    //테스트 앱
    // $headers = array(
    //     'Authorization:key=AAAANd_wh_s:APA91bEkt-xZDwYhZNWKhdPm-NwFHSnBKMPj0Jl97I-xjfnWgpUCG7PrA9lPj9sK5NVQPr_IYvCC7aj-aue7lmuWHhCBCDUvkih6aBijBJmoxSaK6PkBv_BCVyiAE7u-EX9pZSoBtVz_',
    //     'Content-Type: application/json'
    // );

    //최종 앱
     $headers = array(
        'Authorization:key=AAAAEjr6WwQ:APA91bHtuzzFwdtMu9Vz-uImg02v19aRCKBYoODEAZgP99eYJbdQqZMDSXbHt0D_B6CVwrIkvJy4cTbUXSDZkL5QBTM8ArSvebfGbuVzVN3mHtmNuSvbeuJF7h_wz5Pe_qgGrO4swd2U',
      'Content-Type: application/json'
    );

    //테스트
  //  $headers = array(
  //      'Authorization:key=AAAAVEqPeBU:APA91bGvy_7oi0VEKwm6ghvmp7FKk7McrerYnUI6xO9xH0EJKhbDUw5sLPP2lL6ed08XrrInCUzghABDlAGfdW2Oe_ecRGd6_4Y-nvsN3Xdh43x8oiMBHOJZPPjSv9n1-LP2DTbwj8Gd',
  //      'Content-Type: application/json'
  //  );


    $ch=curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    // curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
    // curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

    $result=curl_exec($ch);
    if ($result==FALSE)
      die ('Curl failed :'.curl_error($ch));

    curl_close($ch);

    echo $result;

  }
  //테스트를 위해 작성한 예제 코드이므로 추천 알고리즘 스크립트에 쓰일 예정 ㅎ


?>
