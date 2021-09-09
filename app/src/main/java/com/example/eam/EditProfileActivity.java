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

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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
                //showBottomSheetPickPhoto();

                /*Intent intent = new Intent(Intent.ACTION_PICK);
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

                Intent chooser = Intent.createChooser(intent,"select image");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {intent2});
                startActivityForResult(chooser, IMAGE_GALLERY_REQUEST);*/

                openIntent();
            }
        });

        binding.lnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetEditName();
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

        binding.btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogSignOut();
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

    private void showDialogSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setMessage("Do you want to sign out?");
        builder.setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(EditProfileActivity.this, PhoneLoginActivity.class));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showBottomSheetEditName() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_name,null);

        ((View) view.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bsdEditName.dismiss();
            }
        });

        EditText edUserName = view.findViewById(R.id.ed_username);

        ((View) view.findViewById(R.id.btn_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(edUserName.getText().toString())){
                    Toast.makeText(EditProfileActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    updateName(edUserName.getText().toString());
                    bsdEditName.dismiss();
                }
            }
        });

        bsdEditName = new BottomSheetDialog(this);
        bsdEditName.setContentView(view);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Objects.requireNonNull(bsdEditName.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        bsdEditName.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                bsdEditName=null;
            }
        });
        bsdEditName.show();

    }

    private void updateName(String newName) {
        firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).update("name", newName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(EditProfileActivity.this, "New name updated successfully", Toast.LENGTH_SHORT).show();
                getInfo();
            }
        });
    }

    private void showBottomSheetPickPhoto() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick,null);

        ((View) view.findViewById(R.id.ln_gallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
                bottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.ln_camera)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Objects.requireNonNull(bottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                bottomSheetDialog=null;
            }
        });
        bottomSheetDialog.show();

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
                //String userDOB = documentSnapshot.get("DOB").toString();
                //String userDepartment = documentSnapshot.get("Department").toString();
                //String userEmail = documentSnapshot.get("Email").toString();
                //String userGender = documentSnapshot.get("Gender").toString();
                String userProfilePic = documentSnapshot.get("profilePic").toString();

                binding.tvUsername.setText(userName);
                binding.tvPhone.setText(userPhone);

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

            /*try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.imageProfile.setImageBitmap(bitmap);
            }catch(Exception e){
                e.printStackTrace();
            }*/
        }
        else if(requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK){
            uploadToFirebase();
        }

        /*if(requestCode == 440 && resultCode == RESULT_OK){
            uploadToFirebase();
        }*/
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadToFirebase() {
        if(imageUri != null){
            //progressBar.setMessage("Uploading");
            binding.progressBar.setVisibility(View.VISIBLE);

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
                    binding.progressBar.setVisibility(View.INVISIBLE);

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
                    binding.progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}