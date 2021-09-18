package com.example.eam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.eam.adapter.AttachmentAdapter;
import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.databinding.ActivityLeaveFormBinding;
import com.example.eam.managers.ChatService;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Leave;
import com.example.eam.model.User;
import com.example.eam.service.FirebaseService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;

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
import java.util.Objects;

public class LeaveFormActivity extends AppCompatActivity {
    private static final String TAG = "LeaveFormActivity";
    private ActivityLeaveFormBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference databaseReference;
    private SessionManager sessionManager;
    private int IMAGE_GALLERY_REQUEST = 111;
    private int READ_FILE_REQUEST = 101;
    private String companyID;
    private List<User> list = new ArrayList<>();
    private ArrayAdapter<User> adapter;
    private boolean toggle = true;
    private String chosenDate, chosenTime;
    private boolean isActionShown = false;
    private Uri imageUri;
    private BottomSheetDialog bottomSheetDialog;
    private ChatService chatService;
    private String receiverID;
    private List<Uri> imageList = new ArrayList<>();
    private AttachmentAdapter attachmentAdapter;
    private List<String> imageURLlist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_form);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if(firebaseUser == null){
            startActivity(new Intent(getApplicationContext(),Login.class));
            finish();
        }

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        binding.switchAllDay.setChecked(true);
        chosenDate = getCurrentDate();

        binding.btnTvType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetLeaveDetail();
            }
        });

        binding.btnAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDocumentPermission();




//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("*/*");
//                String[] mimeTypes = {"image/jpeg","image/png", //image
//                        "application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
//                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
//                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
//                        "text/plain",
//                        "application/pdf",
//                        "application/zip"};
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                startActivityForResult(intent, IMAGE_GALLERY_REQUEST);

                //showBottomSheetPickFile();
            }
        });


        //adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //binding.spReviewer.setAdapter(adapter);

        /*firestore.collection("Companies").document(companyID).collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("name") != null && !doc.get("id").equals(firebaseUser.getUid())) {
                        String userID = doc.getString("id");
                        String userName = doc.getString("name");

                        User user = new User();
                        user.setID(userID);
                        user.setName(userName);

                        list.add(user);
                        Log.d(TAG, "User: " + user.getName());
                    }
                }

                adapter.notifyDataSetChanged();

                Log.d(TAG, "User List" + list);
            }
        });*/

//        binding.spReviewer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
//                User user = (User) parent.getSelectedItem();
//                //displayUserData(users);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

