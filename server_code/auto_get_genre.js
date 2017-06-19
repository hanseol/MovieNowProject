var cron=require('node-cron');
var http = require( "http" );
var mysql = require('mysql');

var connection;


//날짜 계산
function CalDate()
{
  var today = new Date();
  var dd = today.getDate()-1;
  var mm = today.getMonth()+1; //January is 0!
  var yyyy = today.getFullYear();
  if(dd<10) {
      dd='0'+dd
  }
  if(mm<10) {
      mm='0'+mm
  }
  return yyyy+mm+dd;
}


function DBCon()
{
  connection = mysql.createConnection({ //MySQL 연결을 위한 정보
    host     : 'localhost',
    user     : 'root',
    password : '133nplab',
    port     : 3306,
    database : 'moona'
  });

  //영화 목록을 MySQL에 저장하기 위해 커넥션 생성
  connection.connect(function(err) { //MySQL 연결. 에러 발생 시 콜백함수로 에러 내용 표시
      if (err) {
          console.error('mysql connection error');
          console.error(err);
          throw err;
      }
      else {
        console.log("DB접속 완료");
      }
  });
}

function DBClose()
{
  connection.end();
}

var key="8d90e1d7cc68d0c50a028641ddb6279d";
//var movieCodeUrl='http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key='+key+'&targetDt='+date;
var movieGenUrl='';
var date;
var movieCodeUrl;
var parsed_movieGen;
var movieCodeArr=["","","","","","","","","",""];
var movieGenObj=[Object(),Object(),Object(),Object(),Object(),Object(),Object(),Object(),Object(),Object()];
var movieGenInsertData;
var i;

function DeleteMovieList(){
  var devQuery = connection.query('delete from movie_list', function(err, rows, fields) {
    if (err)//실패할 시 에러 메시지 전송
    {
      console.log('Error while performing Query(Deleting Movie Data). : ', err.code);
    }
    else
    {
      console.log('info', '1일 전 영화 정보 삭제가 완료되었습니다 : ');
    }
  });
}

function GetMovieGen()
{
  movieGenObj=[Object(),Object(),Object(),Object(),Object(),Object(),Object(),Object(),Object(),Object()];
    //이제 여기서 각각의 것들을 다시 영화정보 REST API를 통해 정보 받아올 것
    for (i=0; i<10; i++)
    {
      movieGenUrl='http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json?key='+key+'&movieCd='+movieCodeArr[i];
      //console.log(movieGenUrl);
      http.get(movieGenUrl, function(res){
          var body = '';

          res.on('data', function(chunk){
              body += chunk;
          });

          res.on('end', function(){
              parsed_movieGen = JSON.parse(body);

              switch(parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm)
              {
                case "액션":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "action"
                    }
                  break;
                case "어드벤쳐":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "adventure"
                    }
                  break;
                case "다큐멘터리":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "documentary"
                    }
                  break;
                case "드라마":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "drama"
                    }
                  break;
                case "성인물(에로)":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "adult"
                    }
                  break;
                case "코미디":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "comedy"
                    }
                  break;
                case "사극":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "history"
                    }
                  break;
                case "미스터리":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "mystery"
                    }
                  break;
                case "멜로/로맨스":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "melo_remance"
                    }
                  break;
                case "범죄":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "crime"
                    }
                  break;
                case "애니메이션":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "animation"
                    }
                  break;
                case "기타":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "etc"
                    }
                  break;
                case "스릴러":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "thriller"
                    }
                  break;
                case "가족":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "family"
                    }
                  break;
                case "공포(호러)":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "horror"
                    }
                  break;
                case "전쟁":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "war"
                    }
                  break;
                case "SF":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "SF"
                    }
                  break;
                case "판타지":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "fantasy"
                    }
                  break;
                case "뮤지컬":
                movieGenObj[i] = {
                      MovieName : parsed_movieGen.movieInfoResult.movieInfo.movieNm,
                      MovieGenre : parsed_movieGen.movieInfoResult.movieInfo.genres[0].genreNm,
                      MovieEnGenre : "musical"
                    }
                  break;

              }
              movieGenInsertData=[movieGenObj[i].MovieName, movieGenObj[i].MovieGenre, movieGenObj[i].MovieEnGenre];
              var devQuery = connection.query('insert into movie_list values(NOW(),?,?,?)',movieGenInsertData, function(err, rows, fields) {
                if (err)//실패할 시 에러 메시지 전송
                {
                  console.log('Error while performing Query(Movie Data Inserting). : ', err.code);
                }
                else
                {
                  console.log('info', '영화 정보 저장이 완료되었습니다 : ' + new Date());
                }
              });
          //console.log("이름 : " + parsed_movieGen.movieListResult.movieList[0].movieNm + " / 장르 : "+ parsed_movieGen.movieListResult.movieList[0].repGenreNm);
          //console.log("Got a response: ", movieNameArr);
          //console.log("Got a response: ", parsed_data.boxOfficeResult.dailyBoxOfficeList[0].movieNm); //데이터를 정상적으로 받아오는 것은 확인
          });

      }).on('error', function(e){
            console.log("Got an Error : ", e);
      });
    }
    setTimeout(DBClose, 3000);
    //mysql에 저장
    console.log("");
}

console.log("영화 장르 자동 추출 중...");
console.log("(Movie Genre is being Exracted...)")

//cron.schedule('*/1 * * * *', function () { //매일 아침 10시 0,1,2분 실행(하루에 총 3번. 혹시나 있을 에러 방지)
cron.schedule('0 10 * * *', function () { //매일 아침 10시 0,1,2분 실행(하루에 총 3번. 혹시나 있을 에러 방지)
  DBCon();

  date=CalDate();//오늘 날짜를 넣을 것(아마 데이터 받으면 그거 갖고 가공해야 할 거야)
  movieCodeUrl='http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key='+key+'&targetDt=' + date;
  movieGenUrl='';
  movieCodeArr=["","","","","","","","","",""];

  DeleteMovieList();
  console.log("delete까지 완료");
  //console.log('info', 'running a task every minute / ' + new Date());
  http.get(movieCodeUrl, function(res){
      var body = '';
      res.setEncoding('utf8');
      res.on('data', function(chunk){
          body += chunk;
      });

      res.on('end', function(){
          parsed_data = JSON.parse(body);
          for (i=0; i<10; i++)
          {
            movieCodeArr[i]=parsed_data.boxOfficeResult.dailyBoxOfficeList[i].movieCd;
            console.log(movieCodeArr[i]);
          }
            
          //console.log("Got a response: ", movieCodeArr);
          //console.log("Got a response: ", parsed_data.boxOfficeResult.dailyBoxOfficeList[0].movieNm); //데이터를 정상적으로 받아오는 것은 확인
      });
  }).on('error', function(e){
        console.log("Got an error: ", e);
  });
  setTimeout(GetMovieGen, 2000);//데이터가 다 들어가게 3초 자고 나서 실행.
}).start();
