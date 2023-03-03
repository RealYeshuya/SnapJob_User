package com.example.snapjob_user.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapjob_user.EditProfile;
import com.example.snapjob_user.HomePage;
import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.R;
import com.example.snapjob_user.Receipt;
import com.example.snapjob_user.TransactionClass;
import com.example.snapjob_user.WaitingRequest;
import com.example.snapjob_user.WorkProgress;
import com.example.snapjob_user.WorkerDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>{
    Context context;
    ArrayList<Transactions> list;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;

    public TransactionAdapter(Context context, ArrayList<Transactions> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.transaction_single_item,parent,false);
        return new TransactionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull TransactionAdapter.TransactionViewHolder holder, int position) {
        Transactions transactions = list.get(position);
        String transId = transactions.transId;
        String userName = transactions.userName;
        String address = transactions.address;
        String workerName = transactions.workerName;
        String workerId = transactions.workerId;
        String transStatus = transactions.transactionStatus;
        String workerArrived = transactions.workerArrived;
        String transDate = transactions.transactionDate;
        String transDescription = transactions.transactionDescription;
        String transactionReview = transactions.review;
        String declineReason = transactions.declineReason;
        float rating = transactions.rating;
        String transactionFee = transactions.transactionFee;

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();

        reference.child("Workers").child(workerId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                .into(holder.workerPic);
                    }else {
                        holder.workerPic.setImageResource(R.drawable.ic_baseline_person_24);
                    }
                }else {
                    holder.workerPic.setImageResource(R.drawable.ic_baseline_person_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tId.setText("TID: " + transId);
        holder.workerName.setText("Worker: " + workerName);
        holder.transStatus.setText("Status: " + transStatus);
        holder.transactionSingleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(transStatus.equals("Waiting")){
                    Intent intent = new Intent(context, WaitingRequest.class);
                    context.startActivity(intent);
                } else if(transStatus.equals("Ongoing")){
                    if(workerArrived.equals("Yes")){
                        Intent intent = new Intent(context, WorkProgress.class);
                        intent.putExtra("transId", transId);
                        intent.putExtra("workerName", workerName);
                        intent.putExtra("userAddress", address);
                        intent.putExtra("transactionDesc", transDescription);
                        intent.putExtra("transactionDate", transDate);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, TransactionClass.class);
                        intent.putExtra("workerName", workerName);
                        intent.putExtra("workerId", workerId);
                        intent.putExtra("transactionStatus", transStatus);
                        context.startActivity(intent);
                    }
                } else if(transStatus.equals("Pending")){
                    Intent intent = new Intent(context, Receipt.class);
                    intent.putExtra("userName", userName);
                    intent.putExtra("workerName", workerName);
                    intent.putExtra("transId", transId);
                    intent.putExtra("userAddress", address);
                    intent.putExtra("transactionDate", transDate);
                    intent.putExtra("transactionDesc", transDescription);
                    intent.putExtra("transStatus", transStatus);
                    intent.putExtra("transFee", transactionFee);
                    context.startActivity(intent);
                } else if(transStatus.equals("Complete")){
                    Intent intent = new Intent(context, Receipt.class);
                    intent.putExtra("userName", userName);
                    intent.putExtra("workerName", workerName);
                    intent.putExtra("transId", transId);
                    intent.putExtra("userAddress", address);
                    intent.putExtra("transactionDate", transDate);
                    intent.putExtra("transactionDesc", transDescription);
                    intent.putExtra("transStatus", transStatus);
                    intent.putExtra("transReview", transactionReview);
                    intent.putExtra("rating", rating);
                    intent.putExtra("transFee", transactionFee);
                    context.startActivity(intent);
                } else if(transStatus.equals("Declined")) {
                    Intent intent = new Intent(context, Receipt.class);
                    intent.putExtra("userName", userName);
                    intent.putExtra("workerName", workerName);
                    intent.putExtra("transId", transId);
                    intent.putExtra("userAddress", address);
                    intent.putExtra("transactionDate", transDate);
                    intent.putExtra("transactionDesc", transDescription);
                    intent.putExtra("transStatus", transStatus);
                    intent.putExtra("declineReason", declineReason);
                    context.startActivity(intent);
                }
                /*
                Intent intent = new Intent(context, TransactionClass.class);
                intent.putExtra("workerName", workerName);
                intent.putExtra("workerId", workerId);
                context.startActivity(intent);
                 */
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class TransactionViewHolder extends RecyclerView.ViewHolder{
        public TextView tId, workerName, transStatus;
        public ImageView workerPic;
        //LinearLayout ni siya ka singleItem.xml
        ConstraintLayout transactionSingleItem;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tId = itemView.findViewById(R.id.transId);
            workerName = itemView.findViewById(R.id.workerName);
            transStatus = itemView.findViewById(R.id.transStatus);
            workerPic = itemView.findViewById(R.id.workerPic);
            transactionSingleItem = itemView.findViewById(R.id.transactionSingleItem);
        }
    }
}
