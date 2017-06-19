package kr.ac.kumoh.s20140350.movienow;

import android.content.Intent;
import android.os.Bundle;
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

public class SecondFutureActivity extends AppCompatActivity {

    public static final String MOVIETAG = "MovieTag";
    protected JSONObject mResult =null;

    protected RequestQueue mQueue = null;

    static String URL = "http://202.31.200.123/moona/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondfuture);

        Cache cache = new DiskBasedCache(this.getCacheDir(), 1024*1024);
        Network network =  new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        Intent intent = getIntent();

        final String Id = intent.getStringExtra("Id");

        requestDetail(Id);
    }

    protected void requestDetail(String id) {
        String url = URL+"futureMovieDetail.php?no=" +id ;
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
            TextView Title = (TextView)findViewById(R.id.toolbar_title);
            Title.setText(name);

            String genre = jsonChildNode.getString("genre");
            TextView Genre = (TextView)findViewById(R.id.genre);
            Genre.setText(genre);

            String date = jsonChildNode.getString("r_date");
            TextView Date = (TextView)findViewById(R.id.date);

            switch(date.substring(7,10)){
                //-00이 들어가면 예정으로 변경
                case "-00" :
                    Date.setText(date.replace("-00"," 예정"));
                    break;
                default:
                    Date.setText(date);
                    break;
            }

            String runtime = jsonChildNode.getString("run_time");
            TextView RunTime = (TextView)findViewById(R.id.runtime);
            RunTime.setText(runtime);

            String poster = jsonChildNode.getString("thumbnail");
            ImageView Poster = (ImageView) findViewById(R.id.poster);
            switch(poster){
                case "":
                    Poster.setImageResource(R.drawable.movie_ready);
                    break;
                default :
                    Picasso.with(getApplicationContext())
                            .load(poster) // here you resize your image to whatever width and height you like
                            .into(Poster);
                    break;
            }

            String story = jsonChildNode.getString("story");
            TextView Story = (TextView)findViewById(R.id.story);
            Story.setText(story);

            String director = jsonChildNode.getString("director");
            TextView Director = (TextView)findViewById(R.id.director);
            Director.setText(director);

            String actor = jsonChildNode.getString("actor");
            TextView Actor = (TextView)findViewById(R.id.actor);
            Actor.setText(actor);

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
        super.onBackPressed();
    }
}
