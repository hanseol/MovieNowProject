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
import org.w3c.dom.Text;

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
    protected ArrayList<TheaterInfoTest> mArray = new ArrayList<TheaterInfoTest>();

    public static final String THEATERTAG = "TheaterTag";
    protected JSONObject mResult =null;

    protected ListView mList;
    protected TheaterAdapter mAdapter;
    protected RequestQueue mQueue = null;
    protected ImageLoader mImageLoader = null;

    ArrayList <String> address = new ArrayList<String>();
    String getMovieName;
    String getMovieGenre;

    public TimeTableFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.fragment_timetable,container, false);

        getMovieName=getArguments().getString("name");
        address= getArguments().getStringArrayList("address");
        getMovieGenre=getArguments().getString("Genre");

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

        requestTheater(getMovieName);

        return v;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void requestTheater(String n) {

        String url = URL + "TestTime.php";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mResult = response;
                        drawList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "서버 에러", Toast.LENGTH_LONG).show();
                    }
                }
        );
        jsObjRequest.setTag(THEATERTAG);
        mQueue.add(jsObjRequest);
    }

    public void drawList(){
        mArray.clear();
        try{
            JSONArray jsonMainMode = mResult.getJSONArray("list");

            for(int i = 0; i < jsonMainMode.length(); i++){
                JSONObject jsonChildNode = jsonMainMode.getJSONObject(i);

                String time = jsonChildNode.getString("time");
                String distance = jsonChildNode.getString("distance");
                String theater = jsonChildNode.getString("theater");
                String branch = jsonChildNode.getString("branch");
                Log.i("MyLog",branch);

                mArray.add(new TheaterInfoTest(time,branch,theater,distance));
            }
        } catch (JSONException | NullPointerException e){
            Toast.makeText(getContext(), "Error" + e.toString(),Toast.LENGTH_LONG).show();
            mResult = null;
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        TheaterInfoTest item = (TheaterInfoTest) parent.getItemAtPosition(position);

        String theaterImg = item.getTheater();
        String branchStr = item.getBranch();

        Bundle args = new Bundle();
        args.putString("theater",theaterImg);
        args.putString("branch",branchStr);
        args.putString("recommend_genre",getMovieGenre);

        MyAlertDialogFragment newDialogFragment = MyAlertDialogFragment.newInstance("","");
        newDialogFragment.setArguments(args);
        newDialogFragment.show(getActivity().getFragmentManager(), "dialog");
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(String s, String s2) {
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
                            Log.i("MyLog", "예매 버튼이 눌림");
                            String genre = getArguments().getString("recommend_genre");
                            String theater = getArguments().getString("theater");
                            if(genre.contains(",")){
                                int idx = genre.indexOf(",");
                                genre = genre.substring(0,idx);
                            }
                            Log.i("MyLog",genre);
                            updateReservation(genre);

                            switch(theater){
                                case "images/cgv.png" :
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.cgv.co.kr/ticket/")));
                                    break;
                                case "images/lotte.png":
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.lottecinema.co.kr/LCHS/Contents/ticketing/ticketing.aspx")));
                                    break;
                                case "images/megabox.png":
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.megabox.co.kr/?show=booking&p=step1")));
                                    break;
                            }
                        }
                    })
                    .setNegativeButton("길 안내 해주세요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("MyLog", "길안내 버튼이 눌림");
                            String branch = getArguments().getString("branch");
                            String longi ;
                            String lati;
                            switch(branch){
                                case "도서관" :
                                    Log.i("MyLog","도서관");
                                    longi = "36.145949";
                                    lati = "128.393767";

                                    Intent intent = new Intent(getActivity(),PathActivity.class);
                                    intent.putExtra("longi",longi);
                                    intent.putExtra("lati",lati);
                                    startActivity(intent);
                                    getActivity().finish();
                                    break;
                                case "본관":
                                    Log.i("MyLog","본관");
                                    longi = "36.144464";
                                    lati = "128.392862";

                                    Intent intent2 = new Intent(getActivity(),PathActivity.class);
                                    intent2.putExtra("longi",longi);
                                    intent2.putExtra("lati",lati);
                                    startActivity(intent2);
                                    getActivity().finish();
                                    break;
                                case "테니스장":
                                    Log.i("MyLog","테니스장");
                                    longi = "36.147131";
                                    lati = "128.388339";

                                    Intent intent3 = new Intent(getActivity(),PathActivity.class);
                                    intent3.putExtra("longi",longi);
                                    intent3.putExtra("lati",lati);
                                    startActivity(intent3);
                                    getActivity().finish();
                                    break;
                                case "오름관1동":
                                    Log.i("MyLog","오름관1동");
                                    longi = "36.147219";
                                    lati = "128.391002";

                                    Intent intent4 = new Intent(getActivity(),PathActivity.class);
                                    intent4.putExtra("longi",longi);
                                    intent4.putExtra("lati",lati);
                                    startActivity(intent4);
                                    getActivity().finish();
                                    break;
                                case "대운동장":
                                    Log.i("MyLog","대운동장");
                                    longi = "36.146242";
                                    lati = "128.387590";

                                    Intent intent5 = new Intent(getActivity(),PathActivity.class);
                                    intent5.putExtra("longi",longi);
                                    intent5.putExtra("lati",lati);
                                    startActivity(intent5);
                                    getActivity().finish();
                                    break;
                                case "학생회관":
                                    Log.i("MyLog","학생회관");
                                    longi = "36.144613";
                                    lati = "128.394014";

                                    Intent intent6 = new Intent(getActivity(),PathActivity.class);
                                    intent6.putExtra("longi",longi);
                                    intent6.putExtra("lati",lati);
                                    startActivity(intent6);
                                    getActivity().finish();
                                    break;
                            }
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
        NetworkImageView imTheater;
        TextView txBranch;
    }

    public class TheaterAdapter extends ArrayAdapter<TheaterInfoTest> {
        public TheaterAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<TheaterInfoTest> objects) {
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
                holder.imTheater = (NetworkImageView) convertView.findViewById(R.id.theater);
                holder.txBranch = (TextView) convertView.findViewById(R.id.branch);

                convertView.setTag(holder);
            }
            else{
                holder = (TheaterViewHolder) convertView.getTag();
            }

            holder.txTime.setText(getItem(position).getTime());
            holder.txDistance.setText(getItem(position).getDistance());
            holder.imTheater.setImageUrl(URL + getItem(position).getTheater(),mImageLoader);
            holder.txBranch.setText(getItem(position).getBranch());
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
