package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapjob_user.Common.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;

public class Receipt extends AppCompatActivity {

    String rclientName, rtransId, rclientAdd, rdate, rworkerName, rTransDesc, rTransStatus, userID, transReview, declinedReason, transactionFee;
    float rating;
    private Button payment, review;
    private TextView clientNameTxt, clientAddTxt, transIdTxt, dateTxt, workerName, transDescTxt, transStat, transFee, reasonDecline;
    private FirebaseUser user;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        databaseReference = FirebaseDatabase.getInstance().getReference().child(Common.USER_LOCATION_REFERENCE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        payment = (Button) findViewById(R.id.paymentConfirm);
        review = (Button) findViewById(R.id.workerReview);
        clientNameTxt = (TextView) findViewById(R.id.clientNameR);
        clientAddTxt = (TextView) findViewById(R.id.clientAddR);
        transIdTxt = (TextView) findViewById(R.id.transID);
        transDescTxt = (TextView) findViewById(R.id.descriptionR);
        dateTxt = (TextView) findViewById(R.id.dateR);
        workerName = (TextView) findViewById(R.id.workerNameR);
        transStat = (TextView) findViewById(R.id.transStatus);
        transFee = (TextView) findViewById(R.id.transFeeReceipt);
        reasonDecline = (TextView) findViewById(R.id.reasonDecline);

        rclientName = getIntent().getStringExtra("userName");
        rclientAdd = getIntent().getStringExtra("userAddress");
        rtransId = getIntent().getStringExtra("transId");
        rTransDesc = getIntent().getStringExtra("transactionDesc");
        rdate = getIntent().getStringExtra("transactionDate");
        rworkerName = getIntent().getStringExtra("workerName");
        rTransStatus = getIntent().getStringExtra("transStatus");
        transReview = getIntent().getStringExtra("transReview");
        declinedReason = getIntent().getStringExtra("declineReason");
        rating = getIntent().getFloatExtra("rating", rating);
        transactionFee = getIntent().getStringExtra("transFee");


        if(declinedReason == null){
            reasonDecline.setText("Reason of Decline: N/A");
        } else {
            reasonDecline.setText("Reason of Decline: " + declinedReason);
        }

        dateTxt.setText("Date: " + rdate);
        clientNameTxt.setText("Client Name:" + rclientName);
        clientAddTxt.setText("Address: " + rclientAdd);
        transDescTxt.setText("Description: " + rTransDesc);
        transIdTxt.setText("Transaction ID: " + rtransId);
        workerName.setText("Worker Name: " + rworkerName);
        transStat.setText("Status: " + rTransStatus);
        transFee.setText("Job Fee: Php " + transactionFee + ".00");

        if (rTransStatus.equals("Complete")){
            transStat.setVisibility(View.GONE);
            payment.setVisibility(View.GONE);
            review.setVisibility(View.VISIBLE);
            reasonDecline.setVisibility(View.GONE);
        } else if(rTransStatus.equals("Declined")){
            transFee.setVisibility(View.GONE);
            payment.setVisibility(View.GONE);
            reasonDecline.setVisibility(View.VISIBLE);
        } else {
            transStat.setVisibility(View.GONE);
            payment.setVisibility(View.VISIBLE);
        }

        //Toast.makeText(Receipt.this, transReview + "\n" + rating, Toast.LENGTH_LONG).show();

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogConfirm();
            }
        });

        if(rating == 0){
            review.setText(R.string.submitReview);

        } else {
            review.setText(R.string.viewReview);
        }

        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Receipt.this, WorkerRating.class);
                intent.putExtra("transId", rtransId);
                startActivity(intent);
                finish();
            }
        });
    }
    private void dialogConfirm(){
        //final View view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you confirm Payment Release?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                databaseReference.child("Bacolod").child(userID).removeValue().addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Receipt.this,"Job Done!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Receipt.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(Receipt.this,"Error Completing Payment!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        builder.show();
    }
}