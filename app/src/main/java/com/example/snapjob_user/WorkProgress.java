package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.snapjob_user.Model.Worker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WorkProgress extends AppCompatActivity {

    String workerName, transId, clientAdd, transDate, transDesc, wKey, wPic;
    //private Button fininshJob;
    private ProgressBar progressBar;
    private TextView workerNameTxt, clientAddTxt, transIdTxt, dateTxt, transDescTxt, transFeeTxt;
    private ImageView workerPic;

    private DatabaseReference databaseReference, imageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_progress);

        databaseReference = FirebaseDatabase.getInstance().getReference("Transactions");
        imageReference = FirebaseDatabase.getInstance().getReference();

        //fininshJob = (Button) findViewById(R.id.finishButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        workerNameTxt = (TextView) findViewById(R.id.workerNameTxt);
        clientAddTxt = (TextView) findViewById(R.id.clientAdd);
        transIdTxt = (TextView) findViewById(R.id.transactionId);
        dateTxt = (TextView) findViewById(R.id.dateR);
        transDescTxt = (TextView) findViewById(R.id.descriptionTxt);
        transFeeTxt = (TextView) findViewById(R.id.transFee);
        workerPic = (ImageView) findViewById(R.id.imageView4);

        workerName = getIntent().getStringExtra("workerName");
        clientAdd = getIntent().getStringExtra("userAddress");
        transId = getIntent().getStringExtra("transId");
        transDate = getIntent().getStringExtra("transactionDate");
        transDesc = getIntent().getStringExtra("transactionDesc");


        dateTxt.setText("Date: " + transDate);
        workerNameTxt.setText("Worker: " + workerName);
        clientAddTxt.setText("Address: " + clientAdd);
        transIdTxt.setText("TID: " + transId);
        transDescTxt.setText("Description: " + transDesc);

        imageReference.child("Workers")
                .orderByChild("fullName")
                .equalTo(workerName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for(DataSnapshot keySnapshot : snapshot.getChildren()){
                            Worker worker = keySnapshot.getValue(Worker.class);
                            String workerKey = keySnapshot.getKey();
                            String minPay = worker.minPay;
                            String maxPay = worker.maxPay;

                            transFeeTxt.setText("Transaction Fee: Php " + minPay +".00 - Php " + maxPay + ".00");
                            wKey = workerKey;

                            imageReference.child("Workers").child(wKey).addListenerForSingleValueEvent(new ValueEventListener() {
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

        /*
        fininshJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //HashMap hashMap = new HashMap();
                        //hashMap.put("transDate",strDate);

                        databaseReference.child(transId).child("transactionDate").setValue(strDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Intent intent = new Intent(WorkProgress.this, Receipt.class);
                                intent.putExtra("clientName",clientName);
                                intent.putExtra("clientAdd",clientAdd);
                                intent.putExtra("transId",transId);
                                intent.putExtra("date",strDate);
                                intent.putExtra("workerName",workerName);
                                //intent.putExtra()
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(WorkProgress.this,"Error Adding Date!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                            //Do Nothing
                    }
                });
            }
        });

        */

    }
}