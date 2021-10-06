package com.example.eam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.eam.adapter.AssignUsersAdapter;
import com.example.eam.adapter.AttachmentAdapter;
import com.example.eam.adapter.CompanyListAdapter;
import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.adapter.SelectedUserAdapter;
import com.example.eam.databinding.ActivityTaskFormBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Company;
import com.example.eam.model.User;
import com.example.eam.service.FirebaseService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskFormActivity extends AppCompatActivity {
    private static final String TAG = "TaskFormActivity";
    private ActivityTaskFormBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private int IMAGE_GALLERY_REQUEST = 111;
    private int READ_FILE_REQUEST = 101;
    private Uri imageUri;
    private List<Uri> imageList = new ArrayList<>();
    private AttachmentAdapter attachmentAdapter;
    private List<User> userList = new ArrayList<>();
    public static List<User> selectedUserList;
    public static List<String> selectedUserIdList;
    public static List<User> removeUserList = new ArrayList<>();
    private AssignUsersAdapter adapter;
    private SelectedUserAdapter selectedUserAdapter;
    private ArrayList<String> imageURLlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task_form);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        if(firebaseUser == null){
            startActivity(new Intent(getApplicationContext(), PhoneLoginActivity.class));
            finish();
        }

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        selectedUserList = new ArrayList<>();
        selectedUserIdList = new ArrayList<>();
        imageURLlist = new ArrayList<>();

        binding.tvStartDate.setText(getCurrentDate());
        binding.tvStartTime.setText(getCurrentTime());
        binding.tvDueDate.setText(getCurrentDate());
        binding.tvDueTime.setText(getCurrentTime());

        binding.tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(binding.tvStartDate);
            }
        });

        binding.tvDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(binding.tvDueDate);
            }
        });

        binding.tvStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(binding.tvStartTime);
            }
        });

        binding.tvDueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(binding.tvDueTime);
            }
        });

        binding.btnAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDocumentPermission();
            }
        });

        binding.btnAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetAssign();
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });

        binding.assignLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetAssign();
            }
        });
    }

    private void bottomSheetAssign() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TaskFormActivity.this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(TaskFormActivity.this).inflate(R.layout.bottom_sheet_assign_person, null);

        RecyclerView recyclerView = (RecyclerView) bottomSheetView.findViewById(R.id.recyclerView);
        getUserList(recyclerView);

        bottomSheetView.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetView.findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*selectedUserList.removeAll(removeUserList);
                Log.d(TAG, "selectedUserList: " + selectedUserList);
                Log.d(TAG, "removeUserList: " + removeUserList);

                if(removeUserList.size() > 0){

                }
                else{
                    bottomSheetDialog.dismiss();
                }*/

                Log.d(TAG, "selectedUserList: " + selectedUserList);
                bottomSheetDialog.dismiss();

                if(selectedUserList.size() > 0){
                    binding.selectedUserRecyclerview.setVisibility(View.VISIBLE);
                    binding.tvPlsSelect.setVisibility(View.GONE);

                    binding.selectedUserRecyclerview.setLayoutManager(new GridLayoutManager(TaskFormActivity.this, 2, GridLayoutManager.VERTICAL, false));

                    selectedUserAdapter = new SelectedUserAdapter(selectedUserList, TaskFormActivity.this, new SelectedUserAdapter.OnClickListener(){
                        @Override
                        public void onBtnDeleteClick(View view, int position) {
                            selectedUserList.remove(position);
                            selectedUserIdList.remove(position);

                            if(selectedUserList.size() > 0){
                                binding.selectedUserRecyclerview.setLayoutManager(new GridLayoutManager(TaskFormActivity.this, 2));
                            }
                            else{
                                binding.selectedUserRecyclerview.setVisibility(View.GONE);
                                binding.tvPlsSelect.setVisibility(View.VISIBLE);
                            }
                            binding.selectedUserRecyclerview.setAdapter(selectedUserAdapter);
                        }
                    });

                    binding.selectedUserRecyclerview.setAdapter(selectedUserAdapter);
                }
                else{
                    binding.selectedUserRecyclerview.setVisibility(View.GONE);
                    binding.tvPlsSelect.setVisibility(View.VISIBLE);
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;

                FrameLayout bottomSheet = (FrameLayout) d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        bottomSheetDialog.show();
    }

    private void getUserList(RecyclerView recyclerView) {
        userList.clear();

        firestore.collection("Companies").document(companyID).collection("Users").orderBy("department", Query.Direction.ASCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    User user = new User(snapshots.getString("id"), snapshots.getString("name"), snapshots.getString("phoneNo"), snapshots.getString("profilePic"), snapshots.getString("email"),"", snapshots.getString("title"), snapshots.getString("department"),snapshots.getString("clockInTime"),snapshots.getString("clockOutTime"),snapshots.getLong("minutesOfWork").intValue());

                    userList.add(user);
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(TaskFormActivity.this));
                adapter = new AssignUsersAdapter(userList, TaskFormActivity.this);
                recyclerView.setAdapter(adapter);

                if (adapter!=null){
                    adapter.notifyItemInserted(0);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void checkDocumentPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_FILE_REQUEST);
        }
        else{
            openIntent();
        }
    }

    private void openIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*|" +
                "application/pdf|" +
                "application/msword|" +
                "application/vnd.ms-powerpoint|" +
                "application/vnd.ms-excel|" +
                "text/plain|" +
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document|" +
                "application/vnd.openxmlformats-officedocument.presentationml.presentation|" +
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String[] mimeTypes = {"image/jpeg","image/png", //image
                "application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                "text/plain",
                "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(TaskFormActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent2.putExtra(MediaStore.EXTRA_OUTPUT,  imageUri);
            intent2.putExtra("listPhotoName", imageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent chooser = Intent.createChooser(intent,"Select Attachment");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {intent2});
        startActivityForResult(chooser, READ_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //From Gallery
        if(requestCode == READ_FILE_REQUEST && resultCode == RESULT_OK && data !=null){
            if(data.getClipData() !=null) {
                int countClipData = data.getClipData().getItemCount();

                int currentImageSelect = 0;

                while (currentImageSelect < countClipData) {
                    imageUri = data.getClipData().getItemAt(currentImageSelect).getUri();
                    imageList.add(imageUri);
                    currentImageSelect = currentImageSelect + 1;
                }
            }
            else{
                imageList.add(data.getData());
            }
        }
        else if(requestCode == READ_FILE_REQUEST && resultCode == RESULT_OK){
            imageList.add(imageUri);
        }

        if(imageList.size() > 0){
            binding.attachmentRecyclerView.setLayoutManager(new GridLayoutManager(this, imageList.size(), GridLayoutManager.VERTICAL, false));

            attachmentAdapter = new AttachmentAdapter(imageList, TaskFormActivity.this, new AttachmentAdapter.OnClickListener(){
                @Override
                public void onBtnDeleteClick(View view, int position) {
                    imageList.remove(position);

                    if(imageList.size() > 0){
                        binding.attachmentRecyclerView.setLayoutManager(new GridLayoutManager(TaskFormActivity.this, imageList.size()));
                    }
                    binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
                }
            });

            binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
        }
    }

    private String getCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        return df.format(date);
    }

    private String getCurrentTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());

        return df.format(date);
    }

    private void getDate(TextView tvDate) {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(TaskFormActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                String day1 = String.valueOf(day), month1 = String.valueOf(month);

                if(day < 10){
                    day1 = "0" + day;
                }
                if(month < 10){
                    month1 = "0" + month;
                }

                String date = day1 + "-" + month1 + "-" + year;
                tvDate.setText(date);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void getTime(TextView tvTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(TaskFormActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                //Calendar calendar = Calendar.getInstance();

                String time = hourOfDay + ":" + minute;

                //Initialize 24 hours time format
                SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                try{
                    Date date = f24Hours.parse(time);

                    //Initialize 12 hours time format
                    SimpleDateFormat f12Hours = new SimpleDateFormat("hh:mm aa");

                    //Set selected time on text view
                    tvTime.setText(f24Hours.format(date));
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }, 12,0, false);

        timePickerDialog.show();
    }

    private void createTask() {
        final ProgressDialog progressDialog = new ProgressDialog(TaskFormActivity.this);
        progressDialog.setMessage("Uploading file...");
        progressDialog.show();

        if(binding.tvTitle.getText().toString().equals("Pls select")){
            Toast.makeText(this, "Please select a leave type.", Toast.LENGTH_SHORT).show();
        }
        else{
            if(imageList.size() > 0){
                for(int i=0; i < imageList.size(); i++){
                    new FirebaseService(TaskFormActivity.this).uploadDocumentToFirebaseStorage(imageList.get(i), new FirebaseService.OnCallBack() {
                        @Override
                        public void onUploadSuccess(String docUrl) {
                            imageURLlist.add(docUrl);

                            if(imageURLlist.size() == imageList.size()){
                                uploadLeaveRequest(imageURLlist);
                            }

                            Log.d(TAG, "docURL: " + docUrl);
                            Log.d(TAG, "imageURLlist: " + imageURLlist);
                        }

                        @Override
                        public void onUploadFailed(Exception e) {
                            Log.e("TAG", "onUploadFailed: " + e);
                        }
                    });
                }
            }
            else{
                uploadLeaveRequest(imageURLlist);
            }
        }


        progressDialog.dismiss();

        Log.d(TAG, "imageURLlist2: " + imageURLlist);
    }

    private void uploadLeaveRequest(List<String> imageURLlist) {
        Map<String,Object> leave = new HashMap<>();
        leave.put("title", binding.tvTitle.getText().toString());
        leave.put("startDate", binding.tvStartDate.getText().toString());
        leave.put("startTime", binding.tvStartTime.getText().toString());
        leave.put("dueDate", binding.tvDueDate.getText().toString());
        leave.put("dueTime", binding.tvDueTime.getText().toString());
        leave.put("note", binding.etDescription.getText().toString());
        leave.put("creator", firebaseUser.getUid());
        leave.put("assignTo", selectedUserIdList);
        leave.put("status", "Pending");
        leave.put("createDateTime", getCurrentDate());
        leave.put("attachments", imageURLlist);

        String taskId = reference.child(companyID).child("Tasks").push().getKey();

        Map<String, Object> childUpdates = new HashMap<>();

        for(int i=0; i < selectedUserIdList.size(); i++){
            childUpdates.put(companyID + "/TaskList/" + selectedUserIdList.get(i) + "/taskID/" + taskId, taskId);
        }

        childUpdates.put(companyID + "/Tasks/" + taskId, leave);

        reference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(TaskFormActivity.this, "Task uploaded successfully.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TaskFormActivity.this, "Fail to send request.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "error: " + e);
            }
        });

        /*reference.child(companyID).child("Tasks").push().setValue(leave).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(TaskFormActivity.this, "Request sent successfully.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TaskFormActivity.this, "Fail to send request.", Toast.LENGTH_SHORT).show();
            }
        });*/
    }
}