package kr.ac.kumoh.s20140350.movienow;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**
 * Created by DH on 2017-05-01.
 */

public class MoonaNotificationManager {
    private Context ctx;
    public static final int NOTIFICATION_ID=899;

    public MoonaNotificationManager(Context ctx)
    {
        this.ctx=ctx;
    }

    public void showNotification(String from, String notification, Intent intent)
    {
        PendingIntent pendingIntent=PendingIntent.getActivity(
                ctx,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder=new NotificationCompat.Builder(ctx);
        Notification mNotification=builder.setSmallIcon(R.mipmap.my_ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(from)
                .setContentText(notification)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.my_ic_launcher))
                .build();

        mNotification.flags |=Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager=(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mNotification);
    }
}
