package kr.ac.kumoh.s20140350.movienow;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by DH on 2017-05-01.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG="fcmexamplemessage";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        //nofityUser(remoteMessage.getFrom(), remoteMessage.getNotification().getBody());
        nofityUser(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
    }

    public void nofityUser(String from, String notification) {
        MoonaNotificationManager moonaNotificationManager=new MoonaNotificationManager(getApplicationContext());
        moonaNotificationManager.showNotification(from, notification, new Intent(getApplicationContext(), FirstActivity.class));
    }
}
