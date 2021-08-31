package com.example.eam.service;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.eam.AddCaptionPicActivity;
import com.example.eam.EditProfileActivity;
import com.example.eam.IndvChatActivity;
import com.example.eam.LeaveFormActivity;
import com.example.eam.managers.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import androidx.annotation.NonNull;

public class FirebaseService {
    private Context context;
    private String path;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference databaseReference;
    private SessionManager sessionManager;
    private String companyID;

    public FirebaseService(Context context) {
        this.context = context;

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(context);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);
    }

    public void uploadDocumentToFirebaseStorage(Uri uri, final OnCallBack onCallBack){
        if(context instanceof IndvChatActivity){
            path = companyID + "/Chat/Documents/";
        }
        else if(context instanceof LeaveFormActivity){
            path = companyID + "/Leave/Documents/";
        }

        StorageReference riversRef = FirebaseStorage.getInstance().getReference().child(path + System.currentTimeMillis() + "." + getFileExtension(uri));
        riversRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();

                final String sdownload_url = String.valueOf(downloadUrl);

                onCallBack.onUploadSuccess(sdownload_url);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCallBack.onUploadFailed(e);
            }
        });
    }

    public void uploadImageToFirebaseStorage(Uri uri, final OnCallBack onCallBack){

        if(context instanceof AddCaptionPicActivity){
            path = companyID + "/Chat/Images/";
        }
        else if(context instanceof LeaveFormActivity){
            path = companyID + "/Leave/Images/";
        }

        StorageReference riversRef = FirebaseStorage.getInstance().getReference().child(path + System.currentTimeMillis() + "." + getFileExtension(uri));
        riversRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();

                final String sdownload_url = String.valueOf(downloadUrl);

                onCallBack.onUploadSuccess(sdownload_url);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCallBack.onUploadFailed(e);
            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public interface OnCallBack{
        void onUploadSuccess(String imageUrl);
        void onUploadFailed(Exception e);
    }

}
