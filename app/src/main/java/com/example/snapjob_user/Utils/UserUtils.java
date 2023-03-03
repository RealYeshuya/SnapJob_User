package com.example.snapjob_user.Utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.Model.TokenModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class UserUtils {

    public static void updateUser(View view, Map<String, Object> updateData){
        FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> Snackbar.make(view, "Update Information Successfully!", Snackbar.LENGTH_SHORT).show());
    }

    public static void updateToken(Context context, String Token){
        TokenModel tokenModel = new TokenModel(Token);

        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
