package com.example.eam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eam.databinding.ActivityMainBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.menu.AdminFragment;
import com.example.eam.menu.AssetsFragment;
import com.example.eam.menu.ChatsFragment;
import com.example.eam.menu.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    //private TextView mtvFullName, mtvEmail, mtvPhone, mtvVerify;
    private SessionManager sessionManager;
    private ActivityMainBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private String companyID;
    private boolean isAdmin = false;
    /*Button mbtnVerify, mbtnClockin;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    String userID;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AssetsFragment()).commit();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //------------------------------------------------------------------------------
        //firestore = FirebaseFirestore.getInstance();

        /*mtvFullName = findViewById(R.id.tvfName);
        mtvPhone = findViewById(R.id.tvPhone);
        mtvEmail = findViewById(R.id.tvEmail);
        mtvVerify = findViewById(R.id.tvVerify);
        mbtnVerify = findViewById(R.id.btnVerify);
        mbtnClockin = findViewById(R.id.btnClockin);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = mAuth.getCurrentUser().getUid();
        FirebaseUser user = mAuth.getCurrentUser();*/

        //--------------------------------------------------------------------------------

        sessionManager = new SessionManager(this);

        if (firebaseUser == null) {
            startActivity(new Intent(MainActivity.this, PhoneLoginActivity.class));

        }
        else {
            boolean isLoggin = sessionManager.checkLogin();

            if(isLoggin){
                HashMap<String, String> userDetail = sessionManager.getUserDetail();
                companyID = userDetail.get(sessionManager.COMPANYID);

                firestore.collection("Companies").document(companyID).collection("Admin").document(firebaseUser.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        if(value != null && value.exists()){
                            binding.bottomNavigation.getMenu().findItem(R.id.nav_admin).setVisible(true);
                        }
                        else{
                            binding.bottomNavigation.getMenu().findItem(R.id.nav_admin).setVisible(false);
                            binding.bottomNavigation.getMenu().removeItem(R.id.nav_admin);
                        }
                    }
                });

                /*firestore.collection("Companies").document(companyID).collection("Admin").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                binding.bottomNavigation.getMenu().findItem(R.id.nav_admin).setVisible(true);
                            } else {
                                binding.bottomNavigation.getMenu().findItem(R.id.nav_admin).setVisible(false);
                                binding.bottomNavigation.getMenu().removeItem(R.id.nav_admin);
                            }
                        } else {
                            Log.d(TAG, "Failed with: " + task.getException());
                        }
                    }
                });*/
            }
            else{
                startActivity(new Intent(MainActivity.this, PhoneLoginActivity.class));
                finish();
            }

            //----------------------------------------------------------------------------
            /*firebaseUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                // Send token to your backend via HTTPS
                                // ...
                            } else {
                                // Handle error -> task.getException();
                            }
                        }
                    });*/

            //-------------------------------------------------------------------------------
        }

        //--------------------------------------------------------------------------------------------------------------------------------------------------

    /*if(!user.isEmailVerified()){
       mbtnVerify.setVisibility(View.VISIBLE);
       mtvVerify.setVisibility(View.VISIBLE);

       mbtnVerify.setOnClickListener(v -> user.sendEmailVerification()
               .addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Verification Email has been sent", Toast.LENGTH_SHORT).show())
               .addOnFailureListener(e -> Log.d(TAG, "onFailure: Email not sent " + e.getMessage())));
    }

    DocumentReference documentReference = fStore.collection("users").document(userID);
    documentReference.addSnapshotListener(this, (documentSnapshot, error) -> {
        mtvPhone.setText(documentSnapshot.getString("phone"));
        mtvEmail.setText(documentSnapshot.getString("email"));
        mtvFullName.setText(documentSnapshot.getString("fName"));
    });*/

        //-------------------------------------------------------------------------------------------------------------------------------------------------

    /*binding.btnClockin.setOnClickListener(view -> {
        startActivity(new Intent(getApplicationContext(),ClockActivity.class));
    });

    binding.btnChat.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this,ChatActivity.class));
        }
    });

    binding.btnAdminUser.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this,AdminUserActivity.class));
        }
    });

    binding.btnCreateCompany.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this,CreateCompanyActivity.class));
        }
    });

    binding.btnLeaveForm.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this,LeaveFormActivity.class));
        }
    });*/

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_assets:
                        selectedFragment = new AssetsFragment();
                        break;
                    case R.id.nav_chat:
                        selectedFragment = new ChatsFragment();
                        break;
                    case R.id.nav_profile:
                        selectedFragment = new ProfileFragment();
                        break;
                    case R.id.nav_admin:
                        selectedFragment = new AdminFragment();
                        break;

                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });
    }

    /*public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        sessionManager.logout();
        startActivity(new Intent(getApplicationContext(),PhoneLoginActivity.class));
        finish();
    }*/

}