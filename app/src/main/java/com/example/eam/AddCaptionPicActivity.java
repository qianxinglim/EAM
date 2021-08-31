package com.example.eam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.databinding.ActivityAddCaptionPicBinding;
import com.example.eam.managers.ChatService;
import com.example.eam.service.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public class AddCaptionPicActivity extends AppCompatActivity {
    private Uri imageUri;
    private ActivityAddCaptionPicBinding binding;
    private ChatService chatService;
    private String receiverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_caption_pic);

        Intent intent = getIntent();
        //String uri = intent.getStringExtra("image");
        //imageUri = Uri.parse(uri);
        receiverID = intent.getStringExtra("receiverID");

        imageUri = IndvChatActivity.imageUri;
        chatService = new ChatService(this,receiverID);

        setInfo();
        initClick();
    }

    private void initClick() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(AddCaptionPicActivity.this);
                progressDialog.setMessage("Sending image...");
                progressDialog.show();

                new FirebaseService(AddCaptionPicActivity.this).uploadImageToFirebaseStorage(imageUri, new FirebaseService.OnCallBack() {
                    @Override
                    public void onUploadSuccess(String imageUrl) {

                        chatService.sendImage(imageUrl,binding.edDescription.getText().toString());
                        progressDialog.dismiss();

                        finish();
                    }

                    @Override
                    public void onUploadFailed(Exception e) {
                        Log.e("TAG", "onUploadFailed: ",e );
                        finish();
                    }
                });
            }
        });
    }

    private void setInfo() {
        Glide.with(this).load(imageUri).into(binding.imageView);
    }
}