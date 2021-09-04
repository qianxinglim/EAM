package com.example.eam.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eam.AddUserActivity;
import com.example.eam.EmployeeListActivity;
import com.example.eam.R;
import com.example.eam.databinding.FragmentAdminBinding;
import com.example.eam.managers.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class AdminFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public AdminFragment() {
        // Required empty public constructor
    }

    private FragmentAdminBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin, container, false);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        binding.btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddUserActivity.class));
            }
        });

        binding.btnUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EmployeeListActivity.class));
            }
        });

        return binding.getRoot();
    }
}