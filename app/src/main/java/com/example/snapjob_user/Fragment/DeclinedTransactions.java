package com.example.snapjob_user.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.R;
import com.example.snapjob_user.Utils.TransactionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeclinedTransactions extends Fragment {

    private FirebaseUser user;
    private RecyclerView listTransHistory;
    private DatabaseReference transReference;
    private TransactionAdapter transactionAdapter;
    ArrayList<Transactions> listHistory;
    private String userID;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_complete_transactions, container, false);

        listTransHistory = (RecyclerView) v.findViewById(R.id.listTransHistory);
        transReference = FirebaseDatabase.getInstance().getReference("Transactions");
        listTransHistory.setHasFixedSize(true);
        listTransHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        listHistory = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(getContext(), listHistory);
        listTransHistory.setAdapter(transactionAdapter);

        transReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Transactions transactions = dataSnapshot.getValue(Transactions.class);
                    String uid = transactions.getUserId();
                    String transactionStat = transactions.getTransactionStatus();
                    if (uid.equals(userID)) {
                        if (transactionStat.equals("Declined")) {
                            listHistory.add(0, transactions);
                        }
                    }

                    listTransHistory.post(new Runnable() {
                        @Override
                        public void run() {
                            listTransHistory.smoothScrollToPosition(0);
                        }
                    });
                }
                transactionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //DO NOTHING
            }
        });

        return v;
    }
}
