package kr.ac.kumoh.s20140350.movienow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;

import java.io.IOException;
import java.util.ArrayList;

//import android.graphics.Camera;

/**
 * Created by kkssh on 2017-05-14.
 */

public class ARViewActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{

    private Preview preview;
    private LinearLayout linearLayout;
    private Button closeBtn;
    private ImageView arrowImg, pinImg;
    private TextView distanceText;

    private ArrayList<TMapPoint> pathPoints;
    private TMapPoint curPoint;
    private Location destPoint;

    private float mMidPointBearing, mDestPointBearing;

    private SensorManager mSensorManager;
    private SensorEventListener sensorEventListenr;
    private TMapGpsManager gpsManager;

    private static float displayWidth = 1080.0f;
    private static float cameraAngle = 50.0f;
    private float pixelPerBearing = displayWidth/cameraAngle;

    private float pixelWidthMidPoint, pixelHeightMidPoint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        /*GPS Data 가져오기*/
        int size = 0;
        Intent intent = getIntent();
        double[] longitude = intent.getDoubleArrayExtra("longitude");
        double[] latitude = intent.getDoubleArrayExtra("latitude");
        size = intent.getIntExtra("size", 0);
        pathPoints = new ArrayList<TMapPoint>();

        Log.i("t","size : "+size);

        for (int i = 0; i < size; i++)
            pathPoints.add(new TMapPoint(latitude[i], longitude[i]));

        if(size == 0) {
            finish();
        }
        else{
            curPoint = pathPoints.get(pathPoints.size() - 1);
            TMapPoint dest = pathPoints.get(0);
            destPoint = new Location(String.valueOf(Location.CREATOR));
            destPoint.setLatitude(dest.getLatitude());
            destPoint.setLongitude(dest.getLongitude());
        }
        /*Camera View 생성*/
        linearLayout = (LinearLayout) findViewById(R.id.cameraView);
        preview = new Preview(this);
        linearLayout.addView(preview);

        /*AR layout over*/
        LayoutInflater arrowInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout arrowLinear = (LinearLayout) arrowInflater.inflate(R.layout.arrow_inflater, null);
        LinearLayout.LayoutParams arrowParamlinear =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(arrowLinear, arrowParamlinear);
        arrowImg = (ImageView)findViewById(R.id.arrow);
        distanceText = (TextView)findViewById(R.id.distanceText);

