package kr.ac.kumoh.s20140350.movienow;

/**
 * Created by SEOL on 2017-04-10.
 */

public class MovieInfo {
    String id;
    String name;
    String genre;
    String date;
    String grade;
    String poster;
    String age;

    public MovieInfo(String id, String name, String genre, String date, String grade, String poster, String age) {
        this.id=id;
        this.name=name;
        this.genre=genre;
        this.date=date;
        this.grade=grade;
        this.poster=poster;
        this.age=age;
    }

    public String getId() {return id;}
    public String getName(){
        return name;
    }
    public String getGenre(){
        return genre;
    }
    public String getDate(){
        return date;
    }
    public String getGrade(){
        return grade;
    }
    public String getPoster(){
        return poster;
    }
    public String getAge(){
        return age;
    }
}

