package com.example.snapjob_user.Fragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.HomePage;
import com.example.snapjob_user.Model.Transactions;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.R;
import com.example.snapjob_user.Receipt;
import com.example.snapjob_user.TransactionClass;
import com.example.snapjob_user.TransactionHistory;
import com.example.snapjob_user.Utils.ProgramAdapter;
import com.example.snapjob_user.Utils.TransactionAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class TransactionFragment extends Fragment {

    private FirebaseUser user;
    private RecyclerView listTransactions, listHistoryTransactions;
    private DatabaseReference reference, databaseReference;
    private TransactionAdapter transactionAdapter, transactionAdapter1;
    ArrayList<Transactions> list, listHistory;
    ArrayList<String> tKey;
    private String userID;
    String contentText;
    private Context contextNullSafe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transaction,container,false);

        databaseReference = FirebaseDatabase.getInstance().getReference().child(Common.USER_LOCATION_REFERENCE);

        if (contextNullSafe == null) getContextNullSafety();

        listTransactions = v.findViewById(R.id.ongoingTransList);
        //listHistoryTransactions = v.findViewById(R.id.transHistoryList);
        reference = FirebaseDatabase.getInstance().getReference("Transactions");
        listTransactions.setHasFixedSize(true);
        listTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        //listHistoryTransactions.setHasFixedSize(true);
        //listHistoryTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();


        list = new ArrayList<>();
        tKey = new ArrayList<>();
        //listHistory = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(getContext(), list);
        //transactionAdapter1 = new TransactionAdapter(getContext(), listHistory);
        listTransactions.setAdapter(transactionAdapter);
        //listHistoryTransactions.setAdapter(transactionAdapter1);

        try{
            reference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                    Transactions transactions = snapshot.getValue(Transactions.class);
                    String uid = transactions.getUserId();
                    String tid = snapshot.getKey();
                    String transactionStat = transactions.getTransactionStatus();
                    if(uid.equals(userID)){
                        if(!transactionStat.equals("Complete") && !transactionStat.equals("Declined")){
                            tKey.add(tid);
                            list.add(transactions);
                        }
                    }
                    transactionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                    Transactions transaction = snapshot.getValue(Transactions.class);
                    String key = snapshot.getKey();
                    String userid = transaction.getUserId();
                    String transactionStatus = transaction.getTransactionStatus();
                    String wArrived = transaction.getWorkerArrived();
                    String workerName = transaction.getWorkerName();
                    if(userid.equals(userID)){

                        int index = tKey.indexOf(key);
                        try{
                            list.set(index, transaction);
                        }catch (Exception e){
                            //DO NOTHING
                        }

                        if(transactionStatus.equals("Ongoing")){
                            if(!wArrived.equals("Yes")){
                                contentText = workerName + " accepted your request!";
                                notificationRequest(transactionStatus);
                            } else if (wArrived.equals("Yes")){
                                contentText = workerName + " has arrived at your location";
                                notificationRequest(transactionStatus);
                            }
                        } else if(transactionStatus.equals("Declined")){
                            if(!wArrived.equals("Yes")){
                                contentText = workerName + " declined your request!";
                                try{
                                    tKey.remove(index);
                                    list.remove(index);
                                }catch (Exception e) {
                                    //Toast.makeText(getContext(),"Something went wrong!", Toast.LENGTH_LONG).show();
                                }
                                databaseReference.child("Bacolod").child(userID).removeValue().addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){
                                            //DO NOTHING
                                        }
                                        else {
                                            //DO NOTHING
                                        }
                                    }
                                });
                                notificationRequest(transactionStatus);
                            }
                        } else if(transactionStatus.equals("Complete")){
                            try{
                                tKey.remove(index);
                                list.remove(index);
                            }catch (Exception e) {
                                //Toast.makeText(getContext(),"Something went wrong!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    transactionAdapter.notifyDataSetChanged();
                }
                @Override
                public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }catch(Exception e){
            //DO NOTHING
        }

        /*
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Transactions transactions = dataSnapshot.getValue(Transactions.class);
                    String uid = transactions.getUserId();
                    String transactionStat = transactions.getTransactionStatus();
                    if(uid.equals(userID)){
                        if(!transactionStat.equals("Complete")){
                            list.add(transactions);
                        }
                    }
                }
                transactionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //DO NOTHING
            }
        });

         */
        /*
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Transactions transactions = dataSnapshot.getValue(Transactions.class);
                    String uid = transactions.getUserId();
                    String transactionStat = transactions.getTransactionStatus();
                    if(uid.equals(userID)){
                        if(transactionStat.equals("Complete")){
                            listHistory.add(transactions);
                        }
                    }
                }
                transactionAdapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //DO NOTHING
            }
        });

 */
        return  v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        contextNullSafe = context;
    }

    /**CALL THIS IF YOU NEED CONTEXT*/
    public Context getContextNullSafety() {
        if (getContext() != null) return getContext();
        if (getActivity() != null) return getActivity();
        if (contextNullSafe != null) return contextNullSafe;
        if (getView() != null && getView().getContext() != null) return getView().getContext();
        if (requireContext() != null) return requireContext();
        if (requireActivity() != null) return requireActivity();
        if (requireView() != null && requireView().getContext() != null)
            return requireView().getContext();

        return null;
    }

    private void notificationRequest(String transactionStatus) {
        final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";
        PendingIntent resultPendingIntent;
        Intent intent;

        if(transactionStatus.equals("Ongoing")){
            intent = new Intent(getContextNullSafety(), HomePage.class);
        } else {
            intent = new Intent(getContextNullSafety(), TransactionHistory.class);
        }

        resultPendingIntent = PendingIntent.getActivity(getContextNullSafety(),1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, "Heads Up Notification", NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = getContextNullSafety().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContextNullSafety(), CHANNEL_ID)
                .setContentText("SnapJob")
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true)
                .setContentTitle("Job Request")
                .setContentText(contentText)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getContextNullSafety());
        managerCompat.notify(999, builder.build());
    }
}