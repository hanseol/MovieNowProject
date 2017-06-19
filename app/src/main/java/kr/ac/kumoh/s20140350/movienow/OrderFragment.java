package kr.ac.kumoh.s20140350.movienow;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;


/**
 * A simple {@link Fragment} subclass.
 */
public class OrderFragment extends Fragment implements AdapterView.OnItemClickListener{

    static String URL = "http://202.31.200.123/moona/";

    protected ArrayList<MovieInfo> mArray = new ArrayList<MovieInfo>();

    public static final String MOVIETAG = "MovieTag";
    protected JSONObject mResult =null;
    ArrayList<TMapPoint> points;
    protected ListView mList;
    protected MovieAdapter mAdapter;
    protected RequestQueue mQueue = null;

    private int[] distance;
    private ArrayList<String> ad;

    private TMapPoint curPoint;

    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.fragment_order,null);

        curPoint = new TMapPoint(35.87120202, 128.59608985);

        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);     //LocationManager 생성
        OrderFragment.GPSListener gpsListener = new OrderFragment.GPSListener();                                                //GPSListener 생성
        long minTime = 1000;

        float minDistance = 0;

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);

        mAdapter = new MovieAdapter (getActivity(), R.layout.order_item, mArray);

        mList = (ListView)v.findViewById(R.id.orderList);
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
        String url = URL + "currentOrderMovie.php";

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

        ad = searchTheater();

        int size = ad.size();
        double[] longitude = new double[size];
        double[] latitude = new double[size];
        for(int i=0; i<size;i++){
            latitude[i] = points.get(i).getLatitude();
            longitude[i] = points.get(i).getLongitude();
        }
//        Log.i("MyLog","Order ad size : "+ad.size());
//        for(int i=0;i<ad.size();i++)
//            Log.i("MyLog", "Order address : " + ad.get(i));

        final MovieInfo ITEM = (MovieInfo) parent.getItemAtPosition(position);

        String idStr = ITEM.getId();
        String nameStr = ITEM.getName();
        String genreStr = ITEM.getGenre();

        Intent intent = new Intent(getActivity(), SecondActivity.class);
        intent.putExtra("Name",nameStr);
        intent.putExtra("Id",idStr);
        intent.putExtra("Genre",genreStr);
        intent.putStringArrayListExtra("Address",ad);
        intent.putExtra("Distance",distance);
        intent.putExtra("long",longitude);
        intent.putExtra("lati",latitude);

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
                convertView = getActivity().getLayoutInflater().inflate(R.layout.order_item, parent, false);

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
                default:
                    holder.imAge.setImageResource(R.drawable.movie_play_level_undefine);
                    break;
            }
            return convertView;
        }
    }

    public ArrayList<String> searchTheater() {
        final TMapData tmapdata = new TMapData();
        ad = new ArrayList<String>();

        final Location p1 = new Location(String.valueOf(Location.CREATOR));
        final Location p2 = new Location(String.valueOf(Location.CREATOR));

        p1.setLatitude(curPoint.getLatitude()); p1.setLongitude(curPoint.getLongitude());

        tmapdata.findAroundNamePOI(curPoint, "영화관",1,100, new TMapData.FindAroundNamePOIListenerCallback() {
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> poiItem) {
                if(poiItem == null) {
                    Log.i("MyLog","findAroundNamePOI() : empty poi");
                    return;
                }
                int poiSize = poiItem.size();
                distance = new int[poiSize];
                ArrayList<TMapPoint> points = new ArrayList<TMapPoint>(poiSize);

                //TODO : test code, marker를 제외한 poiItem Arraylist만 가지고 주소로 변환하여 DB에서 영화관 찾기.
                for(int i = 0;i < poiSize; i++){
                    TMapPOIItem item = poiItem.get(i);
                    points.add(i,poiItem.get(i).getPOIPoint());
                }

                /*중복 제거*/
                for(int i=0;i<poiSize-1;i++){
                    TMapPoint tmp1, tmp2;
                    tmp1 = points.get(i);
                    tmp2 = points.get(i+1);
                    if(tmp1.getLatitude() == tmp2.getLatitude() && tmp1.getLongitude() == tmp2.getLongitude()) {
                        points.remove(i--);
                        poiSize--;
                    }
                }

                for(int i=0; i<points.size(); i++){
                    try {
                        /*주소*/
                        ad.add(tmapdata.convertGpsToAddress(points.get(i).getLatitude(),points.get(i).getLongitude()));
                       /*거리계산*/
                        p2.setLatitude(points.get(i).getLatitude()); p2.setLongitude(points.get(i).getLongitude());
                        distance[i] = (int)p1.distanceTo(p2);
//                        Log.i("MyLog","Order search address : "+tmapdata.convertGpsToAddress(points.get(i).getLatitude(),points.get(i).getLongitude()));
//                        Log.i("MyLog","Order search distance : "+distance[i]+"m");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        SystemClock.sleep(2000);
        return ad;
    }

    //1106 GPS listener 구현
    private class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            curPoint.setLatitude(latitude);
            curPoint.setLongitude(longitude);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.cancelAll(MOVIETAG); //지정된 TGG의 Request를 Queue에서 취소
        }
    }
}
