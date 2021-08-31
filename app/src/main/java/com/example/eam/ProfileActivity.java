package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.eam.databinding.ActivityProfileBinding;
import com.example.eam.managers.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        if(firebaseUser!=null){
            getInfo();
        }

        initClickAction();
    }

    private void initClickAction() {
        binding.lnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this,EditProfileActivity.class));
            }
        });
    }

    private void getInfo(){
        firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String userName = Objects.requireNonNull(documentSnapshot.get("name")).toString();
                String userProfilePic = documentSnapshot.get("profilePic").toString();

                if(!userProfilePic.equals("-") && userProfilePic!=null && !userProfilePic.equals("")) {
                    Glide.with(ProfileActivity.this).load(userProfilePic).into(binding.imageProfile);
                }
                else{
                    Glide.with(ProfileActivity.this).load(R.drawable.icon_male_ph).into(binding.imageProfile);
                }

                binding.tvUsername.setText(userName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Get Data","onFailure: " + e.getMessage());
            }
        });
    }
}