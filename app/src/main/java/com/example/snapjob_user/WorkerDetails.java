package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.Fragment.LocationFragment;
import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.Model.Worker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WorkerDetails extends AppCompatActivity implements View.OnClickListener {

    private FirebaseUser user;
    private String userID;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    TextView wNameTxt, wJobTxt, wExperienceTxt, wPhoneNumTxt, ratingWorkerTxt, wFeeTxt, wWorkDescTxt;
    ImageView workerPic;
    RatingBar workerRating;
    String wFullName, wJob, wExperience, wPhoneNum, wStat, wKey, wPic, wMinPay, wMaxPay, wWorkDesc;
    Button btnHire, btnViewRating;
    int i = 0;
    float totalRating = 0, averageRating;

    public WorkerDetails(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_details);

        wNameTxt = findViewById(R.id.wNameTxt);
        wJobTxt = findViewById(R.id.wJobTxt);
        wExperienceTxt = findViewById(R.id.wExperienceTxt);
        wPhoneNumTxt = findViewById(R.id.wPhoneNumTxt);
        wFeeTxt = findViewById(R.id.wFeeTxt);
        wWorkDescTxt = findViewById(R.id.wWorkDescTxt);
        ratingWorkerTxt = findViewById(R.id.ratingWorkerTxt);
        btnHire = (Button) findViewById(R.id.btnHire);
        btnViewRating = (Button) findViewById(R.id.btnViewRating);
        workerPic = (ImageView) findViewById(R.id.workerPic);
        workerRating = (RatingBar) findViewById(R.id.workerRating);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        wStat = getIntent().getStringExtra("status");

        if(wStat.equals("On Duty")){
            btnHire.setEnabled(false);
        }else if(wStat.equals("Not Available")){
            btnHire.setEnabled(false);
        }else{
            btnHire.setOnClickListener(this);
        }

        btnViewRating.setOnClickListener(this);

        getData();
        setData();
    }

    private void getData() {
        if (getIntent().hasExtra("name")) {
            wFullName = getIntent().getStringExtra("name");
            wJob = getIntent().getStringExtra("job");
            wExperience = getIntent().getStringExtra("experience");
            wPhoneNum = getIntent().getStringExtra("phoneNum");
            wMinPay = getIntent().getStringExtra("minPay");
            wMaxPay = getIntent().getStringExtra("maxPay");
            wWorkDesc = getIntent().getStringExtra("workDesc");
        } else {
            Toast.makeText(this, "No Data...", Toast.LENGTH_SHORT).show();
        }

        reference.child("Workers")
                .orderByChild("fullName")
                .equalTo(wFullName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                       for(DataSnapshot keySnapshot : snapshot.getChildren()){
                           String workerKey = keySnapshot.getKey();
                           wKey = workerKey;

                           reference.child("Workers").child(wKey).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   Worker worker = snapshot.getValue(Worker.class);
                                   if (worker != null){
                                       wPic = worker.image;
                                       Picasso.get()
                                               .load(wPic)
                                               .fit()
                                               .centerCrop()
                                               .into(workerPic);
                                   }
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });
                       }
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        //DO NOTHING
                    }
                });

        reference.child("Transactions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Transactions transactions = dataSnapshot.getValue(Transactions.class);
                    String workerNames = transactions.workerName;
                    if(workerNames.equals(wFullName)){
                        float rating = transactions.getRating();
                        if(rating != 0){
                            i++;
                            totalRating = totalRating + rating;
                        }
                        if(totalRating == 0){
                            workerRating.setRating(1);
                            workerRating.setClickable(false);
                            ratingWorkerTxt.setText("No Rating");
                        } else{
                            averageRating = totalRating / i;
                            String averageWorkerRating = String.format("%.01f", averageRating);
                            workerRating.setRating(1);
                            workerRating.setClickable(false);
                            ratingWorkerTxt.setText(averageWorkerRating + " Rating");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    private void setData() {
        wNameTxt.setText(wFullName);
        wJobTxt.setText(wJob);
        wPhoneNumTxt.setText(wPhoneNum);
        if(wExperience == null){
            wExperienceTxt.setText("N/A");
        } else{
            wExperienceTxt.setText(wJob + " since " + wExperience);
        }
        if(wWorkDesc == null){
            wWorkDescTxt.setText("N/A");
        } else{
            wWorkDescTxt.setText(wWorkDesc);
        }
        if(wMinPay == null || wMaxPay == null){
            wFeeTxt.setText("N/A");
        } else{
            wFeeTxt.setText("Php " + wMinPay +".00 - Php " + wMaxPay + ".00");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnHire:
                new AlertDialog.Builder(WorkerDetails.this)
                        .setTitle("Confirm Hire")
                        .setMessage("Are you sure you want to hire this worker?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(WorkerDetails.this, TransactionClass.class);
                                intent.putExtra("workerName", wFullName);
                                intent.putExtra("workerId", wKey);
                                intent.putExtra("workerJob", wJob);
                                intent.putExtra("workerPhoneNum", wPhoneNum);
                                startActivity(intent);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
                break;
            case R.id.btnViewRating:
                Intent intent = new Intent(WorkerDetails.this, ViewReviews.class);
                intent.putExtra("workerName", wFullName);
                intent.putExtra("workerId", wKey);
                startActivity(intent);
                break;
        }
    }
}