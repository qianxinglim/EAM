package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.eam.databinding.ActivityRegisterUserBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.menu.ProfileFragment;
import com.example.eam.model.Company;
import com.example.eam.model.Leave;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterUserActivity extends AppCompatActivity {
    private ActivityRegisterUserBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private int pos;
    private Company company;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register_user);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        pos = i.getIntExtra("pos", 1);
        company = (Company) getIntent().getSerializableExtra("companyObj");

        binding.etPhone.setEnabled(false);
        binding.etPhone.setText(firebaseUser.getPhoneNumber());

        binding.etClockInTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(binding.etClockInTime);
            }
        });

        binding.etClockOutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(binding.etClockOutTime);
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentReference companyRef = firestore.collection("Companies").document();
                String companyID = companyRef.getId();
                company.setCompanyID(companyID);

                String clockInTime = binding.etClockInTime.getText().toString();
                String clockOutTime = binding.etClockOutTime.getText().toString();
                int workMinutes = 0;

                SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                try {
                    Date clockIn = f24Hours.parse(clockInTime);
                    Date clockOut = f24Hours.parse(clockOutTime);
                    long diff = clockOut.getTime() - clockIn.getTime();
                    workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //company user
                DocumentReference companyUserRef = firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid());
                User user = new User(firebaseUser.getUid(), binding.etName.getText().toString(), firebaseUser.getPhoneNumber(),"",binding.etEmail.getText().toString(),"",binding.etTitle.getText().toString(),binding.etDepartment.getText().toString(), binding.etClockInTime.getText().toString(), binding.etClockOutTime.getText().toString(), workMinutes);

                //company admin
                DocumentReference adminRef = firestore.collection("Companies").document(companyID).collection("Admin").document(firebaseUser.getUid());
                Map<String,Object> addAdmin = new HashMap<>();
                addAdmin.put("id", firebaseUser.getUid());
                addAdmin.put("name", binding.etName.getText().toString());
                addAdmin.put("role", "creator");

                //user
                DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());

                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {
                                //Old user
                                if (pos == 2) {
                                    WriteBatch batch = firestore.batch();

                                    batch.set(companyRef, company);
                                    batch.set(companyUserRef, user);
                                    batch.set(adminRef, addAdmin);
                                    batch.update(userRef, "CompanyID", FieldValue.arrayUnion(companyID));
                                    //firestore.collection("users").document(firebaseUser.getUid()).update("CompanyID", FieldValue.arrayUnion(companyID));

                                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            startActivity(new Intent(RegisterUserActivity.this, MainActivity.class));
                                            Toast.makeText(RegisterUserActivity.this, "Successfully created company.", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterUserActivity.this, "Fail to commit", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(RegisterUserActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                //New User
                                if(pos == 1){
                                    Map<String, Object> addUser = new HashMap<>();
                                    addUser.put("ID", firebaseUser.getUid());
                                    addUser.put("PhoneNo", firebaseUser.getPhoneNumber());
                                    addUser.put("CompanyID", FieldValue.arrayUnion(companyID));

                                    WriteBatch batch = firestore.batch();
                                    batch.set(userRef, addUser);
                                    batch.set(companyRef, company);
                                    batch.set(adminRef, addAdmin);
                                    batch.set(companyUserRef, user);

                                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sessionManager = new SessionManager(RegisterUserActivity.this);

                                            sessionManager.createSession(company.getCompanyID(), company.getCreatorID());
                                            startActivity(new Intent(RegisterUserActivity.this, MainActivity.class));
                                            Toast.makeText(RegisterUserActivity.this, "Successfully created company.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterUserActivity.this, "Fail to commit", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                                else{
                                    Toast.makeText(RegisterUserActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private void getTime(TextView tvTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(RegisterUserActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                String time = hourOfDay + ":" + minute;

                SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                try{
                    Date date = f24Hours.parse(time);

                    tvTime.setText(f24Hours.format(date));
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }, 12,0, false);

        timePickerDialog.show();
    }
}