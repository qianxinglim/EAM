package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
                List<EditText> etlist = new ArrayList<>();
                etlist.add(binding.etName);
                etlist.add(binding.etPhone);
                etlist.add(binding.etEmail);
                etlist.add(binding.etTitle);
                etlist.add(binding.etDepartment);

                List<TextInputLayout> tvlist = new ArrayList<>();
                tvlist.add(binding.tvName);
                tvlist.add(binding.tvPhoneNo);
                tvlist.add(binding.tvEmail);
                tvlist.add(binding.tvTitle);
                tvlist.add(binding.tvDepartment);

                if(binding.etName.getText().toString().equals("") || binding.etName.getText().toString() == null ||
                        binding.etPhone.getText().toString().equals("") || binding.etPhone.getText().toString() == null ||
                        binding.etEmail.getText().toString().equals("") || binding.etEmail.getText().toString() == null ||
                        binding.etTitle.getText().toString().equals("") || binding.etTitle.getText().toString() == null ||
                        binding.etDepartment.getText().toString().equals("") || binding.etDepartment.getText().toString() == null){

                    for(int i=0; i < etlist.size() ; i++){
                        if(etlist.get(i).getText().toString().equals("") || etlist.get(i).getText().toString() == null){
                            tvlist.get(i).setError("This field is required.");
                        }
                    }
                }
                else if(binding.etClockInTime.getText().toString().equals("") || binding.etClockInTime.getText().toString() == null || binding.etClockOutTime.getText().toString().equals("") || binding.etClockOutTime.getText().toString() == null){
                    Toast.makeText(RegisterUserActivity.this, "Please select work time.", Toast.LENGTH_SHORT).show();
                }
                else{
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    try {
                        Date timeFrom = sdf.parse(binding.etClockInTime.getText().toString());
                        Date timeTo = sdf.parse(binding.etClockOutTime.getText().toString());

                        if (timeFrom.after(timeTo)) {
                            Toast.makeText(RegisterUserActivity.this, "Work end time cannot be earlier than work start time.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            registerCompany();
                        }
                    }
                    catch (ParseException e){
                        e.printStackTrace();
                        Toast.makeText(RegisterUserActivity.this, "Fail to update user profile. Please try again.", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });

        validation();
    }

    private void validation() {
        binding.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty() && !s.toString().matches("[a-zA-Z ]*")){
                    binding.tvName.setError("Allows only character");
                }
                else if(s.toString().isEmpty()){
                    binding.tvName.setError("Field cannot be empty");
                }
                else{
                    binding.tvName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Pattern ptr = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

                if(s.toString().isEmpty()){
                    binding.tvEmail.setError("Field cannot be empty");
                }
                else if(!ptr.matcher(s.toString()).matches()){
                    binding.tvEmail.setError("Please enter a valid email address.");
                }
                else{
                    binding.tvEmail.setError(null);
                }
            }
        });

        binding.etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    binding.tvTitle.setError("Field cannot be empty");
                }
                else{
                    binding.tvTitle.setError(null);
                }
            }
        });

        binding.etDepartment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    binding.tvDepartment.setError("Field cannot be empty");
                }
                else{
                    binding.tvDepartment.setError(null);
                }
            }
        });

        binding.etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Pattern ptr = Pattern.compile("^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$");

                if(s.toString().isEmpty()){
                    binding.tvPhoneNo.setError("Field cannot be empty");
                }
                else if(!ptr.matcher(s.toString()).matches()){
                    binding.tvPhoneNo.setError("Please enter a valid phone number.");
                }
                else{
                    binding.tvPhoneNo.setError(null);
                }
            }
        });
    }

    private void registerCompany() {
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