        /*AR Close Button*/
        closeBtn = (Button) findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(ARViewActivity.this,PathActivity.class);
                //startActivity(intent);
                finish();
            }
        });



        /*Pin layout over*/
        LayoutInflater poinInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout pinLinear = (LinearLayout) poinInflater.inflate(R.layout.pin_inflater, null);
        LinearLayout.LayoutParams pinParamlinear =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(pinLinear, pinParamlinear);
        pinImg = (ImageView)findViewById(R.id.pinImg);
        pinImg.setImageResource(R.drawable.poi_dot);

        /*mid point set*/
        pixelWidthMidPoint = displayWidth/2 - pinImg.getWidth()/2;
        pixelHeightMidPoint = linearLayout.getHeight()/2 - pinImg.getWidth()/2 + 250.0f;
        /*Sensor*/
        sensorEventListenr = new SensorEventListener(){
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    float azimuth = sensorEvent.values[0];       //z축
                    float pitch = sensorEvent.values[1];         //x축
                    float roll = sensorEvent.values[2];          //y축
                    float arrowZ = azimuth - mMidPointBearing;
                    float arrowY = roll;
                    float arrowX = pitch;

                    float pinX, pinY;

                    pinX = mDestPointBearing - azimuth;
                    if(pinX < -330) pinX+= 360;

                    pinY = pitch + 90.0f;
                    arrowRotate(arrowX,arrowY,arrowZ);
                    float cam = cameraAngle/2;

                    if(pinY < cam-20 && pinY > -(cam+18)) {
                        if (pinImg.getVisibility() == View.INVISIBLE)
                            pinImg.setVisibility(View.VISIBLE);
                        pinMove(pinX, pinY);
                    }
                    else
                        pinImg.setVisibility(View.INVISIBLE);
                    arrowImg.invalidate();
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(sensorEventListenr,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_NORMAL);

        /*GPS 설정, 현재위치 탐색 시작*/
        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(1000);
        gpsManager.setMinDistance(5);
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gpsManager.CloseGps();
        mSensorManager.unregisterListener(sensorEventListenr);
    }

    @Override
    public void onLocationChange(Location location) {
        curPoint.setLongitude(location.getLongitude());
        curPoint.setLatitude(location.getLatitude());
        if(updatePath()) pathPoints = drawMapPath();
        //updatePath();

        /*현위치-바로 다음 gps점 방향 및 거리계산*/
        if(pathPoints.size() > 0) {
            TMapPoint tMidPoint = pathPoints.get(pathPoints.size() - 1);
            Location midPoint = new Location(String.valueOf(Location.CREATOR));
            midPoint.setLatitude(tMidPoint.getLatitude());
            midPoint.setLongitude(tMidPoint.getLongitude());
            mMidPointBearing = location.bearingTo(midPoint);
            distanceText.setText((int)location.distanceTo(midPoint)+"m");
        }

        /*현위치-최종목적지 방향계산*/
        mDestPointBearing = location.bearingTo(destPoint);
    }

    class Preview extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder holder;
        Camera camera;

        public Preview(Context context) {
            super(context);
            holder = getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            camera = Camera.open();
            camera.stopPreview();
            Camera.Parameters parameters = camera.getParameters();
            camera.setDisplayOrientation(90);
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                camera.release();
                camera = null;
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Camera.Parameters parameters = camera.getParameters();
            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void arrowRotate(float x, float y, float z){
        float imgWidth = arrowImg.getWidth();
        float imgHeight = arrowImg.getHeight();

        /*화살표 3d 변환*/
        Matrix matrix = arrowImg.getImageMatrix();

        arrowImg.setScaleType(ImageView.ScaleType.MATRIX);
        android.graphics.Camera cam = new android.graphics.Camera();

        cam.translate(-imgHeight,imgWidth,(imgWidth+imgHeight)*1.5f);   //평행이동
        cam.rotate(x+20.0f,y,z);                                        //관측변환, 시점의 이동
        cam.getMatrix(matrix);

        matrix.preTranslate(-imgWidth,-imgHeight);
        matrix.postTranslate(imgWidth,imgHeight);

        arrowImg.setImageMatrix(matrix);
    }

    public boolean updatePath(){
        TMapPolyLine distancePolyLine = new TMapPolyLine();

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

            return true;
        }
        return false;
    }

    public ArrayList<TMapPoint> drawMapPath(){
        TMapData tMapData = new TMapData();
        final ArrayList<TMapPoint> points = new ArrayList<TMapPoint>();
        TMapPoint endPoint = pathPoints.get(0);

        tMapData.findPathDataWithType(TMapData.TMapPathType.CAR_PATH,endPoint,curPoint, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapPolyLine.setLineColor(Color.BLUE);
                points.addAll(tMapPolyLine.getLinePoint());

                for(int i=0; i<points.size(); i++)
                    Log.i("drawMapPath()","pathPoints("+i+"):"+points.get(i).toString());
            }
        });
        Log.i("drawMapPath()","Path start");
        return points;
    }

    public void pinMove(float x, float y){
        /*pin image 이동*/
        float moveX = pixelWidthMidPoint + x*pixelPerBearing;
        float moveY = pixelHeightMidPoint - y*pixelPerBearing;

        pinImg.setX(moveX);
        pinImg.setY(moveY);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ARViewActivity.this,FirstActivity.class);
        startActivity(intent);
        finish();
    }

}
