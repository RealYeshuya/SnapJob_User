package com.example.snapjob_user.Utils;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.HomePage;
import com.example.snapjob_user.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;

//Amu ni siya kung magback ang user without logout, mabalik siya sa home
public class Home extends Application {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener listener;

    FirebaseFirestore fStore;
    DocumentReference userInforRef;

    @Override
    public void onCreate() {
        super.onCreate();

        if(user != null){
            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
