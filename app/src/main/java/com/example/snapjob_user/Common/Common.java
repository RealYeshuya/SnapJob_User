package com.example.snapjob_user.Common;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.example.snapjob_user.Model.User;
import com.example.snapjob_user.Model.WorkerGeoModel;
import com.example.snapjob_user.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//For general use
public class Common {
    public static final String USER_INFO_REFERENCE = "Users";
    public static final String TRANSACTIONS_INFO_REFERENCE = "Transactions";
    public static final String TOKEN_REFERENCE = "Token";
    public static final String WORKER_LOCATION_REFERENCE = "WorkerLocation"; // Same as worker app
    public static final String USER_LOCATION_REFERENCE = "UserLocation";
    public static final String WORKER_INFO_REFERENCE = "Workers"; // Same as worker app
    public static User currentUser;

    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "body";
    public static Set<WorkerGeoModel> workersFound = new HashSet<WorkerGeoModel>();
    public static HashMap<String, Marker> markerList = new HashMap<>();

    public static void showNotification(Context context, int id, String title, String body, Intent intent){
        PendingIntent pendingIntent = null;
        if(intent !=null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "Snapjob_User";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "SnapJob", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("SnapJob");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_official));
        if(pendingIntent != null){
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    public static String buildName(String fullName, String email) {
        return new StringBuilder(fullName).append(" ").append(email).toString();
    }

    public static void setWelcomeMessage(TextView txt_welcome) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour >= 1 && hour <= 12)
            txt_welcome.setText(new StringBuilder("Good Morning"));
        else if(hour >= 13 && hour <= 17)
            txt_welcome.setText(new StringBuilder("Good Afternoon"));
        else
            txt_welcome.setText(new StringBuilder("Good Evening"));
    }

    public static ValueAnimator valueAnimate(long duration, ValueAnimator.AnimatorUpdateListener listener){
        ValueAnimator va = ValueAnimator.ofFloat(0,100);
        va.setDuration(duration);
        va.addUpdateListener(listener);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.RESTART);

        va.start();
        return va;
    }
}
