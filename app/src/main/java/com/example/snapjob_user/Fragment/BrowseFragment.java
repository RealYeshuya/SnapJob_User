package com.example.snapjob_user.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.R;
import com.example.snapjob_user.Utils.FirebaseSearchAdapter;
import com.example.snapjob_user.Utils.ProgramAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BrowseFragment extends Fragment {

    private FirebaseSearchAdapter froAdapter;
    private AutoCompleteTextView act_filter;
    private DatabaseReference dbRef;
    private SearchView searchData;
    private FirebaseRecyclerOptions<Worker> options;
    private RecyclerView browseListWorkers;
    private Query searchQuery;

    String chosenFilter = "Name";
    String[] filter;
    ArrayAdapter<String> arrayAdapter_filter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browse, container, false);

        dbRef = FirebaseDatabase.getInstance().getReference().child("Workers");
        searchData = v.findViewById(R.id.searchData);
        act_filter = v.findViewById(R.id.act_filter);

        act_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                chosenFilter = editable.toString();
            }
        });

        searchData.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (chosenFilter.equalsIgnoreCase("Job")) {
                    jobSearch(s);
                } else {
                    nameSearch(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (chosenFilter.equalsIgnoreCase("Job")) {
                    jobSearch(s);
                } else {
                    nameSearch(s);
                }
                return false;
            }
        });

        browseListWorkers = v.findViewById(R.id.browseListWorkers);
        browseListWorkers.setHasFixedSize(true);
        browseListWorkers.setLayoutManager(new LinearLayoutManager(getContext()));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        filter = getResources().getStringArray(R.array.filter);
        arrayAdapter_filter = new ArrayAdapter<>(getActivity(), R.layout.filter_dropdown_item, filter);
        act_filter.setAdapter(arrayAdapter_filter);
    }

    private void nameSearch(String s) {

        searchQuery = dbRef.orderByChild("searchName")
                .startAt(s.toLowerCase())
                .endAt(s.toLowerCase() + "\uf8ff");

        processSearch();
    }

    private void jobSearch(String s) {

        searchQuery = dbRef.orderByChild("searchJob")
                .startAt(s.toLowerCase())
                .endAt(s.toLowerCase() + "\uf8ff");

        processSearch();
    }

    private void processSearch() {

        options = new FirebaseRecyclerOptions.Builder<Worker>()
                .setQuery(searchQuery, Worker.class)
                .build();

        froAdapter = new FirebaseSearchAdapter(options, getContext());
        froAdapter.startListening();
        browseListWorkers.setAdapter(froAdapter);

        if (searchData.getQuery().toString().isEmpty()) {
            froAdapter.stopListening();
        }
    }
}
