package com.example.snapjob_user.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.Model.User;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.Utils.ProgramAdapter;
import com.example.snapjob_user.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView listWorkers;
    private DatabaseReference reference;
    private ProgramAdapter programAdapter;
    private ImageView imageView;
    private TextView textView;
    ArrayList<Worker> list;
    ArrayList<String> tKey;
    String stringTKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_fragment,container,false);

        //recycler view ni siya for the workers
        listWorkers = v.findViewById(R.id.listWorkers);
        reference = FirebaseDatabase.getInstance().getReference(Common.WORKER_INFO_REFERENCE);
        listWorkers.setHasFixedSize(true);
        listWorkers.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        tKey = new ArrayList<>();
        programAdapter = new ProgramAdapter(getContext(), list);
        listWorkers.setAdapter(programAdapter);

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Worker worker = snapshot.getValue(Worker.class);
                String workerStatus = worker.getStatus();
                String tid = snapshot.getKey();
                if(workerStatus.equals("Available")){
                    try{
                        tKey.add(tid);
                        stringTKey = tKey.get(0);
                        list.add(worker);
                    }catch(Exception e){
                        //DO NOTHING
                    }
                }
                programAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Worker worker = snapshot.getValue(Worker.class);
                String workerStatus = worker.getStatus();
                String tid = snapshot.getKey();

                if(workerStatus.equals("Available")){
                    tKey.add(tid);
                    list.add(worker);
                } else {
                    try{
                        int index = tKey.indexOf(tid);
                        tKey.remove(index);
                        list.remove(index);
                    }catch (Exception e) {
                        //Toast.makeText(getContext(),"Error retrieving data", Toast.LENGTH_LONG).show();
                    }
                }

                programAdapter.notifyDataSetChanged();
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
        /*
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Worker worker = dataSnapshot.getValue(Worker.class);
                    String workerStatus = worker.getStatus();
                    if(workerStatus.equals("Available")){
                        list.add(worker);
                    }
                }
                programAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //DO NOTHING
            }
        });
        */
        return v;
    }
}