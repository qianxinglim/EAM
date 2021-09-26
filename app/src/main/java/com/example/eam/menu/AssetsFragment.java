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

import com.example.eam.ClockActivity;
import com.example.eam.ContactActivity;
import com.example.eam.CreateCompanyActivity;
import com.example.eam.LeaveFormActivity;
import com.example.eam.LeaveRecordActivity;
import com.example.eam.PhoneLoginActivity;
import com.example.eam.R;
import com.example.eam.ViewAttendanceActivity;
import com.example.eam.databinding.FragmentAssetsBinding;
import com.example.eam.managers.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Clock;
import java.util.HashMap;

public class AssetsFragment extends Fragment {

    private static final String TAG = "AssetsFragment";

    public AssetsFragment() {
        // Required empty public constructor
    }

    private SessionManager sessionManager;
    private FragmentAssetsBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private String companyID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_assets, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        binding.btnClockIn.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), ClockActivity.class));
        });

        /*binding.btnCreateCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreateCompanyActivity.class));
            }
        });*/

        binding.btnTimesheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();

                            if(document.exists()) {
                                Log.d(TAG, "id: " + document.getString("id"));

                                startActivity(new Intent(getActivity(), ViewAttendanceActivity.class)
                                        .putExtra("userID", document.getString("id"))
                                        .putExtra("userName", document.getString("name"))
                                        .putExtra("userProfilePic", document.getString("profilePic")));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });

        binding.btnLeaveForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LeaveFormActivity.class));
            }
        });

        binding.btnLeaveRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LeaveRecordActivity.class));
            }
        });

        /*binding.btnAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ClockActivity.class).putExtra("pos", 2));
            }
        });*/

        binding.btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ContactActivity.class).putExtra("pos",1));
            }
        });

        return binding.getRoot();
    }
}