package com.example.snapjob_user.Utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.Model.User;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.R;
import com.example.snapjob_user.Receipt;
import com.example.snapjob_user.TransactionClass;
import com.example.snapjob_user.WaitingRequest;
import com.example.snapjob_user.WorkProgress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{
    Context context;
    ArrayList<Transactions> list;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;

    public ReviewAdapter(Context context, ArrayList<Transactions> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.review_item,parent,false);
        return new ReviewAdapter.ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ReviewAdapter.ReviewViewHolder holder, int position) {
        Transactions transactions = list.get(position);
        String userId = transactions.userId;
        String userName = transactions.userName;
        String review = transactions.review;
        float rating = transactions.rating;

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();

        reference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null){
                    String image = user.image;
                    if(image != null){
                        Picasso.get()
                                .load(image)
                                .fit()
                                .centerCrop()
                                .into(holder.clientPic);
                    }else {
                        holder.clientPic.setImageResource(R.drawable.ic_baseline_person_24);
                    }
                }else {
                    holder.clientPic.setImageResource(R.drawable.ic_baseline_person_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.clientName.setText(userName);
        holder.ratingBarWorker.setRating(rating);
        holder.ratingBarWorker.setFocusable(false);

        if(review == null){
            holder.reviewDesc.setText("N/A");
        } else {
            holder.reviewDesc.setText(review);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class ReviewViewHolder extends RecyclerView.ViewHolder{
        public RatingBar ratingBarWorker;
        public ImageView clientPic;
        public TextView reviewDesc, clientName;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ratingBarWorker = itemView.findViewById(R.id.ratingBarWorker);
            clientPic = itemView.findViewById(R.id.clientPic);
            reviewDesc = itemView.findViewById(R.id.reviewDesc);
            clientName = itemView.findViewById(R.id.clientName);
        }
    }

}
