package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.eam.adapter.CalendarAdapter;
import com.example.eam.adapter.LeaveReviewAdapter;
import com.example.eam.adapter.RecordAdapter;
import com.example.eam.common.CalendarUtils;
import com.example.eam.databinding.ActivityReviewLeaveBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReviewLeaveActivity extends AppCompatActivity {
    private static final String TAG = "ReviewLeaveActivity";
    private ActivityReviewLeaveBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    //private CalendarAdapter adapter;
    private List<Leave> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_review_leave);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        getLeaveApplications();
    }

    private void getLeaveApplications(){
        LocalDate date = CalendarUtils.selectedDate;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        reference.child(companyID).child("Leaves").orderByChild("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Leave leave = snapshot.getValue(Leave.class);
                    leave.setLeaveId(snapshot.getKey());
                    list.add(leave);
                }

                Log.d(TAG, "listSize: " + list.size());

                if(list.size() > 0){
                    //binding.tvNoRecord.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(ReviewLeaveActivity.this));
                    LeaveReviewAdapter adapter = new LeaveReviewAdapter(list, ReviewLeaveActivity.this);
                    binding.recyclerView.setAdapter(adapter);
                }
                else{
                    //binding.tvNoRecord.setVisibility(View.VISIBLE);
                    //binding.recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}