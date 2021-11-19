package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.eam.databinding.ActivityAddUserBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AddUserActivity extends AppCompatActivity {
    private static final String TAG = "AdminUserActivity";
    private ActivityAddUserBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_user);

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
                Pattern ptr = Pattern.compile("^[0-9]*$");

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

    private void getTime(TextView tvTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddUserActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                //Calendar calendar = Calendar.getInstance();

                String time = hourOfDay + ":" + minute;

                //Initialize 24 hours time format
                SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                try{
                    Date date = f24Hours.parse(time);

                    //Initialize 12 hours time format
                    //SimpleDateFormat f12Hours = new SimpleDateFormat("hh:mm aa");

                    //Set selected time on text view
                    tvTime.setText(f24Hours.format(date));
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }, 12,0, false);

        timePickerDialog.show();
    }

    /*private void getDate(TextView tvDate) {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddUserActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                String date;

                if(String.valueOf(month).length() == 1 && String.valueOf(day).length() == 1){
                    date = "0" + day + "-0" + month + "-" + year;
                }
                else if(String.valueOf(month).length() == 1){
                    date = day + "-0" + month + "-" + year;
                }
                else if(String.valueOf(day).length() == 1){
                    date = "0" + day + "-" + month + "-" + year;
                }
                else{
                    date = day + "-" + month + "-" + year;
                }

                tvDate.setText(date);
            }
        }, year, month, day);

        datePickerDialog.show();
    }*/

    private void addUser(){
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
            Toast.makeText(this, "Please select work time.", Toast.LENGTH_SHORT).show();
        }
        else{
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                Date timeFrom = sdf.parse(binding.etClockInTime.getText().toString());
                Date timeTo = sdf.parse(binding.etClockOutTime.getText().toString());

                if (timeFrom.after(timeTo)) {
                    Toast.makeText(this, "Work end time cannot be earlier than work start time.", Toast.LENGTH_SHORT).show();
                }
                else{
                    submitUpdate();
                }
            }
            catch (ParseException e){
                e.printStackTrace();
                Toast.makeText(this, "Fail to update user profile. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitUpdate() {
        if(binding.tvName.getError() != null || binding.tvEmail.getError() != null || binding.tvPhoneNo.getError() != null || binding.tvDepartment.getError() != null || binding.tvTitle.getError() != null){
            Toast.makeText(this, "Please fill in.", Toast.LENGTH_SHORT).show();
        }
        else{
            final ProgressDialog progressDialog = new ProgressDialog(AddUserActivity.this);
            progressDialog.setMessage("Updating user profile...");
            progressDialog.show();

            String phoneNo = "+" + binding.spCountryPicker.getSelectedCountryCode() + binding.etPhone.getText().toString();
            String name = binding.etName.getText().toString();
            String department = binding.etDepartment.getText().toString();
            String email = binding.etEmail.getText().toString();
            String title = binding.etTitle.getText().toString();
            String clockInTime = binding.etClockInTime.getText().toString();
            String clockOutTime = binding.etClockOutTime.getText().toString();
            //double durationHour = Double.parseDouble(binding.etHour.getText().toString());
            //double durationMinute = Double.parseDouble(binding.etMinute.getText().toString());
            int workMinutes = 0;

            SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

            try {
                Date clockIn = f24Hours.parse(clockInTime);
                Date clockOut = f24Hours.parse(clockOutTime);
                long diff = clockOut.getTime() - clockIn.getTime();
                workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);

                //Log.d(TAG, "clockIn: " + clockIn.getTime() + ", clockOut: " + clockOut.getTime() + ", diff: " + workHours);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            User user = new User("",name,phoneNo,"",email,"",title,department, clockInTime, clockOutTime, workMinutes);

        /*Map<String, Object> addUser = new HashMap<>();
        addUser.put("phoneNo", phoneNo);
        addUser.put("name", name);
        addUser.put("email", email);
        addUser.put("department", department);
        addUser.put("title", title);
        addUser.put("addedBy", firebaseUser.getUid());*/


            firestore.collection("Companies").document(companyID).collection("Users").whereEqualTo("phoneNo", phoneNo).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.getResult().isEmpty()){

                        firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();

                                    if(!document.exists()){
                                        firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(@NonNull Void aVoid) {
                                                progressDialog.dismiss();
                                                finish();
                                                Toast.makeText(AddUserActivity.this, "Successfully invited user.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "fail to add user");
                                            }
                                        });
                                    }
                                    else{
                                        progressDialog.dismiss();
                                        Toast.makeText(AddUserActivity.this, "Invitation has already been sent to the user (Pending)", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(AddUserActivity.this, "User already exists in your company", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}