package com.example.jobhiringmobileapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddJobActivity extends AppCompatActivity {

    private EditText jobTitle, jobPositionToFill, jobSalary, workHours, jobStatus,workPlace, jobDetail, jobRequirement;
    private Button postNewJob;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private String currentUserID, saveCurrentDate, saveCurrentTime;
    private DatabaseReference JobRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");

        mToolbar = (Toolbar) findViewById(R.id.add_job_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("About Job");

        jobTitle = (EditText) findViewById(R.id.job_title);
        jobPositionToFill = (EditText) findViewById(R.id.job_position_to_fill);
        jobSalary = (EditText) findViewById(R.id.job_salary);
        workHours = (EditText) findViewById(R.id.work_hours_per_day);
        jobStatus = (EditText) findViewById(R.id.job_status);
        workPlace = (EditText) findViewById(R.id.work_place);
        jobDetail = (EditText) findViewById(R.id.job_detail);
        jobRequirement = (EditText) findViewById(R.id.job_requirement);
        loadingBar = new ProgressDialog(this);

        postNewJob = (Button) findViewById(R.id.post_new_job);

        postNewJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewJob();
            }
        });
    }

    private void AddNewJob() {
        String job_title = jobTitle.getText().toString();
        String job_position_to_fill = jobPositionToFill.getText().toString();
        String job_salary = jobSalary.getText().toString();
        String work_hours = workHours.getText().toString();
        String job_status = jobStatus.getText().toString();
        String work_place = workPlace.getText().toString();
        String job_detail = jobDetail.getText().toString();
        String job_requirement = jobRequirement.getText().toString();

        if(TextUtils.isEmpty(job_title)){
            Toast.makeText(this, "Please input job title.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(job_position_to_fill) || Integer.parseInt(job_position_to_fill) < 1){
            Toast.makeText(this, "Please input position to fill.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(job_salary)){
            Toast.makeText(this, "Please input job salary.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(work_hours)){
            Toast.makeText(this, "Please input work hours per day.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(job_status)){
            Toast.makeText(this, "Please input job status.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(work_place)){
            Toast.makeText(this, "Please input work place.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(job_detail)){
            Toast.makeText(this, "Please input job detail.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(job_requirement)){
            Toast.makeText(this, "Please input job requirement.", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Adding Job.");
            loadingBar.setMessage("Please wait, while adding job.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            String g_timestamp = ""+System.currentTimeMillis();
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            HashMap jobMap = new HashMap();
            jobMap.put("title", job_title.toLowerCase());
            jobMap.put("positiontofill", job_position_to_fill);
            jobMap.put("salary", job_salary);
            jobMap.put("workhours", work_hours);
            jobMap.put("status", job_status);
            jobMap.put("workplace", work_place);
            jobMap.put("detail", job_detail);
            jobMap.put("requirement", job_requirement);
            jobMap.put("timestamp", g_timestamp);
            jobMap.put("jobId", g_timestamp);
            jobMap.put("date", saveCurrentDate);
            jobMap.put("time", saveCurrentTime);

            JobRef.child(g_timestamp).updateChildren(jobMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToJobManagementActivity();
                        loadingBar.dismiss();
                        Toast.makeText(AddJobActivity.this, "Job Added Successfully.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String message = task.getException().getMessage();
                        loadingBar.dismiss();
                        Toast.makeText(AddJobActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToJobManagementActivity() {
        Intent jobManagementIntent = new Intent(AddJobActivity.this, JobManagementActivity.class);
        jobManagementIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(jobManagementIntent);
        finish();
    }
}