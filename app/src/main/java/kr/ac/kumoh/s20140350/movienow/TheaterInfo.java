package kr.ac.kumoh.s20140350.movienow;

/**
 * Created by SEOL on 2017-04-10.
 */

public class TheaterInfo {
    String time;
    String url;
    String theater;

    public TheaterInfo(String time, String url, String theater) {
        this.time = time;
        this.url = url;
        this.theater = theater;
    }

    public String getTime() { return time; }
    public String getUrl() {return url;}
    public String getTheater() {return theater;}


}
