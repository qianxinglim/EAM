package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    private EditText metName, metPhone, metEmail, metPassword;
    private Button mbtnRegister;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    String userID;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        metName = findViewById(R.id.etName);
        metPhone = findViewById(R.id.etPhone);
        metEmail = findViewById(R.id.etEmail);
        metPassword = findViewById(R.id.etPassword);
        mbtnRegister = findViewById(R.id.btnRegister);

        //progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        mbtnRegister.setOnClickListener(view -> {
            String email = metEmail.getText().toString().trim();
            String password = metPassword.getText().toString().trim();
            String fullName = metName.getText().toString();
            String phone = metPhone.getText().toString();

            if (TextUtils.isEmpty(email)){
                metEmail.setError("Email is Required");
            }
            if(TextUtils.isEmpty(email)){
                metPassword.setError("Password is Required");
            }
            /*if(password.length()<6){
                metPassword.setError("Password must be 6 or above characters long");
            }*/

            //progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    FirebaseUser fuser = mAuth.getCurrentUser();
                    fuser.sendEmailVerification()
                            .addOnSuccessListener(aVoid -> Toast.makeText(Register.this, "Verification Email has been sent", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Log.d(TAG, "onFailure: Email not sent " + e.getMessage()));

                    Toast.makeText(Register.this, "User Created", Toast.LENGTH_SHORT).show();
                    userID = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userID);
                    Map<String,Object> user = new HashMap<>();
                    user.put("fName", fullName);
                    user.put("email", email);
                    user.put("phone", phone);
                    documentReference.set(user).addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "onSuccess: user profile is created for " + userID);
                    });
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
                else{
                    Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}