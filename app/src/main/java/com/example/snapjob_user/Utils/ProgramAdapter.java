package com.example.snapjob_user.Utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.R;
import com.example.snapjob_user.WorkerDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder>{
    Context context;
    ArrayList<Worker> list;


    public ProgramAdapter(Context context, ArrayList<Worker> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_item,parent,false);
        return new ProgramViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramAdapter.ProgramViewHolder holder, int position) {
        Worker worker = list.get(position);
        String fullName = worker.fullName;
        String job = worker.job;
        String status = worker.status;
        String phoneNum = worker.phoneNum;
        String email = worker.email;
        String image = worker.image;
        String experience = worker.experience;
        String minPay = worker.minPay;
        String maxPay = worker.maxPay;
        String workDesc = worker.workDesc;
        //Worker soon wala pa ang database
        holder.name.setText("Name: " + fullName);
        holder.job.setText("Job: " + job);
        holder.availability.setText("Worker Status: " + status);
        if(image != null){
            Picasso.get()
                    .load(image)
                    .fit()
                    .centerCrop()
                    .into(holder.workerPic);
        }else {
            holder.workerPic.setImageResource(R.drawable.ic_baseline_person_24);
        }
        holder.singleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WorkerDetails.class);
                intent.putExtra("name", fullName);
                intent.putExtra("job", job);
                intent.putExtra("status", status);
                intent.putExtra("phoneNum", phoneNum);
                intent.putExtra("experience", experience);
                intent.putExtra("minPay", minPay);
                intent.putExtra("maxPay", maxPay);
                intent.putExtra("workDesc", workDesc);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ProgramViewHolder extends RecyclerView.ViewHolder{
        public TextView name, job, availability;
        public ImageView workerPic;
        //LinearLayout ni siya ka singleItem.xml
        ConstraintLayout singleItem;

        public ProgramViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.workerName);
            job = itemView.findViewById(R.id.workerJob);
            availability = itemView.findViewById(R.id.workerAvailablility);
            workerPic = itemView.findViewById(R.id.workerPic);
            singleItem = itemView.findViewById(R.id.singleItem);
        }
    }
}
