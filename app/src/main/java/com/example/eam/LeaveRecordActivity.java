package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.adapter.LeaveRequestAdapter;
import com.example.eam.adapter.LeaveReviewAdapter;
import com.example.eam.adapter.ViewAttendanceAdapter;
import com.example.eam.databinding.ActivityLeaveRecordBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Leave;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LeaveRecordActivity extends AppCompatActivity {
    private static final String TAG = "LeaveRecordActivity";
    private ActivityLeaveRecordBinding binding;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private List<Leave> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_record);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        getRequestList();

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.btnAddLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LeaveRecordActivity.this, LeaveFormActivity.class));
            }
        });
    }

    private void getRequestList() {
        reference.child(companyID).child("Leaves").orderByChild("requester").equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
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
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LeaveRecordActivity.this);
                    linearLayoutManager.setReverseLayout(true);
                    linearLayoutManager.setStackFromEnd(true);
                    binding.recyclerView.setLayoutManager(linearLayoutManager);
                    LeaveRequestAdapter adapter = new LeaveRequestAdapter(list, LeaveRecordActivity.this);
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