//        calcTotal();
        initButtonClick();

    }

    private void bottomSheetLeaveDetail() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(LeaveFormActivity.this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(LeaveFormActivity.this).inflate(R.layout.bottom_sheet_choose_leave_type, null);

        bottomSheetView.findViewById(R.id.btnVacation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.tvLeaveType.setText("Vacation");
                binding.ivType.setVisibility(View.GONE);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetView.findViewById(R.id.btnNonPaidAbsence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.tvLeaveType.setText("Non Paid Absence");
                binding.ivType.setVisibility(View.GONE);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetView.findViewById(R.id.btnSickLeave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.tvLeaveType.setText("Sick Leave");
                binding.ivType.setVisibility(View.GONE);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void displayUserData(User user){
        String userName = user.getName();
        String userID = user.getID();

        String userData = "userID: " + userID + ", Name: " + userName;

        Toast.makeText(this, userData, Toast.LENGTH_SHORT).show();
    }

    /*private void getCurrentTime() {
        SimpleDateFormat dfOri = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");

        try{
            Date timeFrom = dfOri.parse("09:00:00");
            Date timeTo = dfOri.parse("12:00:00");

            binding.tvTimeFrom.setText(df.format(timeFrom));
            binding.tvTimeTo.setText(df.format(timeTo));
        }catch (ParseException e){
            e.printStackTrace();
        }
    }*/

    private void calcTotal() {
        if(toggle){
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

            if(binding.tvDateFrom.getText().toString().equals("Pls select") || binding.tvDateTo.getText().toString().equals("Pls select")){
                binding.tvTotal.setText("0 day");
            }
            else{
                try{
                    Date dateFrom = sdf.parse(binding.tvDateFrom.getText().toString());
                    Date dateTo = sdf.parse(binding.tvDateTo.getText().toString());

                    int days = Days.daysBetween(new LocalDate(dateFrom), new LocalDate(dateTo)).getDays();

                    if(days <= 0){
                        binding.tvDateFrom.setText(chosenDate);
                        binding.tvDateTo.setText(chosenDate);
                        binding.tvTotal.setText("1 day");
                    }
                    else{
                        binding.tvTotal.setText((days + 1) + " days");
                    }

                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }
        else{
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            if(binding.tvTimeFrom.getText().toString().equals("Pls select") || binding.tvTimeTo.getText().toString().equals("Pls select")){
                binding.tvTotal.setText("00:00 Work Hours");
            }
            else{
                try{
                    Date timeFrom = sdf.parse(binding.tvTimeFrom.getText().toString());
                    Date timeTo = sdf.parse(binding.tvTimeTo.getText().toString());

                    if(timeFrom.after(timeTo)) {
                        binding.tvTimeFrom.setText(chosenTime);
                        binding.tvTimeTo.setText(chosenTime);
                        binding.tvTotal.setText("00:00 Work Hours");
                    }
                    else{
                        Interval interval = new Interval(timeFrom.getTime(), timeTo.getTime());
                        Period period = interval.toPeriod();
                        binding.tvTotal.setText(period.getHours() + " hours " + period.getMinutes() + " minutes");
                    }
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void initButtonClick() {
        binding.switchAllDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    toggle = true;
                    binding.lnDate.setVisibility(View.GONE);
                    binding.lnStartDate.setVisibility(View.VISIBLE);
                    binding.lnEndDate.setVisibility(View.VISIBLE);
                    binding.lnStartTime.setVisibility(View.GONE);
                    binding.lnEndTime.setVisibility(View.GONE);
                    calcTotal();
                }
                else{
                    toggle = false;
                    binding.lnDate.setVisibility(View.VISIBLE);
                    binding.lnStartDate.setVisibility(View.GONE);
                    binding.lnEndDate.setVisibility(View.GONE);
                    binding.lnStartTime.setVisibility(View.VISIBLE);
                    binding.lnEndTime.setVisibility(View.VISIBLE);
                    calcTotal();
                }
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLeaveRequest();
            }
        });

        /*binding.btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isActionShown){
//                    binding.layoutActions.setVisibility(View.GONE);
//                    isActionShown = false;
//                } else {
//                    binding.layoutActions.setVisibility(View.VISIBLE);
//                    isActionShown = true;
//                }

                showBottomSheetPickFile();

            }
        });*/

        /*binding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    binding.layoutActions.setVisibility(View.GONE);
                    isActionShown = false;
            }
        });*/

        /*binding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });*/

        binding.tvCurrentdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(binding.tvCurrentdate);
            }
        });

        binding.tvDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(binding.tvDateFrom);
            }
        });

        binding.tvDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(binding.tvDateTo);
            }
        });

        binding.tvTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(binding.tvTimeFrom);
            }
        });

        binding.tvTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(binding.tvTimeTo);
            }
        });

        /*binding.btnDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDocumentPermission();
            }
        });

        binding.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(Intent.ACTION_VIEW);
                //intent.setData(pdfUri);
                //startActivity(intent);
            }
        });*/
    }

    private void showBottomSheetPickFile() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick,null);

        view.findViewById(R.id.btn_doc).setVisibility(View.VISIBLE);

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

        ((View) view.findViewById(R.id.btn_doc)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDocumentPermission();
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
            imageUri = FileProvider.getUriForFile(LeaveFormActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent2.putExtra(MediaStore.EXTRA_OUTPUT,  imageUri);
            intent2.putExtra("listPhotoName", imageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent chooser = Intent.createChooser(intent,"Select Attachment");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {intent2});
        startActivityForResult(chooser, READ_FILE_REQUEST);
    }

    private void selectDocument() {
        String[] mimeTypes = {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                "text/plain",
                "application/pdf",
                "application/zip"};

        Intent intent = new Intent();
        //intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }

        startActivityForResult(Intent.createChooser(intent, "select document"), 109);
    }

    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
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

            attachmentAdapter = new AttachmentAdapter(imageList, LeaveFormActivity.this, new AttachmentAdapter.OnClickListener(){
                @Override
                public void onBtnDeleteClick(View view, int position) {
                    imageList.remove(position);

                    if(imageList.size() > 0){
                        binding.attachmentRecyclerView.setLayoutManager(new GridLayoutManager(LeaveFormActivity.this, imageList.size()));
                    }
                    binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
                }
            });

            binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
        }



