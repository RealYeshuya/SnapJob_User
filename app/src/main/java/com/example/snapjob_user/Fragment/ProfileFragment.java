package com.example.snapjob_user.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapjob_user.LoadingDialog;
import com.example.snapjob_user.MainActivity;
import com.example.snapjob_user.Model.User;
import com.example.snapjob_user.Utils.ProfileAdapter;
import com.example.snapjob_user.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment implements View.OnClickListener{

    RecyclerView recyclerView;
    String options[];
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference, imageReference;
    private FirebaseFirestore fStore;
    private String userID;
    private Button logout;
    private ImageView imageView;
    ActivityResultLauncher<String> launcher;
    FirebaseDatabase database;
    FirebaseStorage storage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.profile_fragment,container,false);

        logout = (Button) v.findViewById(R.id.logout);
        logout.setOnClickListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        final TextView fullNameTextView  = (TextView) v.findViewById(R.id.fullNameDisplay);
        final TextView emailAddressTextView = (TextView) v.findViewById(R.id.addressDisplay);
        final TextView phoneNumberTextView = (TextView) v.findViewById(R.id.numberDisplay);
        imageView = (ImageView) v.findViewById(R.id.avatar);

        database = FirebaseDatabase.getInstance();
        imageReference = database.getReference("Users");
        storage = FirebaseStorage.getInstance();

        //Gaerror kung hindi magpasulod pic
        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {

                if(result != null){
                    final ProgressDialog pd = new ProgressDialog(getContext());
                    pd.setTitle("Uploading Image...");
                    pd.show();

                    imageView.setImageURI(result);

                    final StorageReference reference = storage.getReference().child(userID);

                    reference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageReference.child(userID).child("image").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(getContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                            double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            pd.setMessage("Uploading: " + (int) progressPercent + "%");
                        }
                    });
                }else{
                    //DO NOTHING
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcher.launch("image/*");
            }
        });

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String fullName = userProfile.fullName;
                    String email = userProfile.email;
                    String phoneNumber = userProfile.phoneNumber;
                    String avatar = userProfile.image;

                    phoneNumberTextView.setText(phoneNumber);
                    fullNameTextView.setText(fullName);
                    emailAddressTextView.setText(email);

                    if(avatar == null){
                        imageView.setImageResource(R.drawable.ic_baseline_person_white_24);
                    } else {
                        Picasso.get()
                                .load(avatar)
                                .fit()
                                .centerCrop()
                                .into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(),"Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });

        //documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
        //    @Override
        //    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
        //        String fullName = value.getString("fullName");
        //        String email = value.getString("email");
        //        String phoneNumber = value.getString("phoneNumber");

        //        phoneNumberTextView.setText(phoneNumber);
        //        fullNameTextView.setText(fullName);
        //        emailAddressTextView.setText(email);
        //    }
        //});

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        options = getResources().getStringArray(R.array.profileOptions);
        ProfileAdapter profileAdapter = new ProfileAdapter(getActivity(), options);
        recyclerView.setAdapter(profileAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_LONG).show();
                startActivity(intent);
                break;
        }
    }
}