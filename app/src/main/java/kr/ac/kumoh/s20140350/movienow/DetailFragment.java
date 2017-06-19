package kr.ac.kumoh.s20140350.movienow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SEOL on 2017-04-20.
 */

public class DetailFragment extends Fragment {
    public static final String MOVIETAG = "MovieTag";
    protected JSONObject mResult =null;

    static String URL = "http://202.31.200.123/moona/";

    protected RequestQueue mQueue = null;


    public DetailFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_detail, null);

        String Id = getArguments().getString("Id");

        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024*1024);
        Network network =  new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        requestDetail(Id,v);

        return v;
    }

    protected void requestDetail(String id, final View v) {
        String url = URL+"currentMovieContent.php?no=" +id ;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        mResult = response;
                        drawList(v);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),"서버 에러", Toast.LENGTH_LONG).show();
                    }
                }
        );
        jsObjRequest.setTag(MOVIETAG);
        mQueue.add(jsObjRequest);
    }

    public void drawList(View v){
        try{

            JSONArray jsonMainMode = mResult.getJSONArray("list");

            JSONObject jsonChildNode = jsonMainMode.getJSONObject(0);

            String story = jsonChildNode.getString("story");
            TextView Story = (TextView)v.findViewById(R.id.story);
            Story.setText(story);

            String director = jsonChildNode.getString("director");
            TextView Director = (TextView)v.findViewById(R.id.director);
            Director.setText(director);

            String actor = jsonChildNode.getString("actor");
            TextView Actor = (TextView)v.findViewById(R.id.actor);
            Actor.setText(actor);

        } catch (JSONException | NullPointerException e){
            Toast.makeText(getContext(), "Error" + e.toString(),Toast.LENGTH_LONG).show();
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
}
