package kr.ac.kumoh.s20140350.movienow;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
/**
 * Created by SEOL on 2017-04-13.
 */

public class SecondActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    public static final String MOVIETAG = "MovieTag";
    protected RequestQueue mQueue = null;

    String Id ="";
    String Name = "";
    String Genre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Intent intent = getIntent();

        Cache cache = new DiskBasedCache(this.getCacheDir(), 1024*1024);
        Network network =  new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        tabLayout = (TabLayout) findViewById(R.id.tabLayout2);
        viewPager = (ViewPager) findViewById(R.id.viewPager2);

        Id = intent.getStringExtra("Id");
        Name = intent.getStringExtra("Name");
        Genre = intent.getStringExtra("Genre");


        Bundle args = new Bundle();
        args.putString("Name",Name);
        args.putString("Genre",Genre);

        Bundle args2 = new Bundle();
        args2.putString("Id",Id);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        TimeTableFragment TimeTable = new TimeTableFragment();
        TimeTable.setArguments(args);
        viewPagerAdapter.addFragments(TimeTable,"시간표 보기");

        DetailFragment test = new DetailFragment();
        test.setArguments(args2);
        viewPagerAdapter.addFragments(test,"상세 정보");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

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
        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        finish();
    }
}
