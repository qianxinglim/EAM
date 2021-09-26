package com.example.eam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityUserProfileBinding;
import com.example.eam.display.ViewImageActivity;
import com.google.auto.value.AutoOneOf;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userID");
        String userName = intent.getStringExtra("userName");
        String userEmail = intent.getStringExtra("userEmail");
        String userPhone = intent.getStringExtra("userPhone");
        String userDepartment = intent.getStringExtra("userDepartment");
        String userProfilePic = intent.getStringExtra("userProfilePic");

        binding.tvUsername.setText(userName);
        binding.tvPhone.setText(userPhone);
        binding.tvEmail.setText(userEmail);
        binding.tvDepartment.setText(userDepartment);

        if(userProfilePic!=null && !userProfilePic.equals("")) {
            Glide.with(UserProfileActivity.this).load(userProfilePic).into(binding.imageProfile);
        }
        else{
            Glide.with(UserProfileActivity.this).load(R.drawable.icon_male_ph).into(binding.imageProfile);
        }
        
        binding.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfileActivity.this, IndvChatActivity.class)
                        .putExtra("userID", userId)
                        .putExtra("userName", userName)
                        .putExtra("userProfilePic", userProfilePic));
            }
        });

        binding.btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("smsto:" + userPhone);
                Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                i.putExtra("address",userPhone);
                startActivity(i);

                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", userPhone, null)));
            }
        });

        binding.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", userPhone, null));
                startActivity(i);
            }
        });

        binding.btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:" + userEmail)); // only email apps should handle this
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(i);
                }
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.imageProfile.invalidate();
                Drawable dr = binding.imageProfile.getDrawable();
                Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(UserProfileActivity.this, binding.imageProfile,"image");
                Intent intent = new Intent(UserProfileActivity.this, ViewImageActivity.class);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*if(receiverID != null){
            binding.toolbar.setTitle(userName);

            if(userProfilePic.equals("")){
                binding.imageProfile.setImageResource(R.drawable.icon_male_ph);
            }
            else{
                Glide.with(this).load(userProfilePic).into(binding.imageProfile);
            }
        }

        initToolbar();*/
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