package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;

public class WorkerRating extends AppCompatActivity {

    private DatabaseReference reference, imageReference, transactionReference;
    private RatingBar starRating;
    private ImageView workerImageRating;
    private TextView rating_workerName, descriptionReview;
    private EditText ratingDescription;
    private Button reviewConfirm;
    String ratingTransId, workerName, workerId, workerReview;
    float rateValue, workerRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_rating);

        reference = FirebaseDatabase.getInstance().getReference().child("Transactions");
        transactionReference = FirebaseDatabase.getInstance().getReference("Transactions");
        imageReference = FirebaseDatabase.getInstance().getReference();

        starRating = (RatingBar) findViewById(R.id.starRating);
        workerImageRating = (ImageView) findViewById(R.id.workerImageRating);
        rating_workerName = (TextView) findViewById(R.id.rating_workerName);
        descriptionReview = (TextView) findViewById(R.id.descriptionReview);
        ratingDescription = (EditText) findViewById(R.id.ratingDescription);
        reviewConfirm = (Button) findViewById(R.id.reviewConfirm);

        ratingTransId = getIntent().getStringExtra("transId");

        //To get the worker name and image name
        reference.child(ratingTransId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Transactions transactions = snapshot.getValue(Transactions.class);
                workerName = transactions.getWorkerName();
                workerId = transactions.getWorkerId();
                workerRating = transactions.getRating();
                workerReview = transactions.getReview();

                rating_workerName.setText(workerName);

                imageReference.child("Workers").child(workerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Worker worker = snapshot.getValue(Worker.class);
                        if (worker != null){
                            String image = worker.image;
                            if(image != null){
                                Picasso.get()
                                        .load(image)
                                        .fit()
                                        .centerCrop()
                                        .into(workerImageRating);
                            }else {
                                workerImageRating.setImageResource(R.drawable.ic_baseline_person_24);
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                if(workerRating != 0){
                    reviewConfirm.setVisibility(View.GONE);
                    starRating.setRating(workerRating);
                    starRating.setIsIndicator(true);
                    ratingDescription.setVisibility(View.GONE);
                    descriptionReview.setVisibility(View.VISIBLE);
                    descriptionReview.setText(workerReview);

                } else {

                    //To rate the worker
                    starRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                            rateValue = starRating.getRating();
                        }
                    });

                    reviewConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String review = ratingDescription.getText().toString().trim();

                            if(rateValue == 0){
                                Toast.makeText(WorkerRating.this, "Please rate worker before confirming", Toast.LENGTH_LONG).show();
                            } else {
                                transactionReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                        transactionReference.child(ratingTransId).child("rating").setValue(rateValue);
                                        transactionReference.child(ratingTransId).child("review").setValue(review);

                                        Toast.makeText(WorkerRating.this, "Thanks for your review!", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(WorkerRating.this, HomePage.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        finish();
                                        overridePendingTransition(0,0);
                                        startActivity(intent);
                                        overridePendingTransition(0,0);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }
                /*
                Intent intent = new Intent(WorkerRating.this, TransactionHistory.class);
                startActivity(intent);
                finish();
                 */
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}