package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.eam.databinding.ActivityAdminUserBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AdminUserActivity extends AppCompatActivity {
    private static final String TAG = "AdminUserActivity";
    private ActivityAdminUserBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_user);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        binding.btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser();
            }
        });
    }

    private void addUser(){
        String phoneNo = binding.etPhone.getText().toString();
        String name = binding.etName.getText().toString();
        String department = binding.etDepartment.getText().toString();
        String email = binding.etEmail.getText().toString();
        String title = binding.etTitle.getText().toString();

        Users user = new Users("",name,phoneNo,"",email,"",title,department);

        /*Map<String, Object> addUser = new HashMap<>();
        addUser.put("phoneNo", phoneNo);
        addUser.put("name", name);
        addUser.put("email", email);
        addUser.put("department", department);
        addUser.put("title", title);
        addUser.put("addedBy", firebaseUser.getUid());*/


        /*firestore.collection("Companies").document(companyID).collection("tempUsers").document(phoneNo).set(addUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(AdminUserActivity.this, "User successfully added.", Toast.LENGTH_SHORT).show();
            }
        });*/

        //batch.set(nycRef, new Users(user.getUid(), name, user.getPhoneNumber(),"", email,"",title,department));

        firestore.collection("Companies").document(companyID).collection("Users").whereEqualTo("phoneNo", phoneNo).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().isEmpty()){
                    /*firestore.collection("users").whereEqualTo("PhoneNo", phoneNo).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if(task.getResult().isEmpty()){*/

                    firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();

                                if(!document.exists()){
                                    firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(@NonNull Void aVoid) {
                                            Toast.makeText(AdminUserActivity.this, "added to tempUsers successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "fail to add user");
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(AdminUserActivity.this, "Invitation has already been sent to the user (Pending)", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    /*firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            Toast.makeText(AdminUserActivity.this, "added to tempUsers successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "fail to add user");
                        }
                    });*/
                                /*}
                                else {
                                    for (QueryDocumentSnapshot snapshots : task.getResult()) {
                                        //String userName = snapshots.getString("userName");
                                        firestore.collection("users").document(snapshots.getId()).update("CompanyID", FieldValue.arrayUnion(companyID));
                                        Log.d(TAG, "DocumentID: " + snapshots.getId());
                                    }
                                }
                            }
                        }
                    });*/
                }
                else{
                    Toast.makeText(AdminUserActivity.this, "User already exists in your company", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*firestore.collection("users").whereEqualTo("PhoneNo", phoneNo).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()){
                        firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).set(addUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {
                                Toast.makeText(AdminUserActivity.this, "added to tempUsers successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "fail to add user");
                            }
                        });
                    }
                    else {
                        for (QueryDocumentSnapshot snapshots : task.getResult()) {
                            //String userName = snapshots.getString("userName");
                            firestore.collection("users").document(snapshots.getId()).update("CompanyID", FieldValue.arrayUnion(companyID));
                            Log.d(TAG, "DocumentID: " + snapshots.getId());
                        }
                    }
                }
            }
        });*/



                /*.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    //String userName = snapshots.getString("userName");
                    firestore.collection("users").document(snapshots.getId()).update("CompanyID", FieldValue.arrayUnion(companyID));
                    Log.d(TAG, "DocumentID: " + snapshots.getId());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).set(addUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void aVoid) {
                        Toast.makeText(AdminUserActivity.this, "added to tempUsers successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "fail to add user");
                    }
                });


//                firestore.collection("tempUsers").document(phoneNo).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if(task.isSuccessful()){
//                            DocumentSnapshot document = task.getResult();
//
//                            //if user not exists in tempUsers
//                            if(!document.exists()){
//                                firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).set(addUser).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(@NonNull Void aVoid) {
//                                        Toast.makeText(AdminUserActivity.this, "added to tempUsers successfully", Toast.LENGTH_SHORT).show();
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.d(TAG, "fail to add user");
//                                    }
//                                });
//                            }
//                            else{
//                                //if user exists in tempUsers
//                                firestore.collection("tempUsers").whereEqualTo("PhoneNo", phoneNo).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
//                                        for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
//                                            //String userName = snapshots.getString("userName");
//                                            firestore.collection("users").document(snapshots.getId()).update("CompanyID", FieldValue.arrayUnion(companyID));
//                                            Log.d(TAG, "DocumentID: " + snapshots.getId());
//                                        }
//                                    }
//                                });
//                            }
//                        }
//                    }
//                });
            }
        });*/
    }
}