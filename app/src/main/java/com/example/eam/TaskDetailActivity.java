package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.adapter.Attachment2Adapter;
import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.adapter.SelectedUserAdapter;
import com.example.eam.adapter.TaskAdapter;
import com.example.eam.databinding.ActivityTaskDetailBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Leave;
import com.example.eam.model.Project;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TaskDetailActivity extends AppCompatActivity {
    private ActivityTaskDetailBinding binding;
    private static final String TAG = "TaskDetailActivity";
    //private Project task;
    private String projectId;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private Attachment2Adapter attachmentAdapter;
    private SelectedUserAdapter selectedUserAdapter;
    private boolean editing = false;
    private List<User> userList = new ArrayList<>();
    private KeyListener etTitleListener, etDescListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task_detail);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        if(firebaseUser == null){
            startActivity(new Intent(getApplicationContext(), PhoneLoginActivity.class));
            finish();
        }

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        //task = (Project) getIntent().getSerializableExtra("taskObj");
        projectId = getIntent().getExtras().getString("projectId");

        Log.e(TAG, "projectId: " + projectId);

        reference.child(companyID).child("Tasks").child(projectId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Project task = snapshot.getValue(Project.class);
                    displayTaskDetails(task);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//        if(task != null){
//            displayTaskDetails();
//        }
//        else{
//            finish();
//            Toast.makeText(this, "Error viewing leave detail.", Toast.LENGTH_SHORT).show();
//        }
    }

    private void displayTaskDetails(Project task) {
        firestore.collection("Companies").document(companyID).collection("Users").document(task.getCreator()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                binding.tvCreator.setText("Created by " + documentSnapshot.getString("name"));

                if(!documentSnapshot.getString("profilePic").equals("") && documentSnapshot.getString("profilePic")!=null) {
                    Glide.with(getApplicationContext()).load(documentSnapshot.getString("profilePic")).into(binding.ivProfilePic);
                }
                else{
                    Glide.with(getApplicationContext()).load(R.drawable.icon_male_ph).into(binding.ivProfilePic);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        binding.tvTitle.setText(task.getTitle());
        etTitleListener = binding.tvTitle.getKeyListener();
        binding.tvTitle.setFocusable(false);
        //tvNote.setEnabled(false);
        binding.tvTitle.setCursorVisible(false);
        binding.tvTitle.setKeyListener(null);
        binding.tvTitle.setBackgroundColor(Color.TRANSPARENT);

        binding.tvStartDate.setText(task.getStartDate());
        binding.tvStartTime.setText(task.getStartTime());
        binding.tvDueDate.setText(task.getDueDate());
        binding.tvDueTime.setText(task.getDueTime());

        if(task.getNote().equals("")){
            binding.lnNote.setVisibility(View.GONE);
        }
        else{
            binding.etDescription.setText(task.getNote());
            etDescListener = binding.etDescription.getKeyListener();
            binding.etDescription.setFocusable(false);
            //tvNote.setEnabled(false);
            binding.etDescription.setCursorVisible(false);
            binding.etDescription.setKeyListener(null);
            binding.etDescription.setBackgroundColor(Color.TRANSPARENT);
        }

        if(task.getAttachments() != null){
            binding.lnAttachments.setVisibility(View.VISIBLE);
            binding.attachmentRecyclerView.setLayoutManager(new GridLayoutManager(this, task.getAttachments().size(), GridLayoutManager.VERTICAL, false));
            attachmentAdapter = new Attachment2Adapter(task.getAttachments(), this);
            binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
        }
        else{
            binding.lnAttachments.setVisibility(View.GONE);
        }

        if(task.getStatus().equals("Pending")){
            //binding.tvStatus.setVisibility(View.GONE);

            binding.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(editing){
                        editing = false;
                        binding.btnUpdate.setVisibility(View.GONE);
                        binding.btnSubmit.setVisibility(View.VISIBLE);
                        binding.btnDelete.setVisibility(View.VISIBLE);
                    }
                    else {
                        editing = true;
                        binding.btnUpdate.setVisibility(View.VISIBLE);
                        binding.btnSubmit.setVisibility(View.GONE);
                        binding.btnDelete.setVisibility(View.GONE);

                        binding.tvTitle.setFocusable(true);
                        binding.tvTitle.setEnabled(true);
                        binding.tvTitle.setCursorVisible(true);
                        binding.tvTitle.setKeyListener(etTitleListener);

                        binding.etDescription.setFocusable(true);
                        binding.etDescription.setEnabled(true);
                        binding.etDescription.setCursorVisible(false);
                        binding.etDescription.setKeyListener(etDescListener);

                        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                    }
                }
            });

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            try {
                Date dueDate = sdf.parse(task.getDueDate());
                Date currDate = new Date();
                Date dueTime = sdf2.parse(task.getDueTime());
                String currTime1 = sdf2.format(currDate);
                Date currTime = sdf2.parse(currTime1);

                if (currDate.after(dueDate) || currTime.after(dueTime)) {
                    binding.tvStatus.setVisibility(View.VISIBLE);
                    binding.tvStatus.setText("Task is overdue !");
                    binding.tvStatus.setTextColor(Color.RED);
                }
                else{
                    binding.tvStatus.setVisibility(View.GONE);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            binding.btnSubmit.setVisibility(View.VISIBLE);
            binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reference.child(companyID).child("Tasks").child(projectId).child("status").setValue("Done").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            Map<String,Object> doneBy = new HashMap<>();
                            doneBy.put("doneBy", firebaseUser.getUid());

                            reference.child(companyID).child("Tasks").child(projectId).updateChildren(doneBy).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void aVoid) {
                                    //displayTaskDetails(task);
                                    Toast.makeText(TaskDetailActivity.this, "Successfully updated.", Toast.LENGTH_SHORT).show();
                                    //binding.btnSubmit.setVisibility(View.GONE);
                                    //binding.tvStatus.setVisibility(View.VISIBLE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            });
        }
        else{
            binding.tvStatus.setVisibility(View.VISIBLE);
            binding.btnSubmit.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
            binding.btnUpdate.setVisibility(View.GONE);
            binding.btnEdit.setVisibility(View.GONE);

            if(task.getDoneBy() != null){
                firestore.collection("Companies").document(companyID).collection("Users").document(task.getDoneBy()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        binding.tvStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        binding.tvStatus.setText("Task done by " + documentSnapshot.getString("name") + "  ✓");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }

        if(task.getAssignTo() != null){
            userList.clear();

            for(int i=0; i < task.getAssignTo().size(); i++) {
                int finalI = i+1;
                firestore.collection("Companies").document(companyID).collection("Users").document(task.getAssignTo().get(i)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        User user = new User(documentSnapshot.getString("id"), documentSnapshot.getString("name"), documentSnapshot.getString("phoneNo"), documentSnapshot.getString("profilePic"), documentSnapshot.getString("email"),"", documentSnapshot.getString("title"), documentSnapshot.getString("department"),documentSnapshot.getString("clockInTime"),documentSnapshot.getString("clockOutTime"),documentSnapshot.getLong("minutesOfWork").intValue());
                        userList.add(user);

                        if(finalI == task.getAssignTo().size()) {

                            Log.e(TAG, "finalI: " + finalI + ", taskList.size(): " + task.getAssignTo().size());

                            if (userList.size() > 0) {
                                binding.selectedUserRecyclerview.setLayoutManager(new GridLayoutManager(TaskDetailActivity.this, 2, GridLayoutManager.VERTICAL, false));
                                selectedUserAdapter = new SelectedUserAdapter(userList, TaskDetailActivity.this, new SelectedUserAdapter.OnClickListener() {
                                    @Override
                                    public void onBtnDeleteClick(View view, int position) {

                                    }
                                });
                                binding.selectedUserRecyclerview.setAdapter(selectedUserAdapter);

                            }
                            else {
                                //binding.tvNoRecord.setVisibility(View.VISIBLE);
                                //binding.recyclerView.setVisibility(View.GONE);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
    }
}