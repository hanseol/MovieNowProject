package kr.ac.kumoh.s20140350.movienow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import java.util.ArrayList;

public class PathActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    private LinearLayout mapView;
    private TMapView tMapView;
    private TMapGpsManager gpsManager;

    private TMapMarkerItem markerItem;
    private TMapPoint curPoint, endPoint;
    private double curPointLongitude = 126.99874675273895, curPointLatitude = 37.541080713272095;
    private Bitmap poiPin1;

    private TMapData tMapData;

    private Button arBtn;
    private ArrayList<TMapPoint> pathPoints;
    private boolean drawMap = false;

    /*test*/
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_path);

    Intent intent = getIntent();

    String longitude = intent.getStringExtra("longi");
    String latitude = intent.getStringExtra("lati");
    double Longitude = Double.parseDouble(longitude);
    double Latitude = Double.parseDouble(latitude);

    mapView = (LinearLayout)findViewById(R.id.mapView);

        /*mapView 생성 및 KEY등록*/
    tMapView = new TMapView(this);
    tMapView.setSKPMapApiKey("66bee465-d863-36c4-8740-6e3c57b359c3");

        /*mapView 설정*/
    tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
    tMapView.setIconVisibility(true);
    tMapView.setZoomLevel(16);
    tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
    tMapView.setCompassMode(true);
    tMapView.setSightVisible(true);
    tMapView.setTrackingMode(true);

        /*GPS 설정, 현재위치 탐색 시작*/
    gpsManager = new TMapGpsManager(this);
    gpsManager.setMinTime(1000);
    gpsManager.setMinDistance(5);
    gpsManager.setProvider(gpsManager.GPS_PROVIDER);
    gpsManager.OpenGps();

        /*Overlay 설정*/
    //TODO : 디지털관 -> 목적지로 변경
    //endPoint = new TMapPoint(37.566474,126.985022);       //test, skt
    endPoint = new TMapPoint(Longitude,Latitude);       //test, 본관
    //endPoint = new TMapPoint(36.146852,128.392342);       //test, 글로벌관
    markerItem = new TMapMarkerItem();
    markerItem.setTMapPoint(endPoint);
    markerItem.setName("디지털관");
    markerItem.setVisible(TMapMarkerItem.HIDDEN);
    poiPin1 = BitmapFactory.decodeResource(getResources(), R.drawable.poi_dot);

    tMapView.addMarkerItem("디지털관", markerItem);

    pathPoints = new ArrayList<TMapPoint>();

    arBtn = (Button)findViewById(R.id.btn4);
    arBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //if(!drawMap) pathPoints= drawMapPath();
            Intent intent = new Intent(PathActivity.this,ARViewActivity.class);

            int size = pathPoints.size();
            double[] longitude = new double[size];
            double[] latitude = new double[size];

            for(int i=0;i<size;i++){
                longitude[i] = pathPoints.get(i).getLongitude();
                latitude[i] = pathPoints.get(i).getLatitude();
            }
            intent.putExtra("longitude",longitude);
            intent.putExtra("latitude",latitude);
            intent.putExtra("size",size);

            startActivity(intent);
            //finish();
        }
    });
        /*출발지,도착지 아이콘 설정*/
    tMapView.setTMapPathIcon(poiPin1, null);
    mapView.addView(tMapView);

}

    @Override
    public void onLocationChange(Location location) {
        curPointLongitude = location.getLongitude();
        curPointLatitude = location.getLatitude();
        tMapView.setCenterPoint(curPointLongitude,curPointLatitude);
        tMapView.setLocationPoint(curPointLongitude,curPointLatitude);

        curPoint = tMapView.getCenterPoint();
        if(drawMap)
            updatePath();
        else
            pathPoints = drawMapPath();
    }

    public void updatePath(){
        if(!drawMap) return;
        else if(drawMap && pathPoints.size() == 0) return;

        TMapPolyLine distancePolyLine = new TMapPolyLine();
        TMapPolyLine updatePolyLine = new TMapPolyLine();
        int end = pathPoints.size()-1;
        double distance;

        distancePolyLine.addLinePoint(curPoint);
        distancePolyLine.addLinePoint(pathPoints.get(end));

        Log.i("updatePath()","Distance : "+distancePolyLine.getDistance());

        distance = distancePolyLine.getDistance();
        if(distance < 15.0)
            pathPoints.remove(end);
        else if(distance > 70){
            Toast.makeText(this,"경로 재탐색",Toast.LENGTH_LONG).show();
            Log.i("updatePath()","경로 재탐색");
            pathPoints = drawMapPath();
        }

        for(int i = 0; i< pathPoints.size(); i++)
            updatePolyLine.addLinePoint(pathPoints.get(i));
        updatePolyLine.addLinePoint(curPoint);
        updatePolyLine.setLineColor(Color.BLUE);

        tMapView.addTMapPath(updatePolyLine);
    }

    public ArrayList<TMapPoint> drawMapPath(){
        tMapData = new TMapData();
        drawMap = true;
        final ArrayList<TMapPoint> points = new ArrayList<TMapPoint>();

        tMapData.findPathDataWithType(TMapData.TMapPathType.CAR_PATH,endPoint,curPoint, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapPolyLine.setLineColor(Color.BLUE);
                points.addAll(tMapPolyLine.getLinePoint());

                for(int i=0; i<points.size(); i++)
                    Log.i("drawMapPath()","pathPoints("+i+"):"+points.get(i).toString());

                tMapView.addTMapPath(tMapPolyLine);
            }
        });
        Log.i("drawMapPath()","Path start");
        return points;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        finish();
    }
}
