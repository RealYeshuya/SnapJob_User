package com.example.snapjob_user.Services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.Utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

//For notification in the future
public class MyFirebaseMessagingServices extends FirebaseMessagingService {
    public static final String TAG = MyFirebaseMessagingServices.class.getSimpleName();

    public MyFirebaseMessagingServices(){
    }

    public void onNewToken(String s){
        super.onNewToken(s);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            UserUtils.updateToken(this, s);
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        Log.e(TAG, "");
    }
}

