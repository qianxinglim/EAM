package com.example.eam.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.eam.IndvChatActivity;
import com.example.eam.adapter.ChatsAdapter;
import com.example.eam.model.Chats;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.eam.interfaces.OnReadChatCallBack;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

public class ChatService {
    private Context context;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private String receiverID;
    private SessionManager sessionManager;
    private String companyID;

    public ChatService(Context context, String receiverID) {
        this.context = context;
        this.receiverID = receiverID;

        sessionManager = new SessionManager(context);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);
    }

    public void readChatData(final OnReadChatCallBack onCallBack){
        reference.child(companyID).child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Chats> list = new ArrayList<>();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);

                    try {
                        if (chats != null && chats.getSender().equals(firebaseUser.getUid()) && chats.getReceiver().equals(receiverID) || chats.getReceiver().equals(firebaseUser.getUid()) && chats.getSender().equals(receiverID)) {
                            list.add(chats);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                onCallBack.onReadSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onCallBack.onReadFailed();
            }
        });
    }

    public void sendTextMsg(String text){
        Chats chats = new Chats(
                getCurrentDate(),
                text,
                "",
                "TEXT",
                firebaseUser.getUid(),
                receiverID
        );

        reference.child(companyID).child("Chats").push().setValue(chats).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Log.d("Send", "onSuccess: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Send", "onFailure: " + e.getMessage());
            }
        });

        //Add to ChatList
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(companyID).child("ChatList").child(firebaseUser.getUid()).child(receiverID);
        chatRef.child("chatID").setValue(receiverID);

        //Add to ChatList
        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference(companyID).child("ChatList").child(receiverID).child(firebaseUser.getUid());
        chatRef2.child("chatID").setValue(firebaseUser.getUid());
    }

    public void sendImage(String imageUrl, String text){
        Chats chats = new Chats(
                getCurrentDate(),
                text,
                imageUrl,
                "IMAGE",
                firebaseUser.getUid(),
                receiverID
        );

        reference.child(companyID).child("Chats").push().setValue(chats).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Log.d("Send", "onSuccess: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Send", "onFailure: " + e.getMessage());
            }
        });

        //Add to ChatList
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(companyID).child("ChatList").child(firebaseUser.getUid()).child(receiverID);
        chatRef.child("chatID").setValue(receiverID);

        //Add to ChatList
        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference(companyID).child("ChatList").child(receiverID).child(firebaseUser.getUid());
        chatRef2.child("chatID").setValue(firebaseUser.getUid());
    }

    public void sendDocument(String documentUrl, String fileName){
        Chats chats = new Chats(
                getCurrentDate(),
                fileName,
                documentUrl,
                "DOCUMENT",
                firebaseUser.getUid(),
                receiverID
        );

        reference.child(companyID).child("Chats").push().setValue(chats).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Log.d("Send", "onSuccess: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Send", "onFailure: " + e.getMessage());
            }
        });

        //Add to ChatList
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(companyID).child("ChatList").child(firebaseUser.getUid()).child(receiverID);
        chatRef.child("chatID").setValue(receiverID);

        //Add to ChatList
        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference(companyID).child("ChatList").child(receiverID).child(firebaseUser.getUid());
        chatRef2.child("chatID").setValue(firebaseUser.getUid());
    }

    public void sendVoice(String audioPath){
        Uri uriAudio = Uri.fromFile(new File(audioPath));
        final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child(companyID + "/Chat/Voice/" + System.currentTimeMillis());
        audioRef.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(@NonNull UploadTask.TaskSnapshot audioSnapshot) {
                Task<Uri> urlTask = audioSnapshot.getStorage().getDownloadUrl();
                while(!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();
                String voiceUrl = String.valueOf(downloadUrl);

                Chats chats = new Chats(
                        getCurrentDate(),
                        "",
                        voiceUrl,
                        "VOICE",
                        firebaseUser.getUid(),
                        receiverID
                );

                reference.child(companyID).child("Chats").push().setValue(chats).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void aVoid) {
                        Log.d("Send", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Send", "onFailure: " + e.getMessage());
                    }
                });

                //Add to ChatList
                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(companyID).child("ChatList").child(firebaseUser.getUid()).child(receiverID);
                chatRef.child("chatID").setValue(receiverID);

                //Add to ChatList
                DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference(companyID).child("ChatList").child(receiverID).child(firebaseUser.getUid());
                chatRef2.child("chatID").setValue(firebaseUser.getUid());
            }
        });
    }

    public String getCurrentDate(){
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today = formatter.format(date);

        Calendar currentDateTime = Calendar.getInstance();
        @SuppressLint("SimpleDAteFormat") SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String currentTime = df.format(currentDateTime.getTime());

        return today + ", " + currentTime;
    }


}
