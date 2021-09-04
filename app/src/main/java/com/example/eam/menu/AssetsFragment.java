package com.example.eam.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eam.ClockActivity;
import com.example.eam.CreateCompanyActivity;
import com.example.eam.LeaveFormActivity;
import com.example.eam.PhoneLoginActivity;
import com.example.eam.R;
import com.example.eam.databinding.FragmentAssetsBinding;
import com.example.eam.managers.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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
        sessionManager = new SessionManager(getActivity());

        /*if(firebaseUser == null){
            startActivity(new Intent(getActivity(), PhoneLoginActivity.class));
        }
        else{
            sessionManager.checkLogin();
        }*/

        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        binding.tvEmail.setText(companyID);

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        binding.btnClockin.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), ClockActivity.class));
        });

        binding.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getActivity(), ChatActivity.class));
            }
        });

        binding.btnCreateCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreateCompanyActivity.class));
            }
        });

        binding.btnLeaveForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LeaveFormActivity.class));
            }
        });

        return binding.getRoot();
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        sessionManager.logout();
        startActivity(new Intent(getActivity(),PhoneLoginActivity.class));
        getActivity().finish();
    }
}