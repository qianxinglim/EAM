package com.example.eam.menu;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.AddCaptionPicActivity;
import com.example.eam.BuildConfig;
import com.example.eam.EditProfileActivity;
import com.example.eam.PhoneLoginActivity;
import com.example.eam.ProfileActivity;
import com.example.eam.R;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityProfileBinding;
import com.example.eam.databinding.FragmentProfileBinding;
import com.example.eam.display.ViewImageActivity;
import com.example.eam.managers.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public ProfileFragment() {
        // Required empty public constructor
    }

    private FragmentProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;
    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        if(firebaseUser!=null){
            getInfo();
        }

        initActionClick();

        return binding.getRoot();
    }

    /*private void initClickAction() {
        binding.lnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });
    }*/

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
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), binding.imageProfile,"image");
                Intent intent = new Intent(getContext(), ViewImageActivity.class);
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
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to sign out?");
        builder.setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();

                FirebaseAuth.getInstance().signOut();
                sessionManager.logout();
                startActivity(new Intent(getActivity(),PhoneLoginActivity.class));
                getActivity().finish();
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

    private void getInfo() {
        firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.get("name").toString();
                String userPhone = documentSnapshot.get("phoneNo").toString();
                String userDepartment = documentSnapshot.get("department").toString();
                String userEmail = documentSnapshot.get("email").toString();
                String userTitle = documentSnapshot.get("title").toString();
                String userClockInTime = documentSnapshot.get("clockInTime").toString();
                String userClockOutTime = documentSnapshot.get("clockOutTime").toString();
                String userProfilePic = documentSnapshot.get("profilePic").toString();

                binding.tvUsername.setText(userName);
                binding.tvPhone.setText(userPhone);
                binding.tvEmail.setText(userEmail);
                binding.tvDepartment.setText(userDepartment);
                binding.tvTitle.setText(userTitle);
                binding.tvClockInTime.setText(userClockInTime);
                binding.tvClockOutTime.setText(userClockOutTime);

                if(!userProfilePic.equals("-") && userProfilePic!=null && !userProfilePic.equals("")) {
                    Glide.with(getActivity()).load(userProfilePic).into(binding.imageProfile);
                }
                else{
                    Glide.with(getActivity()).load(R.drawable.icon_male_ph).into(binding.imageProfile);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadToFirebase() {
        if(imageUri != null){
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Uploading Profile Picture...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            //binding.progressBar.setVisibility(View.VISIBLE);
            //binding.progressBar.setProgress(progress);

            StorageReference riversRef = FirebaseStorage.getInstance().getReference().child(companyID + "/Profile/Image/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    final String sdownload_url = String.valueOf(downloadUrl);
                    //Toast.makeText(getContext(), "successfully", Toast.LENGTH_SHORT).show();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("profilePic", sdownload_url);
                    //binding.progressBar.setVisibility(View.GONE);
                    //progressDialog.dismiss();

                    firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            //Toast.makeText(getContext(), "upload successfully", Toast.LENGTH_SHORT).show();
                            getInfo();
                            progressDialog.dismiss();
                            progressDialog.setCanceledOnTouchOutside(true);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "upload failed", Toast.LENGTH_SHORT).show();
                    //binding.progressBar.setVisibility(View.INVISIBLE);
                    progressDialog.dismiss();
                    progressDialog.setCanceledOnTouchOutside(true);
                }
            });
        }
    }



    /*private void getInfo(){
        firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String userName = Objects.requireNonNull(documentSnapshot.get("name")).toString();
                String userProfilePic = documentSnapshot.get("profilePic").toString();

                if(!userProfilePic.equals("-") && userProfilePic!=null && !userProfilePic.equals("")) {
                    Glide.with(getContext()).load(userProfilePic).into(binding.imageProfile);
                }
                else{
                    Glide.with(getContext()).load(R.drawable.icon_male_ph).into(binding.imageProfile);
                }

                binding.tvUsername.setText(userName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Get Data","onFailure: " + e.getMessage());
            }
        });
    }*/
}