package com.example.eam.menu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eam.ContactActivity;
import com.example.eam.R;
import com.example.eam.ReviewLeaveActivity;
import com.example.eam.ViewAttendanceActivity;
import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.adapter.TimesheetsAdapter;
import com.example.eam.adapter.TodayTimesheetsAdapter;
import com.example.eam.adapter.ViewAttendanceAdapter;
import com.example.eam.databinding.FragmentTimesheetsBinding;
import com.example.eam.databinding.FragmentTodayBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Chatlist;
import com.example.eam.model.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TodayFragment extends Fragment {

    private static final String TAG = "TodayFragment";

    public TodayFragment() {
        // Required empty public constructor
    }

    private FragmentTodayBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private List<User> userList;
    private List<Attendance> list;
    private List<User> clockedInUserList = new ArrayList<>();
    private List<User> noClockedInUserList = new ArrayList<>();
    private TodayTimesheetsAdapter todayTimesheetsAdapter;
    private int filter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_today, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        list = new ArrayList<>();
        userList = new ArrayList<>();

        getAttendanceList(new OnCallBack() {
            @Override
            public void onSuccess() {
                getUserList(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        compareLists();
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }

            @Override
            public void onFailed(Exception e) {

            }
        });

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetShow();
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        return binding.getRoot();
    }

    private void filter(String text) {
        binding.recyclerView.setVisibility(View.GONE);
        //binding.progressBar.setVisibility(View.VISIBLE);

        ArrayList<User> searchList = new ArrayList<>();

        switch(filter){
            case 0:
                for(User user : userList){
                    if(user.getName().toLowerCase().contains(text.toLowerCase())){
                        searchList.add(user);
                    }
                }

                break;

            case 1:
                for(User user : clockedInUserList){
                    if(user.getName().toLowerCase().contains(text.toLowerCase())){
                        searchList.add(user);
                    }
                }

                break;

            case 2:
                for(User user : noClockedInUserList){
                    if(user.getName().toLowerCase().contains(text.toLowerCase())){
                        searchList.add(user);
                    }
                }

                break;
        }


        /*if(filter.equals("Clocked in")){
            for(User user : clockedInUserList){
                if(user.getName().toLowerCase().contains(text.toLowerCase())){
                    searchList.add(user);
                }
            }
        }
        else if(filter.equals("Not clocked in")){
            for(User user : noClockedInUserList){
                if(user.getName().toLowerCase().contains(text.toLowerCase())){
                    searchList.add(user);
                }
            }
        }
        else{
            for(User user : userList){
                if(user.getName().toLowerCase().contains(text.toLowerCase())){
                    searchList.add(user);
                }
            }
        }*/

//        adapter = new ContactsAdapter(searchList, ContactActivity.this);
//        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        todayTimesheetsAdapter = new TodayTimesheetsAdapter(searchList, list, getContext());
        binding.recyclerView.setAdapter(todayTimesheetsAdapter);
        binding.recyclerView.setVisibility(View.VISIBLE);
        //binding.progressBar.setVisibility(View.GONE);
    }

    private void bottomSheetShow() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_filter_today_attendance, null);

        bottomSheetView.findViewById(R.id.btnAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //binding.btnFilter.setText("All");
                bottomSheetDialog.dismiss();

                filter = 0;

                binding.tvCount.setText(String.valueOf(clockedInUserList.size()));
                binding.tvCountDes.setText("/" + userList.size() + " Users clocked in");

                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                todayTimesheetsAdapter = new TodayTimesheetsAdapter(userList, list, getContext());
                binding.recyclerView.setAdapter(todayTimesheetsAdapter);
            }
        });

        bottomSheetView.findViewById(R.id.btnClockedIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //binding.btnFilter.setText("Clocked in");
                bottomSheetDialog.dismiss();

                filter = 1;

                binding.tvCount.setText(String.valueOf(clockedInUserList.size()));
                binding.tvCountDes.setText("/" + userList.size() + " Users clocked in");

                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                todayTimesheetsAdapter = new TodayTimesheetsAdapter(clockedInUserList, list, getContext());
                binding.recyclerView.setAdapter(todayTimesheetsAdapter);
            }
        });

        bottomSheetView.findViewById(R.id.btnNotClockedIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //binding.btnFilter.setText("Not Clocked in");
                bottomSheetDialog.dismiss();

                filter = 2;

                binding.tvCount.setText(String.valueOf(noClockedInUserList.size()));
                binding.tvCountDes.setText("/" + userList.size() + " Users did not clocked in");

                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                todayTimesheetsAdapter = new TodayTimesheetsAdapter(noClockedInUserList, list, getContext());
                binding.recyclerView.setAdapter(todayTimesheetsAdapter);
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void compareLists(){
        for(User user : userList){
            for(Attendance attendance : list){
                if(user.getID().equals(attendance.getUserId())){
                    clockedInUserList.add(user);
                }
                else{
                    noClockedInUserList.add(user);
                }
            }
        }
    }

    private void getUserList(final OnCallBack onCallBack){
        firestore.collection("Companies").document(companyID).collection("Users").orderBy("department", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                userList.clear();

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getId() != null) {
                        User user = new User(doc.getString("id"), doc.getString("name"), doc.getString("phoneNo"), doc.getString("profilePic"), doc.getString("email"),"", doc.getString("title"), doc.getString("department"),doc.getString("clockInTime"),doc.getString("clockOutTime"),doc.getLong("minutesOfWork").intValue());

                        userList.add(user);
                    }
                }

                onCallBack.onSuccess();

                binding.tvCount.setText(String.valueOf(clockedInUserList.size()));
                binding.tvCountDes.setText("/" + userList.size() + " Users clocked in");

                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                todayTimesheetsAdapter = new TodayTimesheetsAdapter(userList, list, getContext());
                binding.recyclerView.setAdapter(todayTimesheetsAdapter);
//
//                if(todayTimesheetsAdapter != null){
//                    todayTimesheetsAdapter.notifyItemInserted(0);
//                    todayTimesheetsAdapter.notifyDataSetChanged();
//                }
            }
        });
    }

    private void getAttendanceList(final OnCallBack onCallBack) {
        SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
        Date dateTime = new Date();
        String currDate = formatter2.format(dateTime);

        reference.child(companyID).child("Attendance").orderByChild("clockInDate").equalTo("21-09-2021").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Attendance attendance = snapshot.getValue(Attendance.class);

                    list.add(attendance);
                }

                if(list != null){
                    onCallBack.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface OnCallBack{
        void onSuccess();
        void onFailed(Exception e);
    }
}