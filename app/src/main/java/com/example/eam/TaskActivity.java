package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.adapter.TaskAdapter;
import com.example.eam.databinding.ActivityTaskBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Project;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TaskActivity extends AppCompatActivity {
    private static final String TAG = "LeaveFormActivity";
    private ActivityTaskBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private List<String> taskKeyList = new ArrayList<>();
    private List<Project> taskList = new ArrayList<>();
    private List<Project> createdTaskList = new ArrayList<>();
    private List<Project> filterList = new ArrayList<>();
    private boolean isMyTasksChosen = true;
    private int filter;
    private String date;
    private boolean filtered;
    private int passedDueAmt;
    private boolean isMyTasksDueSeen = false, isCreatedTasksDueSeen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task);

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

        filtered = false;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.tvNoRecord.setVisibility(View.GONE);

        getTaskList(new OnCallBack() {
            @Override
            public void onSuccess() {
                getCreatedTaskList(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        if (taskList.size() > 0) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                            binding.tvNoRecord.setVisibility(View.GONE);

                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TaskActivity.this);
                            linearLayoutManager.setReverseLayout(true);
                            linearLayoutManager.setStackFromEnd(true);
                            binding.recyclerView.setLayoutManager(linearLayoutManager);
                            TaskAdapter adapter = new TaskAdapter(taskList, TaskActivity.this);
                            binding.recyclerView.setAdapter(adapter);

                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.tvNoRecord.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                        }

                        checkPassedDueTasks(new OnCallBack() {
                            @Override
                            public void onSuccess() {
                                if(passedDueAmt > 0){
                                    binding.ivDue.setVisibility(View.VISIBLE);
                                }
                                else{
                                    binding.ivDue.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onFailed(Exception e) {

                            }
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }

            @Override
            public void onFailed(Exception e) {

            }
        });

        binding.btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TaskActivity.this, TaskFormActivity.class));
            }
        });

        binding.btnMyTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMyTasksChosen = true;

                filterList();

                checkPassedDueTasks(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        if (isMyTasksDueSeen) {
                            binding.ivDue.setVisibility(View.GONE);
                        } else if (passedDueAmt > 0) {
                            binding.ivDue.setVisibility(View.VISIBLE);
                        } else {
                            binding.ivDue.setVisibility(View.GONE);
                        }

//                        if(passedDueAmt > 0){
//                            binding.ivDue.setVisibility(View.VISIBLE);
//                        }
//                        else{
//                            binding.ivDue.setVisibility(View.GONE);
//                        }
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });

                //getTaskList();
                binding.btnCreatedByMe.setCardBackgroundColor(Color.parseColor("#00FFFFFF"));
                binding.tvCreatedByMe.setTextColor(ContextCompat.getColor(TaskActivity.this, R.color.white));
                binding.btnMyTasks.setCardBackgroundColor(ContextCompat.getColor(TaskActivity.this, R.color.white));
                binding.tvMyTasks.setTextColor(ContextCompat.getColor(TaskActivity.this, R.color.colorPrimary));

                float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, getResources().getDisplayMetrics());
                binding.btnCreatedByMe.setRadius(radius);
                binding.btnMyTasks.setRadius(radius);
                binding.btnCreatedByMe.setCardElevation(0);
                binding.btnMyTasks.setCardElevation(0);
            }
        });

        binding.btnCreatedByMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMyTasksChosen = false;

                filterList();

                checkPassedDueTasks(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        if(filter == 3) {

                        }
                        else{
//                            if (isMyTasksChosen) {
//                                if (isMyTasksDueSeen) {
//                                    binding.ivDue.setVisibility(View.GONE);
//                                } else if (passedDueAmt > 0) {
//                                    binding.ivDue.setVisibility(View.VISIBLE);
//                                } else {
//                                    binding.ivDue.setVisibility(View.GONE);
//                                }
//                            } else {
                                if (isCreatedTasksDueSeen) {
                                    binding.ivDue.setVisibility(View.GONE);
                                } else if (passedDueAmt > 0) {
                                    binding.ivDue.setVisibility(View.VISIBLE);
                                } else {
                                    binding.ivDue.setVisibility(View.GONE);
                                }
                            //}
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });

                //getCreatedTaskList();
                binding.btnCreatedByMe.setCardBackgroundColor(ContextCompat.getColor(TaskActivity.this, R.color.white));
                binding.tvCreatedByMe.setTextColor(ContextCompat.getColor(TaskActivity.this, R.color.colorPrimary));
                binding.btnMyTasks.setCardBackgroundColor(Color.parseColor("#00FFFFFF"));
                binding.tvMyTasks.setTextColor(ContextCompat.getColor(TaskActivity.this, R.color.white));

                float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, getResources().getDisplayMetrics());
                binding.btnCreatedByMe.setRadius(radius);
                binding.btnMyTasks.setRadius(radius);
                binding.btnCreatedByMe.setCardElevation(0);
                binding.btnMyTasks.setCardElevation(0);
            }
        });

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetShow();
            }
        });

        binding.btnDue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMyTasksChosen){
                    isMyTasksDueSeen = true;
                }
                else{
                    isCreatedTasksDueSeen = true;
                }

                binding.ivDue.setVisibility(View.GONE);

                filtered = true;
                filter = 3;

                filterList();

                Log.e(TAG, "filter: " + filter);
            }
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        binding.recyclerView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoRecord.setVisibility(View.GONE);

        ArrayList<Project> searchList = new ArrayList<>();

        if(isMyTasksChosen) {
            for (Project project : taskList) {
                if (project.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    searchList.add(project);
                }
            }
        }
        else{
            for (Project project : createdTaskList) {
                if (project.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    searchList.add(project);
                }
            }
        }

        if(searchList.size() > 0) {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            binding.tvNoRecord.setVisibility(View.GONE);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TaskActivity.this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            binding.recyclerView.setLayoutManager(linearLayoutManager);
            TaskAdapter adapter = new TaskAdapter(searchList, TaskActivity.this);
            binding.recyclerView.setAdapter(adapter);
        }
        else{
            binding.recyclerView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            binding.tvNoRecord.setVisibility(View.VISIBLE);
        }
    }

    private void checkPassedDueTasks(final OnCallBack onCallBack) {
        passedDueAmt = 0;

        if(isMyTasksChosen) {
            for (Project project : taskList) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                try {
                    Date dueDate = sdf.parse(project.getDueDate());
                    Date currDate = new Date();
                    Date dueTime = sdf2.parse(project.getDueTime());
                    String currTime1 = sdf2.format(currDate);
                    Date currTime = sdf2.parse(currTime1);

                    if (currDate.after(dueDate) || currTime.after(dueTime)) {
                        if(project.getStatus().equals("Pending")){
                            passedDueAmt = passedDueAmt + 1;
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            for (Project project : createdTaskList) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                try {
                    Date dueDate = sdf.parse(project.getDueDate());
                    Date currDate = new Date();
                    Date dueTime = sdf2.parse(project.getDueTime());
                    String currTime1 = sdf2.format(currDate);
                    Date currTime = sdf2.parse(currTime1);

                    if (currDate.after(dueDate) || currTime.after(dueTime)) {
                        if(project.getStatus().equals("Pending")){
                            passedDueAmt = passedDueAmt + 1;
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        onCallBack.onSuccess();
    }

    private void getTaskList(final OnCallBack onCallBack) {
        reference.child(companyID).child("TaskList").child(firebaseUser.getUid()).child("taskID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskKeyList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String taskKey = snapshot.getKey();
                    taskKeyList.add(taskKey);
                }

                Log.d(TAG, "taskKeyList: " + taskKeyList);
                getTaskInfo();

                onCallBack.onSuccess();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCreatedTaskList(final OnCallBack onCallBack) {
        reference.child(companyID).child("Tasks").orderByChild("creator").equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                createdTaskList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Project project = snapshot.getValue(Project.class);
                    project.setTaskId(snapshot.getKey());

                    createdTaskList.add(project);
                }

                onCallBack.onSuccess();

//                if (taskList.size() > 0) {
//                    binding.recyclerView.setVisibility(View.VISIBLE);
//
//                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TaskActivity.this);
//                    linearLayoutManager.setReverseLayout(true);
//                    linearLayoutManager.setStackFromEnd(true);
//                    binding.recyclerView.setLayoutManager(linearLayoutManager);
//                    TaskAdapter adapter = new TaskAdapter(createdTaskList, TaskActivity.this);
//                    binding.recyclerView.setAdapter(adapter);
//                } else {
//                    //binding.tvNoRecord.setVisibility(View.VISIBLE);
//                    //binding.recyclerView.setVisibility(View.GONE);
//                }

                Log.d(TAG, "taskKeyList: " + taskKeyList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTaskInfo() {
        taskList.clear();

        for(int i=0; i < taskKeyList.size(); i++) {
            int finalI = i+1;
            reference.child(companyID).child("Tasks").child(taskKeyList.get(i)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        DataSnapshot snapshot = task.getResult();

                        Project project = snapshot.getValue(Project.class);
                        project.setTaskId(snapshot.getKey());
                        //String projectId = snapshot.getKey();

                        //Log.e(TAG, "projectId: " + projectId);

                        taskList.add(project);

//                        if(finalI == taskKeyList.size()) {
//
//                            Log.e(TAG, "finalI: " + finalI + ", taskKeyList.size(): " +taskKeyList.size());
//
//                            if (taskList.size() > 0) {
//                                binding.recyclerView.setVisibility(View.VISIBLE);
//
//                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TaskActivity.this);
//                                linearLayoutManager.setReverseLayout(true);
//                                linearLayoutManager.setStackFromEnd(true);
//                                binding.recyclerView.setLayoutManager(linearLayoutManager);
//                                TaskAdapter adapter = new TaskAdapter(taskList, TaskActivity.this);
//                                binding.recyclerView.setAdapter(adapter);
//                            } else {
//                                //binding.tvNoRecord.setVisibility(View.VISIBLE);
//                                //binding.recyclerView.setVisibility(View.GONE);
//                            }
//                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void bottomSheetShow() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_filter_task, null);

        bottomSheetView.findViewById(R.id.btnAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();

                binding.progressBar.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
                binding.tvNoRecord.setVisibility(View.GONE);

                filtered = false;
                filterList();
            }
        });

        bottomSheetView.findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();

                filtered = true;
                filter = 0;

                getDate(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.tvNoRecord.setVisibility(View.GONE);

                        Log.e(TAG, "date: " + date);
                        filterList();
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });

                //Log.e(TAG, "date: " + date);
                //filterList();
            }
        });

        bottomSheetView.findViewById(R.id.btnDue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();

                filtered = true;
                filter = 1;

                getDate(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.tvNoRecord.setVisibility(View.GONE);

                        Log.e(TAG, "date: " + date);
                        filterList();
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }
        });

        bottomSheetView.findViewById(R.id.btnCreation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();

                filtered = true;
                filter = 2;

                getDate(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.tvNoRecord.setVisibility(View.GONE);

                        Log.e(TAG, "date: " + date);
                        filterList();
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void filterList() {
        filterList.clear();

        if(filtered) {
            if (isMyTasksChosen) {
                switch (filter) {
                    case 0:
                        for (Project project : taskList) {
                            if (project.getStartDate().equals(date)) {
                                filterList.add(project);
                            }
                        }

                        break;

                    case 1:
                        for (Project project : taskList) {
                            if (project.getDueDate().equals(date)) {
                                filterList.add(project);
                            }
                        }

                        break;

                    case 2:
                        for (Project project : taskList) {
                            if (project.getCreateDateTime().equals(date)) {
                                filterList.add(project);
                            }
                        }

                        break;

                    case 3:
                        for (Project project : taskList) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                            try {
                                Date dueDate = sdf.parse(project.getDueDate());
                                Date currDate = new Date();
                                Date dueTime = sdf2.parse(project.getDueTime());
                                String currTime1 = sdf2.format(currDate);
                                Date currTime = sdf2.parse(currTime1);

                                if(currDate.after(dueDate) || currTime.after(dueTime)) {
                                    if(project.getStatus().equals("Pending")){
                                        filterList.add(project);
                                    }
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                }
            } else {
                switch (filter) {
                    case 0:
                        for (Project project : createdTaskList) {
                            if (project.getStartDate().equals(date)) {
                                filterList.add(project);
                            }
                        }

                        break;

                    case 1:
                        for (Project project : createdTaskList) {
                            if (project.getDueDate().equals(date)) {
                                filterList.add(project);
                            }
                        }

                        break;

                    case 2:
                        for (Project project : createdTaskList) {
                            if (project.getCreateDateTime().equals(date)) {
                                filterList.add(project);
                            }
                        }

                        break;

                    case 3:
                        for (Project project : createdTaskList) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                            try {
                                Date dueDate = sdf.parse(project.getDueDate());
                                Date currDate = new Date();
                                Date dueTime = sdf2.parse(project.getDueTime());
                                String currTime1 = sdf2.format(currDate);
                                Date currTime = sdf2.parse(currTime1);

                                if(currDate.after(dueDate) || currTime.after(dueTime)) {
                                    if(project.getStatus().equals("Pending")){
                                        filterList.add(project);
                                    }
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        Log.d(TAG, "filterList: " + filterList);

                        break;
                }
            }

            if (filterList.size() > 0) {
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.tvNoRecord.setVisibility(View.GONE);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TaskActivity.this);
                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);
                binding.recyclerView.setLayoutManager(linearLayoutManager);
                TaskAdapter adapter = new TaskAdapter(filterList, TaskActivity.this);
                binding.recyclerView.setAdapter(adapter);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvNoRecord.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
        }
        else{

            if(isMyTasksChosen) {
                if(taskList.size() > 0) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.tvNoRecord.setVisibility(View.GONE);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TaskActivity.this);
                    linearLayoutManager.setReverseLayout(true);
                    linearLayoutManager.setStackFromEnd(true);
                    binding.recyclerView.setLayoutManager(linearLayoutManager);
                    TaskAdapter adapter = new TaskAdapter(taskList, TaskActivity.this);
                    binding.recyclerView.setAdapter(adapter);
                }
                else{
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoRecord.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                }
            }
            else{
                if(createdTaskList.size() > 0) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.tvNoRecord.setVisibility(View.GONE);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TaskActivity.this);
                    linearLayoutManager.setReverseLayout(true);
                    linearLayoutManager.setStackFromEnd(true);
                    binding.recyclerView.setLayoutManager(linearLayoutManager);
                    TaskAdapter adapter = new TaskAdapter(createdTaskList, TaskActivity.this);
                    binding.recyclerView.setAdapter(adapter);
                }
                else{
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoRecord.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void getDate(final OnCallBack onCallBack) {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(TaskActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                date = day1 + "-" + month1 + "-" + year;

                onCallBack.onSuccess();
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    public interface OnCallBack{
        void onSuccess();
        void onFailed(Exception e);
    }
}