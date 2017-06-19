package kr.ac.kumoh.s20140350.movienow;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

import com.android.volley.AuthFailureError;
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
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kr.ac.kumoh.s20140350.movienow.R.drawable.marker;

/**
 * Created by SEOL on 2017-04-20.
 */

public class TimeTableFragment extends Fragment implements AdapterView.OnItemClickListener{

    static String URL = "http://202.31.200.123/moona/";
    protected ArrayList<TheaterInfo> mArray = new ArrayList<TheaterInfo>();

    public static final String THEATERTAG = "TheaterTag";
    protected JSONObject mResult =null;

    protected ListView mList;
    protected TheaterAdapter mAdapter;
    protected RequestQueue mQueue = null;
    protected ImageLoader mImageLoader = null;

    ArrayList <String> address = new ArrayList<String>();
    private int[] getDistance;
    String getMovieName;
    String getMovieGenre;
    double[] longitude;
    double[] latitude;

    public TimeTableFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.fragment_timetable,container, false);

        getMovieName=getArguments().getString("Name");
        getMovieGenre=getArguments().getString("Genre");
        getDistance = getArguments().getIntArray("Distance");
        address= getArguments().getStringArrayList("Address");
        longitude = getArguments().getDoubleArray("Long");
        latitude = getArguments().getDoubleArray("Lati");

        mAdapter = new TheaterAdapter(getActivity(), R.layout.timetable_item, mArray);

        mList = (ListView)v.findViewById(R.id.TimetableList);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);

        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024*1024);
        Network network =  new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        mImageLoader = new ImageLoader(mQueue,
                new LruBitmapCache(LruBitmapCache.getCacheSize(getActivity())));

        for(int test=0;test<address.size();test++)
            requestTheater(test);

        return v;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void requestTheater(int testing) {
        final int tt = testing;
        String url = "";
        try {
            Log.i("MyLog", "TimeTableFragment :" +testing +"="+ address.get(testing));

            url = URL + "TheaterToTime.php?addr="
                    + URLEncoder.encode(address.get(testing), "utf-8") + "&name=" +
                    URLEncoder.encode(getMovieName, "utf-8");
        } catch (Exception e) {
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mResult = response;
                        drawList(tt);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getActivity(), "서버 에러", Toast.LENGTH_LONG).show();
                    }
                }
        );

        jsObjRequest.setTag(THEATERTAG);
        mQueue.add(jsObjRequest);
    }


    public ArrayList<TheaterInfo> drawList(int Test){
        try {
            JSONArray jsonMainMode = mResult.getJSONArray("list");
            final int numberOfItemsInResp = jsonMainMode.length();

            for (int j = 0; j < numberOfItemsInResp; j++) {
                JSONObject jsonChildNode = jsonMainMode.getJSONObject(j);

                String time = jsonChildNode.getString("time");
                String url = jsonChildNode.getString("reservation_url");
                String theater = jsonChildNode.getString("theater_no");
                Log.i("MyLog","drawList(1) : "+Test +"="+theater);
                mArray.add(new TheaterInfo(time, url, theater));
            }
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(getContext(), "Error" + e.toString(), Toast.LENGTH_LONG).show();
            mResult = null;
        }
        mAdapter.notifyDataSetChanged();
        return mArray;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        TheaterInfo item = (TheaterInfo) parent.getItemAtPosition(position);

        String urlStr = item.getUrl();

        Bundle args = new Bundle();
        args.putString("recommend_genre",getMovieGenre);
        args.putString("url",urlStr);
        args.putDoubleArray("Long",longitude);
        args.putDoubleArray("Lati",latitude);

        MyAlertDialogFragment newDialogFragment = MyAlertDialogFragment.newInstance("");
        newDialogFragment.setArguments(args);
        newDialogFragment.show(getActivity().getFragmentManager(), "dialog");
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(String s) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            return frag;
        }
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setIcon(marker)
                    .setTitle("선택하세요")
                    .setMessage("길 안내 : 위치확인 및 AR 길 안내\n" + "예매하기 : 해당 영화 사이트 연결")
                    .setPositiveButton("예매할래요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.i("MyLog", "예매 버튼이 눌림");
                            String genre = getArguments().getString("recommend_genre");
                            String url = getArguments().getString("url");

                            if(genre.contains(",")){
                                int idx = genre.indexOf(",");
                                genre = genre.substring(0,idx);
                            }
                            updateReservation(genre);

                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://"+url)));
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("길 안내 해주세요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("MyLog", "길안내 버튼이 눌림");

//                            startActivity(new Intent(getActivity(),PathActivity.class));
//                            getActivity().finish();
                        }
                    })
                    .create();
        }

        private void updateReservation(String genre)
        {//필요할 경우 매개변수 생성해도 상관 없음
            final String userID=SharedPrefManager.getInstance(getActivity()).getUserID();
            final String genreName=genre;

            StringRequest stringRequest= new StringRequest(Request.Method.POST, Constants.URL_UPDATERESERVE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try
                            {
                                JSONObject obj=new JSONObject(response);
                                //응답으로 넘어온 데이터 값 중 success field의 값을 비교해서
                                if(obj.getBoolean("success"))//성공했으면
                                {
                                    Log.i("MyLog","SUCCESS");
                                }
                                else
                                {
                                    int rescode=obj.getInt("rescode");
                                    switch (rescode){
                                        case 1 :
                                            Log.i("MyLog","SUCCESS");
                                            break;
                                        case 2:
                                            // Toast.makeText(getActivity().getApplicationContext(), "예매 기록 업데이트 중 문제가 발생했습니다", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 3:
                                            // Toast.makeText(getActivity().getApplicationContext(), "예매 기록 검색 중 문제가 발생했습니다", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            }
                            catch(JSONException e)
                            {
                                Log.i("MyLog","FAIL");
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            //progressDialog.hide();
                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })//parameter list end
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params=new HashMap<>();
                    //만약 php에서 문제가 생기면 이 변수와 제대로 짝을 맞추었는지 확인할 것
                    params.put("userID", userID);
                    params.put("genreName", genreName);
                    return params;
                }
            };
            RequestHandler.getInstance(getActivity()).addToRequestQueue(stringRequest);
        }
    }

    static class TheaterViewHolder{
        TextView txTime;
        TextView txDistance;
        ImageView imTheater;
    }

    public class TheaterAdapter extends ArrayAdapter<TheaterInfo> {
        public TheaterAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<TheaterInfo> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TheaterViewHolder holder;

            if (convertView==null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.timetable_item, parent, false);

                holder = new TheaterViewHolder();

                holder.txTime = (TextView) convertView.findViewById(R.id.time);
                holder.txDistance = (TextView) convertView.findViewById(R.id.distance);
                holder.imTheater = (ImageView) convertView.findViewById(R.id.theater);

                convertView.setTag(holder);
            }
            else{
                holder = (TheaterViewHolder) convertView.getTag();
            }

            holder.txTime.setText(getItem(position).getTime());

            switch (getItem(position).getTheater().substring(0,1)){
                case "1":
                    holder.imTheater.setImageResource(R.drawable.cgv);
                    break;
                case "2":
                    holder.imTheater.setImageResource(R.drawable.lotte);
                    break;
                case "3":
                    holder.imTheater.setImageResource(R.drawable.megabox);
                    break;
            }

            return convertView;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.cancelAll(THEATERTAG); //지정된 TGG의 Request를 Queue에서 취소
        }
    }
}
