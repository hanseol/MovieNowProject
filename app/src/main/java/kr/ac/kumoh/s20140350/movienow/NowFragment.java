package kr.ac.kumoh.s20140350.movienow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class NowFragment extends Fragment implements AdapterView.OnItemClickListener{
    static String URL = "http://202.31.200.123/moona/";

    protected ArrayList<MovieInfo> mArray = new ArrayList<MovieInfo>();

    public static final String MOVIETAG = "MovieTag";
    protected JSONObject mResult =null;

    protected ListView mList;
    protected MovieAdapter mAdapter;
    protected RequestQueue mQueue = null;

    public NowFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.fragment_now,null);

        mAdapter = new MovieAdapter (getActivity(), R.layout.now_item, mArray);

        mList = (ListView)v.findViewById(R.id.nowList);
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
        String url = URL + "currentMovie.php";

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
                String genre = jsonChildNode.getString("genre");
                String date = jsonChildNode.getString("r_date");
                String grade = jsonChildNode.getString("grade");
                String poster = jsonChildNode.getString("thumbnail");
                String age = jsonChildNode.getString("rating");

                mArray.add(new MovieInfo(id, name, genre, date, grade, poster, age));
            }
        } catch (JSONException | NullPointerException e){
            Toast.makeText(getContext(), "Error" + e.toString(),Toast.LENGTH_LONG).show();
            mResult = null;
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final MovieInfo ITEM = (MovieInfo) parent.getItemAtPosition(position);

        String idStr = ITEM.getId();
        String nameStr = ITEM.getName();
        String genreStr = ITEM.getGenre();

        Intent intent = new Intent(getActivity(), SecondActivity.class);
        intent.putExtra("Name",nameStr);
        intent.putExtra("Id",idStr);
        intent.putExtra("Genre",genreStr);

        startActivity(intent);
        getActivity().finish();
    }

    static class MovieViewHolder{
        TextView txName;
        TextView txGenre;
        TextView txDate;
        TextView txGrade;
        ImageView imPoster;
        ImageView imAge;
    }

    public class MovieAdapter extends ArrayAdapter<MovieInfo> {
        public MovieAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<MovieInfo> objects) {
            super(context, resource, objects);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MovieViewHolder holder;

            if (convertView==null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.now_item, parent, false);

                holder = new MovieViewHolder();

                holder.txName = (TextView) convertView.findViewById(R.id.title);
                holder.txGenre = (TextView) convertView.findViewById(R.id.genre);
                holder.txDate = (TextView) convertView.findViewById(R.id.date);
                holder.txGrade = (TextView) convertView.findViewById(R.id.grade);
                holder.imPoster = (ImageView) convertView.findViewById(R.id.poster);
                holder.imAge = (ImageView) convertView.findViewById(R.id.age);
                convertView.setTag(holder);
            }
            else{
                holder = (MovieViewHolder) convertView.getTag();
            }

            holder.txName.setText(getItem(position).getName());
            holder.txGenre.setText(getItem(position).getGenre());
            holder.txDate.setText(getItem(position).getDate());
            holder.txGrade.setText(getItem(position).getGrade());

            Picasso.with(getContext())
                    .load(getItem(position).getPoster()) // here you resize your image to whatever width and height you like
                    .into(holder.imPoster);

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
                case "":
                case "NR":
                case "UR":
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


}
