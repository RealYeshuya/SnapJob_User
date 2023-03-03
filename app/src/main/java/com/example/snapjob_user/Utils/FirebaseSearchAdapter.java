package com.example.snapjob_user.Utils;

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

import com.example.snapjob_user.Fragment.BrowseFragment;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.R;
import com.example.snapjob_user.WorkerDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class FirebaseSearchAdapter extends FirebaseRecyclerAdapter<Worker, FirebaseSearchAdapter.SearchViewHolder>{

    Context ct;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FirebaseSearchAdapter(@NonNull @NotNull FirebaseRecyclerOptions<Worker> options, Context ct) {
        super(options);

        this.ct = ct;
    }


    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item, parent, false);

        return new SearchViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull @NotNull FirebaseSearchAdapter.SearchViewHolder holder, int position, @NonNull @NotNull Worker model) {
        String fullName = model.getFullName();
        String job = model.getJob();
        String available = model.getStatus();
        String phoneNum = model.getPhoneNum();
        String email = model.getEmail();
        String image = model.getImage();
        String experience = model.getExperience();
        String minPay = model.getMinPay();
        String maxPay = model.getMaxPay();
        String workDesc = model.getWorkDesc();

        holder.name.setText("Name: " + fullName);
        holder.job.setText("Job: " + job);
        holder.availability.setText("Worker Status: " + available);
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
                Intent intent = new Intent(ct, WorkerDetails.class);
                intent.putExtra("name", fullName);
                intent.putExtra("job", job);
                intent.putExtra("status", available);
                intent.putExtra("phoneNum", phoneNum);
                intent.putExtra("experience", experience);
                intent.putExtra("minPay", minPay);
                intent.putExtra("maxPay", maxPay);
                intent.putExtra("workDesc", workDesc);
                ct.startActivity(intent);
            }
        });
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder{
        public TextView name, job, availability;
        public ImageView workerPic;
        public ConstraintLayout singleItem;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.workerName);
            job = itemView.findViewById(R.id.workerJob);
            availability = itemView.findViewById(R.id.workerAvailablility);
            workerPic = itemView.findViewById(R.id.workerPic);
            singleItem = itemView.findViewById(R.id.singleItem);
        }
    }
}
