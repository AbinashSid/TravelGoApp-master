package com.example.pallab.travelgoapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.pallab.travelgoapp.CustomerCall;
import com.example.pallab.travelgoapp.Home;
import com.example.pallab.travelgoapp.R;
import com.example.pallab.travelgoapp.RateActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.logging.Handler;

/**
 * Created by PALLAB on 3/13/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification().getTitle().equals("Cancel")){

            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

            handler.post(new Runnable(){
                @Override
                public void run() {

                    Toast.makeText(MyFirebaseMessaging.this, ""+remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();


                }
            });

        }else  if (remoteMessage.getNotification().getTitle().equals("DropOff"))
        {
            openRateActivity(remoteMessage.getNotification().getBody());

        }
        else  if (remoteMessage.getNotification().getTitle().equals("Arrived"))
        {
           showArrivedNotification(remoteMessage.getNotification().getBody());

        }
        else{
            //Because we will send a Firebase message which contains lat and lng from app

            LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

            Intent intent = new Intent(getBaseContext(), CustomerCall.class);

            intent.putExtra("lat",customer_location.latitude);
            intent.putExtra("lng",customer_location.longitude);
            intent.putExtra("customer",remoteMessage.getNotification().getTitle());
            startActivity(intent);

        }

    }

    private void openRateActivity(String body) {
        Intent intent = new Intent(this, RateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showArrivedNotification(String body) {
        //This code only workfor Android API 25 and bellow
        //From Android API26 or higher,you need create Notification channel
        //I have publish tutorial about this content, you can watch on my channel
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                                        0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager  = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());

    }

}