//
//        //From Gallery
//        if(requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data !=null){
//            if(data.getClipData() !=null) {
//                int countClipData = data.getClipData().getItemCount();
//
//                int currentImageSelect = 0;
//
//                while (currentImageSelect < countClipData) {
//                    imageUri = data.getClipData().getItemAt(currentImageSelect).getUri();
//                    imageList.add(imageUri);
//                    currentImageSelect = currentImageSelect + 1;
//                }
//            }
//            else{
//                imageList.add(data.getData());
//            }
//
//
//            binding.attachmentRecyclerView.setLayoutManager(new GridLayoutManager(this, imageList.size(), GridLayoutManager.VERTICAL, false));
//
//            attachmentAdapter = new AttachmentAdapter(imageList, LeaveFormActivity.this, new AttachmentAdapter.OnClickListener(){
//                @Override
//                public void onBtnDeleteClick(View view, int position) {
//                    imageList.remove(position);
//
//                    if(imageList.size() > 0){
//                        binding.attachmentRecyclerView.setLayoutManager(new GridLayoutManager(LeaveFormActivity.this, imageList.size()));
//                    }
//                    binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
//                }
//            });
//
//            binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
//        }
//
//        /*if(requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data !=null && data.getData() !=null){
//            imageUri = data.getData();
//
//            try{
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//
//
//                //reviewImage(bitmap);
//                //binding.tvImage.setImageBitmap(bitmap);
//                //binding.tvImage.setVisibility(View.VISIBLE);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }*/
//
//        //From Camera
//        if(requestCode == 440 && resultCode == RESULT_OK){
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                //reviewImage(bitmap);
//                //binding.tvImage.setImageBitmap(bitmap);
//                //binding.tvImage.setVisibility(View.VISIBLE);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//
//        if(requestCode == 109 && resultCode == RESULT_OK && data !=null && data.getData() !=null){
//            pdfUri = data.getData();
//
//            if (pdfUri!=null){
//                String displayName = getUriPath(pdfUri);
//                //binding.layoutDocument.setVisibility(View.VISIBLE);
//                //binding.tvDocumentName.setText(displayName);
//
//
//            }
//        }
    }

    private String getUriPath(Uri pdfUri){
        String uriString = pdfUri.toString();
        File theFile = new File(uriString);
        String filePath = theFile.getAbsolutePath();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(pdfUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = theFile.getName();
        }

        return displayName;
    }

    /*private void reviewImage(Bitmap bitmap) {
        new DialogReviewSendImage(LeaveFormActivity.this,bitmap).show(new DialogReviewSendImage.OnCallBack() {
            @Override
            public void onButtonSendClick() {
                // to Upload Image to firebase storage to get url image...
                if (imageUri!=null){
                    //hide action buttonss
                    binding.layoutActions.setVisibility(View.GONE);
                    isActionShown = false;

                    binding.tvImage.setImageBitmap(bitmap);
                }

            }
        });
    }*/

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    231);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    232);
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

    private void sendLeaveRequest() {
        final ProgressDialog progressDialog = new ProgressDialog(LeaveFormActivity.this);
        progressDialog.setMessage("Uploading file...");
        progressDialog.show();

        if(toggle){
            if(binding.tvLeaveType.getText().toString().equals("Pls select")){
                Toast.makeText(this, "Please select a leave type.", Toast.LENGTH_SHORT).show();
            }
            else if(binding.tvDateFrom.getText().toString().equals("Pls select")){
                Toast.makeText(this, "Please select start date.", Toast.LENGTH_SHORT).show();
            }
            else if(binding.tvDateTo.getText().toString().equals("Pls select")){
                Toast.makeText(this, "Please select end date.", Toast.LENGTH_SHORT).show();
            }
            else{
                if(imageList.size() > 0){
                    for(int i=0; i < imageList.size(); i++){
                        int finalI = i + 1;
                        new FirebaseService(LeaveFormActivity.this).uploadDocumentToFirebaseStorage(imageList.get(i), new FirebaseService.OnCallBack() {
                            @Override
                            public void onUploadSuccess(String docUrl) {
                                imageURLlist.add(docUrl);

                                if(finalI == imageList.size()){
                                    uploadLeaveRequest(imageURLlist);
                                }

                                Log.d(TAG, "docURL: " + docUrl);
                                Log.d(TAG, "imageURLlist: " + imageURLlist);
                                //String displayName = getUriPath(pdfUri);
                                //chatService.sendDocument(pdfUrl, displayName);
                                //progressDialog.dismiss();
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
        }
        else{
            if(binding.tvLeaveType.getText().toString().equals("Pls select")){
                Toast.makeText(this, "Please select a leave type.", Toast.LENGTH_SHORT).show();
            }
            else if(binding.tvCurrentdate.getText().toString().equals("Pls select")){
                Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            }
            else if(binding.tvTimeFrom.getText().toString().equals("Pls select")){
                Toast.makeText(this, "Please select start time.", Toast.LENGTH_SHORT).show();
            }
            else if(binding.tvTimeTo.getText().toString().equals("Pls select")){
                Toast.makeText(this, "Please select end time.", Toast.LENGTH_SHORT).show();
            }
            else{
                if(imageList.size() > 0){
                    for(int i=0; i < imageList.size(); i++){
                        int finalI = i + 1;
                        new FirebaseService(LeaveFormActivity.this).uploadDocumentToFirebaseStorage(imageList.get(i), new FirebaseService.OnCallBack() {
                            @Override
                            public void onUploadSuccess(String docUrl) {
                                imageURLlist.add(docUrl);

                                if(finalI == imageList.size()){
                                    uploadLeaveRequest(imageURLlist);
                                }

                                Log.d(TAG, "docURL: " + docUrl);
                                Log.d(TAG, "imageURLlist: " + imageURLlist);
                                //String displayName = getUriPath(pdfUri);
                                //chatService.sendDocument(pdfUrl, displayName);
                                //progressDialog.dismiss();
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
        }

        progressDialog.dismiss();

        Log.d(TAG, "imageURLlist2: " + imageURLlist);

        //uploadLeaveRequest();

        /*if(imageUri == null && pdfUri == null){
            uploadLeaveRequest(leave);
            progressDialog.dismiss();
            finish();
        }
        else{
            if(imageUri != null){
                new FirebaseService(LeaveFormActivity.this).uploadImageToFirebaseStorage(imageUri, new FirebaseService.OnCallBack() {
                    @Override
                    public void onUploadSuccess(String imageUrl) {
                        leave.put("image", imageUrl);
                        uploadLeaveRequest(leave);
                        progressDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onUploadFailed(Exception e) {
                        Log.e("TAG", "onUploadFailed: " + e);
                    }
                });
            }
//            if(pdfUri != null){
//                new FirebaseService(LeaveFormActivity.this).uploadDocumentToFirebaseStorage(pdfUri, new FirebaseService.OnCallBack() {
//                    @Override
//                    public void onUploadSuccess(String pdfUrl) {
//                        leave.put("document", pdfUrl);
//                    }
//
//                    @Override
//                    public void onUploadFailed(Exception e) {
//                        Log.e("TAG", "onUploadFailed: " + e);
//                    }
//                });
//            }

//            uploadLeaveRequest(leave);
//            progressDialog.dismiss();
//            finish();
        }*/
    }

    private void uploadLeaveRequest(List<String> imageURLlist) {
        Map<String,Object> leave = new HashMap<>();
        leave.put("type", binding.tvLeaveType.getText().toString());
        //leave.put("type", binding.spAbsenceType.getSelectedItem().toString());
        leave.put("duration", binding.tvTotal.getText().toString());
        leave.put("note", binding.etNote.getText().toString());
        leave.put("requester", firebaseUser.getUid());
        leave.put("status", "Pending");
        leave.put("requestDate", getCurrentDate());
        leave.put("requestTime", getCurrentTime());
        leave.put("attachments", imageURLlist);
        //leave.put("reviewer", );

        if(toggle){
            leave.put("fullDay", true);
            leave.put("dateFrom", binding.tvDateFrom.getText().toString());
            leave.put("dateTo", binding.tvDateTo.getText().toString());
        }
        else{
            leave.put("fullDay", false);
            leave.put("date", binding.tvCurrentdate.getText().toString());
            leave.put("timeFrom", binding.tvTimeFrom.getText().toString());
            leave.put("timeTo", binding.tvTimeTo.getText().toString());
        }

        databaseReference.child(companyID).child("Leaves").push().setValue(leave).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(LeaveFormActivity.this, "Request sent successfully.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LeaveFormActivity.this, "Fail to send request.", Toast.LENGTH_SHORT).show();
            }
        });
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(LeaveFormActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                chosenDate = date;
                calcTotal();
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void getTime(TextView tvTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(LeaveFormActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
                    chosenTime = f24Hours.format(date);
                    calcTotal();
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }, 12,0, false);

        timePickerDialog.show();
    }

}