package kr.ac.kumoh.s20140350.movienow;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class FutureFragment extends Fragment implements AdapterView.OnItemClickListener{

    static String URL = "http://202.31.200.123/moona/";

    protected ArrayList<FutureMovieInfo> mArray = new ArrayList<FutureMovieInfo>();

    public static final String MOVIETAG = "MovieTag";
    protected JSONObject mResult =null;

    protected ListView mList;
    protected MovieAdapter mAdapter;
    protected RequestQueue mQueue = null;

    public FutureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.fragment_future,null);

        mAdapter = new MovieAdapter(getActivity(), R.layout.future_item, mArray);

        mList = (ListView)v.findViewById(R.id.FutureList);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);

        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024*1024);
        Network network =  new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        requestMovie();

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void requestMovie() {
        String url = URL+"futureMovie.php";

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
                        Toast.makeText(getActivity(),"서버 에러", Toast.LENGTH_LONG).show();
                    }
                }
        );
        jsObjRequest.setTag(MOVIETAG);
        mQueue.add(jsObjRequest);
    }

    public void drawList(){
        mArray.clear();
        try{
            JSONArray jsonMainMode = mResult.getJSONArray("list");

            for(int i = 0; i < jsonMainMode.length(); i++){
                JSONObject jsonChildNode = jsonMainMode.getJSONObject(i);

                String id = jsonChildNode.getString("no");
                String name = jsonChildNode.getString("name");
                String date = jsonChildNode.getString("r_date");
                String genre = jsonChildNode.getString("genre");
                String poster = jsonChildNode.getString("thumbnail");
                String age=jsonChildNode.getString("rating");

                mArray.add(new FutureMovieInfo(id,name, date, genre, poster, age));
            }
        } catch (JSONException | NullPointerException e){
            Toast.makeText(getContext(), "Error" + e.toString(),Toast.LENGTH_LONG).show();
            mResult = null;
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        FutureMovieInfo item = (FutureMovieInfo) parent.getItemAtPosition(position);

        String idStr = item.getID();
        Intent intent = new Intent(getActivity(), SecondFutureActivity.class);
        intent.putExtra("Id",idStr);

        startActivity(intent);

    }

    public class FutureMovieInfo {
        String id;
        String name;
        String date;
        String genre;
        String poster;
        String age;

        public FutureMovieInfo(String id, String name, String date ,String genre, String poster, String age) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.genre = genre;
            this.poster = poster;
            this.age = age;
        }

        public String getID() {return id;}
        public String getName() {
            return name;
        }
        public String getDate() {
            return date;
        }
        public String getGenre() {
            return genre;
        }
        public String getPoster() { return poster; }
        public String getAge() {
            return age;
        }
    }

    static class MovieViewHolder{
        TextView txName;
        TextView txDday;
        TextView txDate;
        TextView txGenre;
        ImageView imPoster;
        ImageView imAge;
    }

    public class MovieAdapter extends ArrayAdapter<FutureMovieInfo> {
        public MovieAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<FutureMovieInfo> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MovieViewHolder holder;

            if (convertView==null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.future_item, parent, false);

                holder = new MovieViewHolder();

                holder.txName = (TextView) convertView.findViewById(R.id.title);
                holder.txDday = (TextView) convertView.findViewById(R.id.leftday);
                holder.txDate = (TextView) convertView.findViewById(R.id.date);

                holder.txGenre = (TextView) convertView.findViewById(R.id.genre);
                holder.imPoster = (ImageView) convertView.findViewById(R.id.poster);
                holder.imAge = (ImageView) convertView.findViewById(R.id.age);
                convertView.setTag(holder);
            }
            else{
                holder = (FutureFragment.MovieViewHolder) convertView.getTag();
            }

            holder.txName.setText(getItem(position).getName());

            switch(getItem(position).getDate().substring(7,10)){
                //-00이 들어가면 예정으로 변경
                case "-00" :
                    holder.txDday.setText("?");
                    holder.txDate.setText(getItem(position).getDate().replace("-00"," 예정"));
                    break;
                default:
                    holder.txDday.setText(Dday(getItem(position).getDate()));
                    holder.txDate.setText(getItem(position).getDate());
                    break;
            }

            holder.txGenre.setText(getItem(position).getGenre());

            switch(getItem(position).getPoster()){
                case "":
                    holder.imPoster.setImageResource(R.drawable.movie_ready);
                    break;
                default :
                    Picasso.with(getContext())
                            .load(getItem(position).getPoster()) // here you resize your image to whatever width and height you like
                            .into(holder.imPoster);
                    break;
            }

            switch (getItem(position).getAge()){
                case "전체 관람가":
                case "G":
                    holder.imAge.setImageResource(R.drawable.movie_play_level_all);
                    break;
                case "12세 관람가":
                case "PG" :
                case "PG-13" :
                    holder.imAge.setImageResource(R.drawable.movie_play_level_12);
                    break;
                case "15세 관람가":
                    holder.imAge.setImageResource(R.drawable.movie_play_level_15);
                    break;
                case "청소년 관람불가":
                case "R" :
                case "NC-17" :
                    holder.imAge.setImageResource(R.drawable.movie_play_level_19);
                    break;
                default:
                    holder.imAge.setImageResource(R.drawable.movie_play_level_undefine);
                    break;
            }
            return convertView;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.cancelAll(MOVIETAG); //지정된 TGG의 Request를 Queue에서 취소
        }
    }

    public static String Dday(String mday){

        if (mday == null) return "";
        mday = mday.trim();
        int first = mday.indexOf("-");
        int last = mday.lastIndexOf("-");
        int year = Integer.parseInt(mday.substring(0,first));
        int month = Integer.parseInt(mday.substring(first+1,last));
        int day = Integer.parseInt(mday.substring(last +1, mday.length()));

        GregorianCalendar cal = new GregorianCalendar();
        long currentTime = cal.getTimeInMillis() / (1000*60*60*24);
        cal.set(year,month -1,day);
        long birthTime = cal.getTimeInMillis() / (1000*60*60*24);
        String interval = Long.toString(birthTime - currentTime);

        return  interval;
    }
}
