package com.example.eam.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.eam.EditProfileActivity;
import com.example.eam.ProfileActivity;
import com.example.eam.R;
import com.example.eam.databinding.ActivityProfileBinding;
import com.example.eam.databinding.FragmentProfileBinding;
import com.example.eam.managers.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public ProfileFragment() {
        // Required empty public constructor
    }

    private FragmentProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        if(firebaseUser!=null){
            getInfo();
        }

        initClickAction();

        return binding.getRoot();
    }

    private void initClickAction() {
        binding.lnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
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
                    Glide.with(getContext()).load(userProfilePic).into(binding.imageProfile);
                }
                else{
                    Glide.with(getContext()).load(R.drawable.icon_male_ph).into(binding.imageProfile);
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