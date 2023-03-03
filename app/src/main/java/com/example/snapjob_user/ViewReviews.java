package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.Utils.ReviewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewReviews extends AppCompatActivity {

    private RecyclerView reviewList;
    private DatabaseReference reference;
    private ReviewAdapter reviewAdapter;
    ArrayList<Transactions> listReview;
    String workerKey, workerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reviews);

        reviewList = (RecyclerView) findViewById(R.id.reviewList);
        reference = FirebaseDatabase.getInstance().getReference("Transactions");
        reviewList.setHasFixedSize(true);
        reviewList.setLayoutManager(new LinearLayoutManager(this));

        listReview = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, listReview);
        reviewList.setAdapter(reviewAdapter);

        workerKey= getIntent().getStringExtra("workerId");
        workerName = getIntent().getStringExtra("workerName");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Transactions transactions = dataSnapshot.getValue(Transactions.class);
                    String wid = transactions.getWorkerId();
                    String transactionStat = transactions.getTransactionStatus();
                    float rating = transactions.getRating();
                    if(wid.equals(workerKey)){
                        if(rating != 0){
                            listReview.add(0, transactions);
                        }
                    }

                    reviewList.post(new Runnable() {
                        @Override
                        public void run() {
                            reviewList.smoothScrollToPosition(0);
                        }
                    });
                }
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //DO NOTHING
            }
        });
    }
}