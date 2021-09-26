package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.eam.databinding.ActivityEnterCodeBinding;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EnterCodeActivity extends AppCompatActivity {
    private static String TAG = "EnterCodeActivity";
    private ActivityEnterCodeBinding binding;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private ArrayList<String> listCompanyid = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_enter_code);

        Intent intent = getIntent();
        String phone = intent.getStringExtra("phoneNo");

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: Complete");
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG,"onVerificationFailed: " + e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        startPhoneVerification(phone);

        validateOtpInput();

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = binding.etOtp1.getText().toString()
                        + binding.etOtp2.getText().toString()
                        + binding.etOtp3.getText().toString()
                        + binding.etOtp4.getText().toString()
                        + binding.etOtp5.getText().toString()
                        + binding.etOtp6.getText().toString();

                verifyPhoneNumberWithCode(mVerificationId, code);
            }
        });

        binding.btnResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(phone, mResendToken);
            }
        });
    }

    private void validateOtpInput() {
        binding.etOtp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    binding.etOtp2.requestFocus();
                }
                else{
                    binding.etOtp1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etOtp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    binding.etOtp3.requestFocus();
                }
                else{
                    binding.etOtp1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etOtp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    binding.etOtp4.requestFocus();
                }
                else{
                    binding.etOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etOtp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    binding.etOtp5.requestFocus();
                }
                else{
                    binding.etOtp3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etOtp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    binding.etOtp6.requestFocus();
                }
                else{
                    binding.etOtp4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etOtp6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    binding.etOtp6.requestFocus();
                }
                else{
                    binding.etOtp5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions
                .newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .setForceResendingToken(token)     // ForceResendingToken from callbacks
                .build());
    }

    private void startPhoneVerification(String phoneNumber){
        PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setActivity(this)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(mCallbacks)
                .build());
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                //binding.progressBar.setVisibility(View.INVISIBLE);
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success");

                FirebaseUser user = task.getResult().getUser();

                if(user != null) {
                    DocumentReference userRef = firestore.collection("users").document(user.getUid());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();

                                if(!document.exists()){
                                    firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if(task.getResult().isEmpty()){
                                                    //Toast.makeText(EnterCodeActivity.this, "1Register a company or contact ur company's admin to join.", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(EnterCodeActivity.this, NoCompanyActivity.class));
                                                }
                                                else {
                                                    Map<String, Object> addUser = new HashMap<>();
                                                    addUser.put("id", user.getUid());
                                                    addUser.put("phoneNo", user.getPhoneNumber());
                                                    addUser.put("CompanyID", listCompanyid);

                                                    for (QueryDocumentSnapshot snapshots : task.getResult()) {
                                                        listCompanyid.add(snapshots.getId());

                                                        firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").document(snapshots.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot){
                                                                String companyID = documentSnapshot.getId();
                                                                String name = documentSnapshot.getString("name");
                                                                String email = documentSnapshot.getString("email");
                                                                String phone = documentSnapshot.getString("phoneNo");
                                                                String department = documentSnapshot.getString("department");
                                                                String title = documentSnapshot.getString("title");
                                                                String status = documentSnapshot.getString("status");
                                                                String profilepic = documentSnapshot.getString("profilePic");
                                                                String clockInTime = documentSnapshot.getString("clockInTime");
                                                                String clockOutTime = documentSnapshot.getString("clockOutTime");
                                                                int minutesOfWork = documentSnapshot.getLong("minutesOfWork").intValue();

                                                                WriteBatch batch = firestore.batch();

                                                                DocumentReference nycRef = firestore.collection("Companies").document(companyID).collection("Users").document(user.getUid());
                                                                batch.set(nycRef, new User(user.getUid(), name, user.getPhoneNumber(),"", email,"",title,department,clockInTime, clockOutTime, minutesOfWork));

                                                                Log.d(TAG, "companyID: " + companyID);

                                                                batch.set(userRef, addUser);

                                                                DocumentReference laRef = firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").document(companyID);
                                                                batch.delete(laRef);

                                                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(EnterCodeActivity.this, "Successfully committed", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(EnterCodeActivity.this, SelectCompanyActivity.class));
                                                                        finish();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(EnterCodeActivity.this, "Fail to commit: ", Toast.LENGTH_SHORT).show();
                                                                        Log.d(TAG, "Fail message: " + e.getMessage());
                                                                    }
                                                                });
                                                            }
                                                        });

                                                        Log.d(TAG, "DocumentID: " + snapshots.getId());
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                                else{
                                    firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if(task.getResult().isEmpty()){
                                                    List<String> group = (List<String>) document.get("CompanyID");
                                                    if(group.size() > 0){
                                                        startActivity(new Intent(EnterCodeActivity.this, SelectCompanyActivity.class));
                                                        finish();
                                                    }
                                                    else{
                                                        Toast.makeText(EnterCodeActivity.this, "Register a company or contact ur company's admin to join.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(EnterCodeActivity.this, NoCompanyActivity.class));
                                                    }
                                                }
                                                else {
                                                    for (QueryDocumentSnapshot snapshots : task.getResult()) {
                                                        listCompanyid.add(snapshots.getId());

                                                        firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").document(snapshots.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot){
                                                                String companyID = documentSnapshot.getId();
                                                                String name = documentSnapshot.getString("name");
                                                                String email = documentSnapshot.getString("email");
                                                                String phone = documentSnapshot.getString("phoneNo");
                                                                String department = documentSnapshot.getString("department");
                                                                String title = documentSnapshot.getString("title");
                                                                String status = documentSnapshot.getString("status");
                                                                String profilepic = documentSnapshot.getString("profilePic");
                                                                String clockInTime = documentSnapshot.getString("clockInTime");
                                                                String clockOutTime = documentSnapshot.getString("clockOutTime");
                                                                int minutesOfWork = documentSnapshot.getLong("minutesOfWork").intValue();

                                                                WriteBatch batch = firestore.batch();

                                                                DocumentReference nycRef = firestore.collection("Companies").document(companyID).collection("Users").document(user.getUid());
                                                                batch.set(nycRef, new User(user.getUid(), name, user.getPhoneNumber(),"", email,"",title,department,clockInTime,clockOutTime,minutesOfWork));

                                                                Log.d(TAG, "companyID: " + companyID);

                                                                batch.update(userRef, "CompanyID", FieldValue.arrayUnion(companyID));

                                                                DocumentReference laRef = firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").document(companyID);
                                                                batch.delete(laRef);

                                                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(EnterCodeActivity.this, "Successfully committed", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(EnterCodeActivity.this, SelectCompanyActivity.class));
                                                                        finish();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(EnterCodeActivity.this, "Fail to commit: ", Toast.LENGTH_SHORT).show();
                                                                        Log.d(TAG, "Fail message: " + e.getMessage());
                                                                    }
                                                                });
                                                            }
                                                        });

                                                        Log.d(TAG, "DocumentID: " + snapshots.getId());
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
            else {
                // Sign in failed, display a message and update the UI
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                }
            }
        });
    }
}