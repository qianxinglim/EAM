package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.eam.databinding.ActivityPhoneLoginBinding;
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

public class PhoneLoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static String TAG = "PhoneLoginActivity";
    private ActivityPhoneLoginBinding binding;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    //private ProgressBar progressBar;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    private ArrayList<String> listCompanyid = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        //progressBar = new ProgressBar(this);

        binding.btnNext.setOnClickListener(view -> {
            String phone = "+" + binding.spCountryPicker.getSelectedCountryCode() + binding.edPhone.getText().toString();

            startActivity(new Intent(PhoneLoginActivity.this, EnterCodeActivity.class).putExtra("phoneNo", phone));

            /*if(binding.btnNext.getText().equals("Next")){
                //progressBar.setMessage("Please wait");
                binding.progressBar.setVisibility(View.VISIBLE);
                String phone = "+" + binding.spCountryPicker.getSelectedCountryCode() + binding.edPhone.getText().toString();
                //Toast.makeText(this, "number: " + phone, Toast.LENGTH_SHORT).show();
                //String phone = "+" + binding.edCodeCountry.getText().toString() + binding.edPhone.getText().toString();
                startPhoneVerification(phone);
            }
            else{
                verifyPhoneNumberWithCode(mVerificationId, binding.edCode.getText().toString());
            }*/

        });

        /*binding.btnResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = "+" + binding.spCountryPicker.getSelectedCountryCode() + binding.edPhone.getText().toString();

                resendVerificationCode(phone, mResendToken);
            }
        });*/

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: Complete");
                signInWithPhoneAuthCredential(phoneAuthCredential);
                //progressBar.setVisibility(View.INVISIBLE);
                //signInWithPhoneAuthCredential(phoneAuthCredential);
                //binding.btnNext.setText("Confirm");
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG,"onVerificationFailed: " + e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                binding.btnNext.setText("Continue");
                binding.edCode.setVisibility(View.VISIBLE);
                binding.spCountryPicker.setEnabled(false);
                //binding.edCodeCountry.setEnabled(false);
                binding.edPhone.setEnabled(false);

                binding.progressBar.setVisibility(View.INVISIBLE);
                //verifyPhoneNumberWithCode(mVerificationId,binding.edCode.getText().toString());
            }
        };
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

        //mVerificationProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                binding.progressBar.setVisibility(View.INVISIBLE);
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success");

                FirebaseUser user = task.getResult().getUser();

                /*if (user != null){
                    String phone = user.getPhoneNumber();
                    String userID = user.getUid();

                    DocumentReference usersRef = firestore.collection("users").document(userID);
                    usersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                            if(task1.isSuccessful()){
                                DocumentSnapshot document = task1.getResult();

                                if(!document.exists()){
                                    DocumentReference docRefFrom = firestore.collection("temp").document(phone);
                                    docRefFrom.get().addOnCompleteListener(task2 -> {
                                        Log.d(TAG, "Successfully Retrieve Data");

                                        if (task2.isSuccessful()) {
                                            DocumentSnapshot document1 = task2.getResult();
                                            String name = (String) document1.get("Name");
                                            String email = (String) document1.get("Email");
                                            String gender = (String) document1.get("Gender");
                                            String dob = (String) document1.get("DOB");
                                            String department = (String) document1.get("Department");
                                            String profilepic = (String) document1.get("ProfilePic");
                                            //String employmentStartDate = (String) document.get("Employment Start Date");

                                            String tagInput = "BevQrqbZPgCxcGXt2O82, S7ENXxBq8hSF2sDyGUiy";
                                            String[] tagArray = tagInput.split("\\s*,\\s*");
                                            List<String> tags = Arrays.asList(tagArray);

                                            DocumentReference docRefTo = firestore.collection("users").document(userID);
                                            Map<String, Object> addUser = new HashMap<>();
                                            addUser.put("Name", name);
                                            addUser.put("Email", email);
                                            addUser.put("PhoneNo", phone);
                                            addUser.put("Gender",gender);
                                            addUser.put("DOB",dob);
                                            addUser.put("Department",department);
                                            addUser.put("ProfilePic",profilepic);

                                            addUser.put("CompanyID", tags);

                                            docRefTo.set(addUser).addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "onSuccess: user profile is created for " + userID);
                                            });
                                        } else {
                                            Log.d(TAG, "get failed with ", task1.getException());
                                        }
                                    });
                                }
                            }
                            else{
                                Log.d(TAG, "Failed with: ", task.getException());
                            }

                            startActivity(new Intent(PhoneLoginActivity.this, SelectCompanyActivity.class));
                        }
                    });






                    //moveFirestoreDocument(docRef, documentReference);
                }*/


                if(user != null) {

                    //ArrayList<String> listCompanyid = new ArrayList<>();
                    DocumentReference userRef = firestore.collection("users").document(user.getUid());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();

                                if(!document.exists()){
                                    /*Map<String, Object> addUser = new HashMap<>();
                                    addUser.put("PhoneNo", user.getPhoneNumber());
                                    addUser.put("ID", user.getUid());

                                    firestore.collection("users").document(user.getUid()).set(addUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(@NonNull Void aVoid) {
                                            startActivity(new Intent(PhoneLoginActivity.this, SelectCompanyActivity.class));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "fail to add user");
                                        }
                                    });*/

                                    /*firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                                            //Users2 user = new Users2();
                                            //user.setUserCompanies(queryDocumentSnapshots);
                                            Log.d(TAG, "DocumentID" + queryDocumentSnapshots);
                                        }
                                    });*/

                                    firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if(task.getResult().isEmpty()){
                                                    /*firestore.collection("tempUsers").document(phoneNo).collection("Companies").document(companyID).set(addUser).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                                                    Toast.makeText(PhoneLoginActivity.this, "1Register a company or contact ur company's admin to join.", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(PhoneLoginActivity.this, CreateCompanyActivity.class));
                                                }
                                                else {

                                                    //DocumentReference userRef2 = firestore.collection("users").document(user.getUid());
                                                    Map<String, Object> addUser = new HashMap<>();
                                                    addUser.put("id", user.getUid());
                                                    addUser.put("phoneNo", user.getPhoneNumber());
                                                    addUser.put("CompanyID", listCompanyid);
                                                    //batch.set(userRef, addUser);

                                                    for (QueryDocumentSnapshot snapshots : task.getResult()) {
                                                        listCompanyid.add(snapshots.getId());

                                                        firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").document(snapshots.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot){
                                                                /*Users2 user2 = new Users2();
                                                                user2.setUserName(documentSnapshot.getString("Name"));
                                                                user2.setUserEmail(documentSnapshot.getString("Email"));
                                                                user2.setUserPhone(documentSnapshot.getString("PhoneNo"));
                                                                user2.setUserDepartment(documentSnapshot.getString("Department"));

                                                                compUserList.add(user2);*/

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


                                                                /*firestore.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            DocumentSnapshot document = task.getResult();
                                                                            if (document.exists()) {
                                                                                Log.d(TAG, "userExists: true");
                                                                                //userExists = true;
                                                                                DocumentReference userRef1 = firestore.collection("users").document(user.getUid());
                                                                                batch.update(userRef1, "CompanyID", FieldValue.arrayUnion(companyID));
                                                                            }
                                                                            else{
                                                                                Log.d(TAG, "userExists: false");
                                                                                //userExists = false;
                                                                                DocumentReference userRef2 = firestore.collection("users").document(user.getUid());
                                                                                Map<String, Object> addUser = new HashMap<>();
                                                                                addUser.put("ID", user.getUid());
                                                                                addUser.put("PhoneNo", user.getPhoneNumber());
                                                                                addUser.put("CompanyID", FieldValue.arrayUnion(companyID));
                                                                                batch.set(userRef2, addUser);

                                                                            }
                                                                        }
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "Exception: " + e.getMessage());
                                                                    }
                                                                });

                                                                DocumentReference userRef2 = firestore.collection("users").document(user.getUid());
                                                                Map<String, Object> addUser = new HashMap<>();
                                                                addUser.put("ID", user.getUid());
                                                                addUser.put("PhoneNo", user.getPhoneNumber());
                                                                addUser.put("CompanyID", listCompanyid);
                                                                userRef.set(addUser);

                                                                if(tempUsersExists){
                                                                    batch.delete(laRef);
                                                                    Log.d(TAG, "tempUserExists: true");
                                                                }
                                                                else{
                                                                    Log.d(TAG, "tempUserExists: false");
                                                                }

                                                                DocumentReference Ref = firestore.collection("users").document(user.getUid());
                                                                Ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            DocumentSnapshot document = task.getResult();
                                                                            if (document.exists()) {
                                                                                //userExists[0] = true;
                                                                                batch.update(Ref, "CompanyID", FieldValue.arrayUnion(companyID));
                                                                            }
                                                                            else{
                                                                                //userExists[0] = false;
                                                                                Map<String, Object> addUser = new HashMap<>();
                                                                                addUser.put("ID", user.getUid());
                                                                                addUser.put("PhoneNo", user.getPhoneNumber());
                                                                                addUser.put("CompanyID", FieldValue.arrayUnion(companyID));
                                                                                batch.set(Ref, addUser);
                                                                            }
                                                                        }
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "Exception: " + e.getMessage());
                                                                    }
                                                                });

                                                                //Map<String, Object> addUser = new HashMap<>();
                                                                //addUser.put("ID", user.getUid());
                                                                //addUser.put("PhoneNo", user.getPhoneNumber());
                                                                //addUser.put("CompanyID", FieldValue.arrayUnion(companyID));
                                                                //DocumentReference sfRef2 = firestore.collection("users").document(user.getUid());
                                                                //batch.set(sfRef2, addUser);

                                                                //DocumentReference sfRef = firestore.collection("users").document(user.getUid());
                                                                //batch.update(sfRef, "CompanyID", FieldValue.arrayUnion(companyID));

                                                                DocumentReference laRef = firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").document(companyID);
                                                                laRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                                                                        batch.delete(laRef);
                                                                    }
                                                                });*/


                                                                DocumentReference laRef = firestore.collection("tempUsers").document(user.getPhoneNumber()).collection("Companies").document(companyID);
                                                                batch.delete(laRef);

                                                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(PhoneLoginActivity.this, "Successfully committed", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(PhoneLoginActivity.this, SelectCompanyActivity.class));
                                                                        finish();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(PhoneLoginActivity.this, "Fail to commit: ", Toast.LENGTH_SHORT).show();
                                                                        Log.d(TAG, "Fail message: " + e.getMessage());
                                                                    }
                                                                });
                                                            }
                                                        });

                                                        //listCompanyid.add(snapshots.getId());

                                                        Log.d(TAG, "DocumentID: " + snapshots.getId());
                                                    }

                                                    //updateData(user.getUid());
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
                                                        startActivity(new Intent(PhoneLoginActivity.this, SelectCompanyActivity.class));
                                                        finish();
                                                    }
                                                    else{
                                                        Toast.makeText(PhoneLoginActivity.this, "Register a company or contact ur company's admin to join.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(PhoneLoginActivity.this, CreateCompanyActivity.class));
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
                                                                        Toast.makeText(PhoneLoginActivity.this, "Successfully committed", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(PhoneLoginActivity.this, SelectCompanyActivity.class));
                                                                        finish();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(PhoneLoginActivity.this, "Fail to commit: ", Toast.LENGTH_SHORT).show();
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


                                    /*List<String> group = (List<String>) document.get("companyId");
                                    if(group.size() > 0){
                                        startActivity(new Intent(PhoneLoginActivity.this, SelectCompanyActivity.class));
                                    }
                                    else{
                                        Toast.makeText(PhoneLoginActivity.this, "Register a company or contact ur company's admin to join.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(PhoneLoginActivity.this, CreateCompanyActivity.class));
                                    }*/
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

    private void updateData(String userID) {
        WriteBatch batch = firestore.batch();

        for (int i = 0; i < listCompanyid.size(); i++) {
            //Users2 user2 = compUserList.get(i);

//            String companyID = listCompanyid.get(i);
//            String name = compUserList.get(i).getUserName();
//            String email = compUserList.get(i).getUserEmail();
//            String phone = compUserList.get(i).getUserPhone();
//            String department = compUserList.get(i).getUserDepartment();

            //Log.d(TAG, "companyID: " + companyID + ", userid: " + userID + ", username: " + name);

        }
    }

    /*public void moveFirestoreDocument(DocumentReference fromPath, final DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        toPath.set(document.getData())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        fromPath.delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error deleting document", e);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }*/

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}