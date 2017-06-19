package kr.ac.kumoh.s20140350.movienow;

/**
 * Created by SEOL on 2017-04-10.
 */

public class TheaterInfoTest {
    String time;
    String branch;
    String theater;
    String distance;

    public TheaterInfoTest(String time, String branch, String theater, String distance) {
        this.time = time;
        this.branch = branch;
        this.theater = theater;
        this.distance = distance;
    }

    public String getTime() { return time; }
    public String getBranch() {return branch;}
    public String getTheater() {return theater;}
    public String getDistance() {return distance;}


}
