package kr.ac.kumoh.s20140350.movienow;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.skp.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class FirstActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mNdefExchangeFilters;
    private boolean mResumed = false;

    private ProgressDialog progressDialog;

    private BackPressCloseHandler backPressCloseHandler;

    TMapView tMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        /*mapView 생성 및 KEY등록*/
        tMapView = new TMapView(this);
        tMapView.setSKPMapApiKey("66bee465-d863-36c4-8740-6e3c57b359c3");

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        NowFragment Now = new NowFragment();
        viewPagerAdapter.addFragments(Now,"현재상영작");

        OrderFragment Order = new OrderFragment();
        viewPagerAdapter.addFragments(Order,"평점순");

        viewPagerAdapter.addFragments(new FutureFragment(),"개봉예정작");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("잠시만 기다려 주세요....");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false);
        navigationView.addHeaderView(headerView);
        TextView userInfo = (TextView)headerView.findViewById(R.id.userID);

        //로그인 되어있을 경우
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {

            userInfo.setText(SharedPrefManager.getInstance(this).getUserID()+"님 환영합니다");
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.navigation_with_logout);

        }
        else {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.navigation_with_login);
        }

        navigationView.setNavigationItemSelectedListener(this);

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            getApplicationContext().startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //nfc 관련 인텐트 필터 생성
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("application/kr.ac.kumoh.s20140350.movienow");
        } catch (IntentFilter.MalformedMimeTypeException e) { }
        mNdefExchangeFilters = new IntentFilter[] { ndefDetected };

        backPressCloseHandler = new BackPressCloseHandler(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            backPressCloseHandler.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_signup) {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        } else if (id == R.id.nav_change_ref) {
            startActivity(new Intent(this, ChangeRefActivity.class));
            finish();
        } else if (id == R.id.nav_change_password) {
            startActivity(new Intent(this, checkPasswordActivity.class));
            finish();
        } else if (id == R.id.nav_unsign) {
            startActivity(new Intent(this, withdrawReadyActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            userLogout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void userLogout()
    {
        final String userID= SharedPrefManager.getInstance(this).getUserID();

        StringRequest stringRequest= new StringRequest(
                Request.Method.POST,
                Constants.URL_LOGOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            JSONObject obj=new JSONObject(response);
                            //응답으로 넘어온 데이터 값 중 success field의 값을 비교해서
                            if(obj.getBoolean("success"))//성공했으면
                            {
                                SharedPrefManager.getInstance(getApplicationContext()).logout();
                                Toast.makeText(getApplicationContext(), "정상적으로 로그아웃되었습니다", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), FirstActivity.class));
                                finish();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "로그아웃에 문제가 생겼습니다. 다시 시도해 보세요", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch(JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })//parameter list end
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                //만약 php에서 문제가 생기면 이 변수와 제대로 짝을 맞추었는지 확인할 것
                params.put("userID", userID);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //앱이 실행될 때 NFC 어댑터 활성화
        mResumed = true;
        //앱이 꺼져있을 때 찍으면 실행
        // nfc 태깅에 의해서 앱이 실행 될때 이부분 수행
        Log.e("nfctag", "onResume");
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            changeModeNormalSilent();
        }
    }

    @Override
    protected void onPause() {
        //앱이 종료될 때 NFC 어댑터를 비활성화
        super.onPause();
        Log.e("nfctag", "onPause");
        mResumed = false;
        mNfcAdapter.disableForegroundNdefPush(this);
    }

    private void changeModeNormalSilent()
    {

        AudioManager am;
        am=(AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        switch (am.getRingerMode())
        {
            //벨소리거나 무음 모드로 변환
            case AudioManager.RINGER_MODE_NORMAL:
                Toast.makeText(getApplicationContext(), "무음 모드로 변경되었습니다", Toast.LENGTH_SHORT).show();
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
            case AudioManager.RINGER_MODE_SILENT:
                Toast.makeText(getApplicationContext(), "소리 모드로 변경되었습니다", Toast.LENGTH_SHORT).show();
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
        }
    }

}
