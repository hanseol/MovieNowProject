package kr.ac.kumoh.s20140350.movienow;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SEOL on 2017-04-13.
 */

public class SecondActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    public static final String MOVIETAG = "MovieTag";
    protected JSONObject mResult =null;

    protected RequestQueue mQueue = null;

    static String URL = "http://202.31.200.123/moona/";

    String Id ="";
    String Name = "";
    String Genre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Cache cache = new DiskBasedCache(this.getCacheDir(), 1024*1024);
        Network network =  new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        tabLayout = (TabLayout) findViewById(R.id.tabLayout2);
        viewPager = (ViewPager) findViewById(R.id.viewPager2);

        Intent intent = getIntent();
        Id = intent.getStringExtra("Id");
        Name = intent.getStringExtra("Name");
        Genre = intent.getStringExtra("Genre");

        Bundle args = new Bundle();
        args.putString("Name",Name);
        args.putString("Genre",Genre);

        Bundle args2 = new Bundle();
        args2.putString("Id",Id);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        TimeTableFragment TimeTable = new TimeTableFragment();
        TimeTable.setArguments(args);
        viewPagerAdapter.addFragments(TimeTable,"시간표 보기");

        DetailFragment test = new DetailFragment();
        test.setArguments(args2);
        viewPagerAdapter.addFragments(test,"상세 정보");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        requestDetail(Id);


    }

    protected void requestDetail(String id) {
        String url = URL+"currentMovieDetail.php?no=" +id ;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        mResult = response;
                        drawList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"서버 에러", Toast.LENGTH_LONG).show();
                    }
                }
        );
        jsObjRequest.setTag(MOVIETAG);
        mQueue.add(jsObjRequest);
    }

    public void drawList(){
        try{
            JSONArray jsonMainMode = mResult.getJSONArray("list");

            JSONObject jsonChildNode = jsonMainMode.getJSONObject(0);
            String name = jsonChildNode.getString("name");
            TextView Title = (TextView) findViewById(R.id.toolbar_title);
            Title.setText(name);

            String genre = jsonChildNode.getString("genre");
            TextView Genre = (TextView) findViewById(R.id.genre);
            Genre.setText(genre);

            String date = jsonChildNode.getString("r_date");
            TextView Date = (TextView) findViewById(R.id.date);

            switch (date.substring(7, 10)) {
                //-00이 들어가면 예정으로 변경
                case "-00":
                    Date.setText(date.replace("-00", " 예정"));
                    break;
                default:
                    Date.setText(date);
                    break;
            }

            String runtime = jsonChildNode.getString("run_time");
            TextView RunTime = (TextView) findViewById(R.id.runtime);
            RunTime.setText(runtime);

            String poster = jsonChildNode.getString("thumbnail");
            ImageView Poster = (ImageView) findViewById(R.id.poster);
            Picasso.with(getApplicationContext())
                    .load(poster) // here you resize your image to whatever width and height you like
                    .into(Poster);

        } catch (JSONException | NullPointerException e){
            Toast.makeText(getApplicationContext(), "Error" + e.toString(),Toast.LENGTH_LONG).show();
            mResult = null;
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.cancelAll(MOVIETAG); //지정된 TGG의 Request를 Queue에서 취소
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        finish();
    }
}
