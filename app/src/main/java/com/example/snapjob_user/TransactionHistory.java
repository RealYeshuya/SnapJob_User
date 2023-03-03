package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.snapjob_user.Fragment.CompleteTransactions;
import com.example.snapjob_user.Fragment.DeclinedTransactions;
import com.example.snapjob_user.Fragment.FavoritesFragment;
import com.example.snapjob_user.Fragment.TransactionFragment;
import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.Utils.TransactionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TransactionHistory extends AppCompatActivity implements View.OnClickListener{

    private FirebaseUser user;
    private RecyclerView listTransHistory;
    private DatabaseReference transReference;
    private TransactionAdapter transactionAdapter;
    ArrayList<Transactions> listHistory;
    private String userID;

    private Button completeTransaction, declinedTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        completeTransaction = (Button) findViewById(R.id.completeTransaction);
        declinedTransaction = (Button) findViewById(R.id.declinedTransaction);

        completeTransaction.setOnClickListener(this);
        declinedTransaction.setOnClickListener(this);
        /*
        listTransHistory = (RecyclerView) findViewById(R.id.listTransHistory);
        transReference = FirebaseDatabase.getInstance().getReference("Transactions");
        listTransHistory.setHasFixedSize(true);
        listTransHistory.setLayoutManager(new LinearLayoutManager(this));

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        listHistory = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, listHistory);
        listTransHistory.setAdapter(transactionAdapter);

        transReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Transactions transactions = dataSnapshot.getValue(Transactions.class);
                    String uid = transactions.getUserId();
                    String transactionStat = transactions.getTransactionStatus();
                    if(uid.equals(userID)){
                        if(transactionStat.equals("Complete")){
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

         */
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.declinedTransaction:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentTransaction, new DeclinedTransactions()).commit();
                break;
            default:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentTransaction, new CompleteTransactions()).commit();
                break;
        }
    }
}