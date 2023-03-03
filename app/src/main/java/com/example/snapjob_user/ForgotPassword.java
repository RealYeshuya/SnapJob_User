package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class ForgotPassword extends AppCompatActivity {

    private EditText getRPEmail;
    private Button resetPasswordBtn;
    private LoadingDialog loadingDialog;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getRPEmail = (EditText) findViewById(R.id.getRPEmail);
        resetPasswordBtn = (Button) findViewById(R.id.resetPasswordBtn);

        auth = FirebaseAuth.getInstance();

        loadingDialog = new LoadingDialog(ForgotPassword.this);
        
        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

    }

    private void resetPassword() {
        String email = getRPEmail.getText().toString().trim();

        if(email.isEmpty()){
            getRPEmail.setError("Please enter Email!");
            getRPEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            getRPEmail.setError("Please enter Email!");
            getRPEmail.requestFocus();
            return;
        }

        loadingDialog.startLoadingDialog();

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this, "Check your email to reset your password!", Toast.LENGTH_LONG).show();
                    onBackPressed();
                } else {
                    Toast.makeText(ForgotPassword.this, "Try again, something wrong happened", Toast.LENGTH_LONG).show();
                }
                loadingDialog.dismissDialog();
            }
        });
    }
}