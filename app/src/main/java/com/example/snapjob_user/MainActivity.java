package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapjob_user.Model.User;
import com.example.snapjob_user.Utils.Home;
import com.example.snapjob_user.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String userID;
    private TextView forgotPassword;
    private Button register;
    private Button login;
    private EditText getEmail, getPassword;
    private LoadingDialog loadingDialog;
    private DatabaseReference loginReference;


    private FirebaseAuth mAuth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(this);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);
        loginReference = FirebaseDatabase.getInstance().getReference();

        getEmail = (EditText) findViewById(R.id.getEmail);
        getPassword = (EditText) findViewById(R.id.getPassword);

        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();

        if(user != null){
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        loadingDialog = new LoadingDialog(MainActivity.this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register:
                startActivity(new Intent(this, RegisterUser.class));
                break;
            case R.id.login:
                userLogin();
                break;
            case R.id.forgotPassword:
                startActivity(new Intent(this, ForgotPassword.class));
                break;
        }
    }

    private void userLogin() {
        String email = getEmail.getText().toString().trim();
        String password = getPassword.getText().toString().trim();

        if(email.isEmpty()){
            getEmail.setError("Please enter Email!");
            getEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            getEmail.setError("Please enter valid Email!");
            getEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            getPassword.setError("Please enter Password!");
            getPassword.requestFocus();
            return;
        }

        if(password.length() < 6){
            getPassword.setError("Minimum password length should be 6 characters");
            getPassword.requestFocus();
            return;
        }

        loadingDialog.startLoadingDialog();

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    loginReference.child("Users")
                            .orderByChild("email")
                            .equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            for(DataSnapshot keySnapshot : snapshot.getChildren()){
                                String userId = keySnapshot.getKey();
                                userID = userId;
                            }

                            if(userID != null){
                                Intent intent = new Intent(MainActivity.this, HomePage.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this,"Incorrect Email or Password, please try again!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            Toast.makeText(MainActivity.this,"Incorrect Email or Password, please try again!", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    Toast.makeText(MainActivity.this, "Incorrect Email or Password, please try again!", Toast.LENGTH_LONG).show();
                }
                loadingDialog.dismissDialog();
            }
        });
    }
}