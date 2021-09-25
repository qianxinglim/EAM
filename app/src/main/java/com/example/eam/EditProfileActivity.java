package com.example.eam;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityEditProfileBinding;
import com.example.eam.display.ViewImageActivity;
import com.example.eam.managers.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;

    private BottomSheetDialog bottomSheetDialog, bsdEditName;
    //private ProgressBar progressBar;

    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        //progressBar = new ProgressBar(this);
        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        if(firebaseUser!=null){
            getInfo();
        }

        initActionClick();
    }

    private void initActionClick() {
        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openIntent();
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.imageProfile.invalidate();
                Drawable dr = binding.imageProfile.getDrawable();
                Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(EditProfileActivity.this, binding.imageProfile,"image");
                Intent intent = new Intent(EditProfileActivity.this, ViewImageActivity.class);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        });
    }

    private void openIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent2.putExtra(MediaStore.EXTRA_OUTPUT,  imageUri);
            intent2.putExtra("listPhotoName", imageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent chooser = Intent.createChooser(intent,"Select Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {intent2});
        startActivityForResult(chooser, IMAGE_GALLERY_REQUEST);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    221);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    222);
        }
        else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,  imageUri);
            intent.putExtra("listPhotoName", imageFileName);
            startActivityForResult(intent, 440);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getInfo() {
        firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.get("name").toString();
                String userPhone = documentSnapshot.get("phoneNo").toString();
                String userDepartment = documentSnapshot.get("department").toString();
                String userEmail = documentSnapshot.get("email").toString();
                String userProfilePic = documentSnapshot.get("profilePic").toString();
                String userTitle = documentSnapshot.get("title").toString();
                String userClockInTime = documentSnapshot.get("clockInTime").toString();
                String userClockOutTime = documentSnapshot.get("clockOutTime").toString();

                binding.tvUsername.setText(userName);
                binding.tvPhone.setText(userPhone);
                binding.tvDepartment.setText(userDepartment);
                binding.tvEmail.setText(userEmail);
                binding.tvTitle.setText(userTitle);
                binding.tvClockInTime.setText(userClockInTime);
                binding.tvClockOutTime.setText(userClockOutTime);

                if(!userProfilePic.equals("-") && userProfilePic!=null && !userProfilePic.equals("")) {
                    Glide.with(EditProfileActivity.this).load(userProfilePic).into(binding.imageProfile);
                }
                else{
                    Glide.with(EditProfileActivity.this).load(R.drawable.icon_male_ph).into(binding.imageProfile);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //Intent chooserIntent = Intent.createChooser(intent,"select image");

        startActivityForResult(Intent.createChooser(intent,"select image"),IMAGE_GALLERY_REQUEST);
        /*ActivityResultLauncher<Intent> launchActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                }
            }
        });
        launchActivity.launch(chooserIntent);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data !=null && data.getData() !=null){

            imageUri = data.getData();
            uploadToFirebase();
        }
        else if(requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK){
            uploadToFirebase();
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadToFirebase() {
        if(imageUri != null){
            final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
            progressDialog.setMessage("Uploading profile picture...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            StorageReference riversRef = FirebaseStorage.getInstance().getReference().child(companyID + "/Profile/Image/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    final String sdownload_url = String.valueOf(downloadUrl);
                    Toast.makeText(EditProfileActivity.this, "successfully", Toast.LENGTH_SHORT).show();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("profilePic", sdownload_url);
                    progressDialog.dismiss();
                    progressDialog.setCanceledOnTouchOutside(true);

                    firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            Toast.makeText(EditProfileActivity.this, "upload successfully", Toast.LENGTH_SHORT).show();
                            getInfo();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "upload failed", Toast.LENGTH_SHORT).show();
                    //binding.progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}