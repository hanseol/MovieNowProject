<?php
class DBOperations {
    private $con;
    function __construct()
    {
      require_once '/var/www/html/moona/includes/DBConnect.php';
      $db= new DBConnect();
      $this->con=$db->connect();
    }

    //사용자 생성
    public function createUser($username, $pass, $token, $recom,$prefGenre) {//이메일 변수는 나중에 사용자의 선호 장르 배열로 변경할 것
      if ($this->isUserExist($username))//이게 true면 중복 아이디가 있다는 뜻. 중복 아이디 테스트를 제거하면 필요없을지도 몰라!
      {
        return 0;//중복 아이디 존재생
      }
      else {
        //영화 추천 기능 on/off 설정
        $recom_allow=0;

        if ($recom=="true")
          $recom_allow=1;
        else
          $recom_allow=0;

        $query=$this->con->prepare("Insert into user values(?,SHA(?),NOW(),NOW(),?, '$recom_allow', 0)");
        $query->bind_param("sss", $username, $pass, $token);//변수 바인딩

        if ($query->execute())
        {
          //여기부터 다시 수정하기
          //장르 하나하나 확인해서 테이블 만들기
          $sf=$prefGenre['value0'];
          $crime=$prefGenre['value1'];
          $action=$prefGenre['value2'];
          $melo_romance=$prefGenre['value3'];
          $drama=$prefGenre['value4'];
          $comedy=$prefGenre['value5'];
          $animation=$prefGenre['value6'];
          $fantasy=$prefGenre['value7'];
          $adventure=$prefGenre['value8'];
          $thriller=$prefGenre['value9'];
          $family=$prefGenre['value10'];
          $documentary=$prefGenre['value11'];
          $war=$prefGenre['value12'];
          $mystery=$prefGenre['value13'];
          $adult=$prefGenre['value14'];
          $horror=$prefGenre['value15'];
          $musical=$prefGenre['value16'];
          $history=$prefGenre['value17'];
          $western=$prefGenre['value18'];
          $etc=$prefGenre['value19'];

          //회원가입이 정상 처리되었으면
          //선호 장르 생성해야지?


          // $prefCreateQuery=$this->con->prepare("Insert into preference_genre values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
          // $prefCreateQuery->bind_param("siiiiiiiiiiiiiiiiiii", $username, $sf, $crime, $action, $melo_romance, $drama, $comedy, $animation, $fantasy, $adventure, $thriller, $family, $documentary, $war, $mystery, $adult, $horror, $musical, $history,$western);//변수 바인딩

          //echo $prefCreateQuery;

          $prefCreateQuery=$this->con->prepare("Insert into preference_genre values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
          $prefCreateQuery->bind_param("siiiiiiiiiiiiiiiiiiii", $username, $sf, $crime, $action, $melo_romance, $drama, $comedy, $animation, $fantasy, $adventure, $thriller, $family, $documentary, $war, $mystery, $adult, $horror, $musical, $history, $western, $etc);//변수 바인딩
          if ($prefCreateQuery->execute())
          {
            //선호 장르 생성 환료했으면
            //예매 기록 생성해야지?
            // $reserveCreateQuery=$this->con->prepare("Insert into reserved_genre values(?,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)");
            // $reserveCreateQuery->bind_param("s", $username);//변수 바인딩

            $reserveCreateQuery=$this->con->prepare("Insert into reserved_genre values(?,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)");
            $reserveCreateQuery->bind_param("s", $username);//변수 바인딩
            if (!($reserveCreateQuery->execute()))
            {
              return 4;//예매 기록 생성 도중 오류 발생
            }//if ($reserveCreateQuery->execute()) end
          }// if ($prefCreateQuery->execute()) end
          else{
            return 3;//선호 장르 생성 도중 오류 발생
          }
          return 1;//정상 처리
        }// if ($query->execute()) end
        else
        {
          return 2;//회원 정보 저장에 문제 발생
        }
      }
    }

    //선호 장르 업데이트 함수성

    private function updatePrefGenre($genreName, $username)
    {

      $genUpdateQuery=$this->con->prepare("update preference_genre set $genreName=1 where user_id=?");
      $genUpdateQuery->bind_param("s", $username);

      if ($genUpdateQuery->execute())
      {
        return 0;
      }
      else {
        return 5;
      }
    }

    public function updateReservedGenre($genreName, $username)
    {//예매 기록 업데이트하는 함수 -> 응답 유형에 따른 응답 코드 정의할 것(일단은 응답 내용으로 대체)
      $preVal=0;

      $query="select ".$genreName." from reserved_genre where user_id='".$username."'";
      //echo $query;

       if ($getValue=$this->con->query($query)) {
        // 레코드 출력
        while ($getValueRow = $getValue->fetch_object()) {
            $preVal= $getValueRow->$genreName;
        }
        // 메모리 정리
        //$getValue->free();

        $preVal=$preVal+1;
        //echo $preVal;
        $query= "update reserved_genre set ".$genreName."=".$preVal." where user_id='".$username."'";

        if ($updateValue=$this->con->query($query))
        {
          //echo $genreName." 정상 처리 완료";
          return 1;
          //$updateValue->free();
        }
        else {
          //echo "예매 수 저장하는 데서 오류 발생";
          return 2;
          //$updateValue->free();
        }
       }
      else {
        //에러 코드 정의해야 될 텐데
        //echo "예매 수 가져오는 데서 오류 발생";
        return 3;
        //$getValue->free();
      }

    }

    //사용자 로그인
    public function userLogin($username, $pass)
    {
      $query=$this->con->prepare("select * from user where id=? and password=SHA(?)");
      $query->bind_param("ss", $username,$pass);
      $query->execute();
      $query->store_result();

      //최근 로그인 시간 업데이트
      if ($query->num_rows > 0)
      {
        $logquery=$this->con->prepare("update user set last_login=NOW() where id=?");
        $logquery->bind_param("s", $username);
        $logquery->execute();

        $logquery=$this->con->prepare("update user set isloggedin=1 where id=?");
        $logquery->bind_param("s", $username);
        $logquery->execute();
      }

      return $query->num_rows > 0;
    }

    //사용자 로그아웃
    public function userLogout($username)
    {
      $logoutQuery="update user set isloggedin=0 where id='".$username."'";

      $logoutResult=$this->con->query($logoutQuery);
      if ($logoutResult)
      {
        //정상 처리
        return 1;
      }
      else
      {
        //에러 발생
        return 0;
      }
    }

    //비밀번호 확인
    public function checkPassword($username, $password)
    {
      $query=$this->con->prepare("select * from user where id=? and password=SHA(?)");
      $query->bind_param("ss", $username,$password);
      $query->execute();
      $query->store_result();

      if ($query->num_rows > 0)
        return 1;//정상 처리
      else {
        return 0;//비밀번호 틀림
      }
    }

    //비밀번호 변경
    public function changePassword($username, $password)
    {
      $query= "update user set password=SHA(".$password.") where id='".$username."'";
      //echo $query;
      if ($updateValue=$this->con->query($query))
        return 1;
      else
        return 0;
    }

    //사용자 아이디 얻어오기
    // public function getUserByID($username)//??뭐에 쓰는 거지
    // {
    //   $query=$this->con->prepare("select * from user where id=?");
    //   $query->bind_param("s", $username);
    //   return $query->get_result()->fetch_assoc();
    // }

    //중복 유저 체크
    public function isUserExist($username) {
      $query=$this->con->prepare("select * from user where id=?");
      $query->bind_param("s", $username);
      $query->execute();
      $query->store_result();
      return $query->num_rows > 0;
    }

    //사용자 삭제
    public function deleteUser($username)
    {
      $logoutQuery="delete from user where id='".$username."'";

      $logoutResult=$this->con->query($logoutQuery);
      if ($logoutResult)
      {
        //정상 처리
        return 1;
      }
      else
      {
        //에러 발생
        return 0;
      }
    }

    //사용자의 선호 장르 검사
    public function checkPrefGenre($username)
    {
      $refArr=array();

      $checkRecomQuery="select * from user where id='".$username."'";
      $checkRecomResult=$this->con->query($checkRecomQuery);
      if ($checkRecomResult)
      {
        $recomRow=$checkRecomResult->fetch_object();

        $refArr['recommendation']=$recomRow->recommend_on;

        $checkPrefQuery="select * from preference_genre where user_id='".$username."'";
        $checkPrefResult=$this->con->query($checkPrefQuery);
        if ($checkPrefResult)
        {
          $prefRow = $checkPrefResult->fetch_object();
          $refArr['success']=true;
          $refArr['value0']=$prefRow->SF;
          $refArr['value1']=$prefRow->crime;
          $refArr['value2']=$prefRow->action;
          $refArr['value3']=$prefRow->melo_romance;
          $refArr['value4']=$prefRow->drama;
          $refArr['value5']=$prefRow->comedy;
          $refArr['value6']=$prefRow->animation;
          $refArr['value7']=$prefRow->fantasy;
          $refArr['value8']=$prefRow->adventure;
          $refArr['value9']=$prefRow->thriller;
          $refArr['value10']=$prefRow->family;
          $refArr['value11']=$prefRow->documentary;
          $refArr['value12']=$prefRow->war;
          $refArr['value13']=$prefRow->mystery;
          $refArr['value14']=$prefRow->adult;
          $refArr['value15']=$prefRow->horror;
          $refArr['value16']=$prefRow->musical;
          $refArr['value17']=$prefRow->history;
          $refArr['value18']=$prefRow->western;
          $refArr['value19']=$prefRow->etc;

          return $refArr;
        }//if ($checkPrefResult) end
        else{
          //선호 장르를 불러오는 도중 오류 발생
          $refArr['success']=false;
          return $refArr;
        }
      }// if ($checkRecomResult) end
      else {
        //추천 여부를 불러오는 도중 오류 발생
        $refArr['success']=false;
        return $refArr;
        # code...
      }
    }

    //사용자 선호 장르 변경
    public function changePrefGenre($username, $recom,$prefGenre)
    {
      //영화 추천 기능 값 확인 및 변경
      $recom_allow=0;

      if ($recom=="true")
        $recom_allow=1;
      else
        $recom_allow=0;
      $query=$this->con->prepare("update user set recommend_on='$recom_allow' where id=?");
      $query->bind_param("s", $username);
      if ($query->execute())
      {
        //선호 장르 확인 및 변경
        $sf=$prefGenre['value0'];
        $crime=$prefGenre['value1'];
        $action=$prefGenre['value2'];
        $melo_romance=$prefGenre['value3'];
        $drama=$prefGenre['value4'];
        $comedy=$prefGenre['value5'];
        $animation=$prefGenre['value6'];
        $fantasy=$prefGenre['value7'];
        $adventure=$prefGenre['value8'];
        $thriller=$prefGenre['value9'];
        $family=$prefGenre['value10'];
        $documentary=$prefGenre['value11'];
        $war=$prefGenre['value12'];
        $mystery=$prefGenre['value13'];
        $adult=$prefGenre['value14'];
        $horror=$prefGenre['value15'];
        $musical=$prefGenre['value16'];
        $history=$prefGenre['value17'];
        $western=$prefGenre['value18'];
        $etc=$prefGenre['value19'];

        $resetQuery=$this->con->prepare("update preference_genre set SF=?,crime=?,action=?,melo_romance=?,drama=?,comedy=?,animation=?,fantasy=?,adventure=?,thriller=?,family=?,documentary=?,war=?,mystery=?,adult=?,horror=?,musical=?,history=?,western=?,etc=? where user_id=?");
        //$resetQuery=$this->con->prepare("update preference_genre set action=0,adult=0,adventure=0,animation=0 where id=?");
        $resetQuery->bind_param("iiiiiiiiiiiiiiiiiiiis",$sf,$crime,$action,$melo_romance,$drama,$comedy,$animation,$fantasy,$adventure, $thriller,$family,$documentary, $war,$mystery,$adult,$horror,$musical,$history,$western,$etc, $username);
        if ($resetQuery->execute())
        {
          return 1;
        }
      }
      else {
        //추천 여부 업데이트 도중 오류 발생
        return 2;
      }
    }
}
?>
