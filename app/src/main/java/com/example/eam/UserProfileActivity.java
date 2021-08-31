package com.example.eam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.eam.databinding.ActivityUserProfileBinding;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);

        Intent intent = getIntent();
        String receiverID = intent.getStringExtra("userID");
        String userName = intent.getStringExtra("userName");
        String userProfilePic = intent.getStringExtra("imageProfile");

        if(receiverID != null){
            binding.toolbar.setTitle(userName);

            if(userProfilePic.equals("")){
                binding.imageProfile.setImageResource(R.drawable.icon_male_ph);
            }
            else{
                Glide.with(this).load(userProfilePic).into(binding.imageProfile);
            }
        }

        initToolbar();
    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}