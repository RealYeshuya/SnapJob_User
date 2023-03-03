package com.example.snapjob_user.Utils;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapjob_user.Addresses;
import com.example.snapjob_user.EditProfile;
import com.example.snapjob_user.Fragment.FavoritesFragment;
import com.example.snapjob_user.R;
import com.example.snapjob_user.TransactionHistory;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    String choices[];
    Context context;

    public ProfileAdapter(Context ct, String options[]){
        choices = options;
        context = ct;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(context).inflate(R.layout.profile_single_item, parent, false);
        ProfileViewHolder vHolder = new ProfileViewHolder(v);

        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        holder.profileOptions.setText(choices[position]);

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onclick conditions wala pa sa transaction kag settings kay di ko sure kung mayara tuod
                //may gin add ko sa android manifest para magandar siya
                if(choices[position].equalsIgnoreCase("Edit Profile")){
                    Intent intent = new Intent(context, EditProfile.class);
                    context.startActivity(intent);
                } else if(choices[position].equalsIgnoreCase("Transaction History")){
                    Intent intent= new Intent(context, TransactionHistory.class);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return choices.length;
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder {

        TextView profileOptions;
        ConstraintLayout profileLayout;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            profileLayout = itemView.findViewById(R.id.profileLayout);
            profileOptions = itemView.findViewById(R.id.optionName);
        }
    }
}
