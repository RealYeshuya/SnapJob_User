package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.Model.User;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class RegisterUser extends AppCompatActivity implements View.OnClickListener{

    private EditText getRName, getREmail, getNumber, getRPassword, getRConPassword;
    private Button btnRegister;
    private Button btnCancel;
    final LoadingDialog loadingDialog = new LoadingDialog(RegisterUser.this);
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    ArrayList<String> listPhoneNumber;
    boolean bool = false;

    String TAG = MainActivity.class.getSimpleName();
    String SITE_KEY = "6Lc1qHwdAAAAAIfheHTn9Huh6_kI6aJDu2i_RK3v";
    String SECRET_KEY = "6Lc1qHwdAAAAALVOsx8XAMn3QKhUiAM6OseuBxiz";
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        getRName = (EditText) findViewById(R.id.getRName);
        getREmail = (EditText) findViewById(R.id.getREmail);
        getNumber = (EditText) findViewById(R.id.getRNumber);
        getRPassword = (EditText) findViewById(R.id.getRPassword);
        getRConPassword = (EditText) findViewById(R.id.getRConPassword);

        reference = FirebaseDatabase.getInstance().getReference("Users");
        listPhoneNumber = new ArrayList<>();

        queue = Volley.newRequestQueue(getApplicationContext());

        btnRegister = (Button) findViewById(R.id.registerBtn);
        btnCancel = (Button) findViewById(R.id.cancelBtn);

        mAuth = FirebaseAuth.getInstance();

        btnCancel.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cancelBtn:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.registerBtn:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String fullName = getRName.getText().toString().trim();
        String email = getREmail.getText().toString().trim();
        String phoneNumber = getNumber.getText().toString().trim();
        String password = getRPassword.getText().toString().trim();
        String conPassword = getRConPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            getRName.setError("Please enter Full Name!");
            getRName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            getREmail.setError("Please enter Email!");
            getREmail.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            getNumber.setError("Please enter Phone Number!");
            getNumber.requestFocus();
            return;
        }

        if (phoneNumber.length() != 11) {
            getNumber.setError("Please enter Valid Phone Number!");
            getNumber.requestFocus();
            return;
        }

        /*
        if (checkPhoneNumExist(phoneNumber)) {
            getNumber.setError("Please enter Valid Phone Number!");
            getNumber.requestFocus();
            return;
        }
         */

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            getREmail.setError("Please provide valid Email!");
            getREmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            getRPassword.setError("Please enter Password!");
            getRPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            getRPassword.setError("Minimum password length should be 6 characters");
            getRPassword.requestFocus();
            return;
        }

        if (conPassword.isEmpty()) {
            getRConPassword.setError("Please confirm Password!");
            getRConPassword.requestFocus();
            return;
        }

        if (validate(password, conPassword) == false) {
            getRConPassword.setError("Password does not match");
            getRConPassword.requestFocus();
            return;
        }

        SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                .addOnSuccessListener(this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                        if (!response.getTokenResult().isEmpty()) {
                            handleSiteVerify(response.getTokenResult(), email, password, fullName, phoneNumber);
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.d(TAG, "Error message: " +
                            CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                } else {
                    Log.d(TAG, "Unknown type of error: " + e.getMessage());
                }
            }
        });
    }

    protected void handleSiteVerify(final String responseToken, String email, String password, String fullName, String phoneNumber){
        //it is google recaptcha siteverify server
        //you can place your server url
        String url = "https://www.google.com/recaptcha/api/siteverify";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if(jsonObject.getBoolean("success")){
                            loadingDialog.startLoadingDialog();
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if(task.isSuccessful()){
                                                User user = new User(fullName, email, phoneNumber);

                                                FirebaseDatabase.getInstance().getReference("Users")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(RegisterUser.this, "User has been registered successfully!", Toast.LENGTH_LONG);
                                                            loadingDialog.dismissDialog();
                                                            Intent intent = new Intent(RegisterUser.this, MainActivity.class);
                                                            startActivity(intent);
                                                        }else{
                                                            Toast.makeText(RegisterUser.this, "Failed to register, try again!", Toast.LENGTH_LONG);
                                                            loadingDialog.dismissDialog();
                                                        }
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(RegisterUser.this, "Failed to register, try again!", Toast.LENGTH_LONG).show();
                                                loadingDialog.dismissDialog();
                                            }
                                        }
                                    });
                        }
                        else{
                            Toast.makeText(getApplicationContext(),String.valueOf(jsonObject.getString("error-codes")),Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "JSON exception: " + ex.getMessage());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error message: " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("secret", SECRET_KEY);
                params.put("response", responseToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }


    private boolean validate(String passWord, String conPassword) {
        if(conPassword.equals(passWord)){
            return true;
        }
        return false;
    }

    /*
    private boolean checkPhoneNumExist(String phoneNumber) {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    String workerPhoneNum = user.getPhoneNumber();

                    listPhoneNumber.add(workerPhoneNum);

                    for(int i = 0; i < listPhoneNumber.size(); i++){
                        String existingPhoneNum = listPhoneNumber.get(i);
                        if(existingPhoneNum.equals(phoneNumber)){
                            bool = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //DO NOTHING
            }
        });

        return bool;
    }
     */
}