package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private EditText metEmail, metPassword;
    private Button mbtnLogin;
    private TextView mtvForgotPassword;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        metEmail = findViewById(R.id.etEmail);
        metPassword = findViewById(R.id.etPassword);
        mbtnLogin = findViewById(R.id.btnLogin);
        mtvForgotPassword = findViewById(R.id.tvForgotPassword);
        mAuth = FirebaseAuth.getInstance();

        mbtnLogin.setOnClickListener(view -> {
            String email = metEmail.getText().toString().trim();
            String password = metPassword.getText().toString().trim();

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

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(Login.this, "Login successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
                else{
                    Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        mtvForgotPassword.setOnClickListener(view -> {
            EditText resetMail = new EditText(view.getContext());
            AlertDialog.Builder passwordResetDiaog = new AlertDialog.Builder(view.getContext());
            passwordResetDiaog.setTitle("Reset Password ?");
            passwordResetDiaog.setMessage("Enter your email to receive reset link");
            passwordResetDiaog.setView(resetMail);

            passwordResetDiaog.setPositiveButton("Yes", (dialogInterface, i) -> {
                String mail = resetMail.getText().toString();
                mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(aVoid -> {
                    Toast.makeText(Login.this, "Reset link is sent to your email.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(Login.this, "Error ! Link is not sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            });

            passwordResetDiaog.setNegativeButton("No", (dialogInterface, i) -> {

            });

            passwordResetDiaog.create().show();
        });
    }
}