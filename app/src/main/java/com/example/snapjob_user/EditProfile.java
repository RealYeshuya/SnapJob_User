package com.example.snapjob_user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snapjob_user.Fragment.ProfileFragment;
import com.example.snapjob_user.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    private FirebaseUser user;
    private DatabaseReference reference;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String userID, clientName, clientPhoneNumber;
    private EditText nameEdit, phoneEdit;
    private Button btnUpdate;
    final LoadingDialog loadingDialog = new LoadingDialog(EditProfile.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        nameEdit = (EditText) findViewById(R.id.nameEdit);
        phoneEdit = (EditText) findViewById(R.id.phoneEdit);

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                clientName = userProfile.fullName;
                clientPhoneNumber = userProfile.phoneNumber;

                nameEdit.setText(clientName);
                phoneEdit.setText(clientPhoneNumber);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        btnUpdate = (Button) findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnUpdate:
                updateUser();
                break;
        }
    }

    private void updateUser() {
        String editFullName = nameEdit.getText().toString().trim();
        String editPhoneNumber = phoneEdit.getText().toString().trim();

        if(editFullName.isEmpty()){
            nameEdit.setError("Please enter Full Name!");
            phoneEdit.requestFocus();
            return;
        }

        if(editPhoneNumber.isEmpty()){
            phoneEdit.setError("Please enter Phone Number!");
            phoneEdit.requestFocus();
            return;
        }

        loadingDialog.startLoadingDialog();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                HashMap hashMap = new HashMap();
                hashMap.put("fullName", editFullName);
                hashMap.put("phoneNumber", editPhoneNumber);

                reference.child(userID).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(EditProfile.this, "Profile has been Updated!", Toast.LENGTH_LONG).show();
                        loadingDialog.dismissDialog();
                        Intent intent = new Intent(EditProfile.this, ProfileFragment.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        loadingDialog.dismissDialog();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                //DO NOTHING
            }
        });
    }
}