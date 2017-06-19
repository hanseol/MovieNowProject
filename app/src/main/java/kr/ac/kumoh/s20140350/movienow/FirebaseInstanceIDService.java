package kr.ac.kumoh.s20140350.movienow;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by DH on 2017-05-01.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {


    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e("myfirebaseID", "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        storeToken(refreshedToken);
        //sendRegistrationToServer(refreshedToken);
    }

    private void storeToken(String token)
    {
        SharedPrefManager.getInstance(getApplicationContext()).storeToken(token);
    }
}