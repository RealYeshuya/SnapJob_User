package com.example.snapjob_user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Toast;

import com.example.snapjob_user.Fragment.LocationFragment;
import com.example.snapjob_user.Fragment.ViewWorker;

public class TransactionClass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_class);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Bundle data = new Bundle();

        String wFullName = getIntent().getStringExtra("workerName");
        String wKey = getIntent().getStringExtra("workerId");
        String wJob = getIntent().getStringExtra("workerJob");
        String transStatus = getIntent().getStringExtra("transactionStatus");
        String wPhoneNum = getIntent().getStringExtra("workerPhoneNumber");

        data.putString("workerFullName", wFullName);
        data.putString("workerId", wKey);
        data.putString("workerJob", wJob);
        data.putString("transactionStatus", transStatus);
        data.putString("workerPhoneNum", wPhoneNum);

        if(transStatus == null){
            LocationFragment locationFragment = new LocationFragment();

            locationFragment.setArguments(data);

            fragmentTransaction.replace(R.id.fragment_container_transaction, locationFragment);
            //Toast.makeText(this, "Entered LocationFragment", Toast.LENGTH_SHORT).show();
        } else {
            ViewWorker viewWorker = new ViewWorker();

            viewWorker.setArguments(data);

            fragmentTransaction.replace(R.id.fragment_container_transaction, viewWorker);
            //Toast.makeText(this, "Entered ViewWroerk", Toast.LENGTH_SHORT).show();
        }
        fragmentTransaction.commit();
    }
}