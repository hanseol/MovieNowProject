package kr.ac.kumoh.s20140350.movienow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import org.json.JSONObject;

/**
 * Created by SEOL on 2017-04-13.
 */

public class SecondFutureActivity extends AppCompatActivity {

    public static final String MOVIETAG = "MovieTag";

    protected RequestQueue mQueue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondfuture);

        Cache cache = new DiskBasedCache(this.getCacheDir(), 1024*1024);
        Network network =  new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        Intent intent = getIntent();

        final String Id = intent.getStringExtra("Id");

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
        super.onBackPressed();
    }
}
