package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditJobActivity extends AppCompatActivity {

    private EditText jobTitle, jobPositionToFill, jobSalary, workHours, jobStatus,workPlace, jobDetail, jobRequirement;
    private Button postUpdateJob;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private String currentUserID, jobId, saveCurrentDate, saveCurrentTime;
    private DatabaseReference JobRef;
    private ProgressDialog loadingBar;
    private String job_position_to_filled;
    private int countPositionFilled = 0;
    private String jobPositionToFillInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        Intent viewJobIntent = getIntent();
        jobId = viewJobIntent.getStringExtra("jobId");

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs").child(jobId);

        mToolbar = (Toolbar) findViewById(R.id.edit_job_page_toolbar);
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

        postUpdateJob = (Button) findViewById(R.id.post_update_job);

        LoadJobInfo();

        postUpdateJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateJob();
            }
        });
    }

    private void LoadJobInfo() {
        JobRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String job_title = ""+snapshot.child("title").getValue();
                    String job_position_to_fill = ""+snapshot.child("positiontofill").getValue();
                    String job_salary = ""+snapshot.child("salary").getValue();
                    String work_hours = ""+snapshot.child("workhours").getValue();
                    String job_status = ""+snapshot.child("status").getValue();
                    String work_place = ""+snapshot.child("workplace").getValue();
                    String job_detail = ""+snapshot.child("detail").getValue();
                    String job_requirement = ""+snapshot.child("requirement").getValue();

                    jobTitle.setText(capitalize(job_title));
                    jobPositionToFill.setText(job_position_to_fill);
                    jobSalary.setText(job_salary);
                    workHours.setText(work_hours);
                    jobStatus.setText(job_status);
                    workPlace.setText(work_place);
                    jobDetail.setText(job_detail);
                    jobRequirement.setText(job_requirement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        JobRef.child("Applicants").orderByChild("request_type").equalTo("approved").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    countPositionFilled = (int) snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void UpdateJob() {


        String job_title = jobTitle.getText().toString();
        job_position_to_filled = jobPositionToFill.getText().toString();
        String job_salary = jobSalary.getText().toString();
        String work_hours = workHours.getText().toString();
        String job_status = jobStatus.getText().toString();
        String work_place = workPlace.getText().toString();
        String job_detail = jobDetail.getText().toString();
        String job_requirement = jobRequirement.getText().toString();

        Log.d("TAG", "UpdateJob: "+job_position_to_filled + countPositionFilled);


        if(TextUtils.isEmpty(job_title)){
            Toast.makeText(this, "Please input job title.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(job_position_to_filled) || Integer.parseInt(job_position_to_filled) < 1){
            Toast.makeText(this, "Please input position to fill.", Toast.LENGTH_SHORT).show();
        }
        else if (Integer.parseInt(job_position_to_filled) < countPositionFilled){
            Toast.makeText(this, "Please remove some of approved applicant.", Toast.LENGTH_SHORT).show();
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
            loadingBar.setTitle("Updating Job Details.");
            loadingBar.setMessage("Please wait, while updating job.");
            loadingBar.show();

            String g_timestamp = ""+System.currentTimeMillis();
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            HashMap jobMap = new HashMap();
            jobMap.put("detail", job_detail);
            jobMap.put("positiontofill", job_position_to_filled);
            jobMap.put("requirement", job_requirement);
            jobMap.put("salary", job_salary);
            jobMap.put("status", job_status);
            jobMap.put("timestamp", g_timestamp);
            jobMap.put("title", job_title.toLowerCase());
            jobMap.put("workhours", work_hours);
            jobMap.put("workplace", work_place);
            jobMap.put("date", saveCurrentDate);
            jobMap.put("time", saveCurrentTime);

            JobRef.updateChildren(jobMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    SendUserToViewJobActivity();
                    loadingBar.dismiss();
                    Toast.makeText(EditJobActivity.this, "Job updated successfully.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingBar.dismiss();
                    Toast.makeText(EditJobActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

//            JobRef.child("detail").setValue(job_detail);
//            JobRef.child("positiontofill").setValue(job_position_to_fill);
//            JobRef.child("requirement").setValue(job_requirement);
//            JobRef.child("salary").setValue(job_salary);
//            JobRef.child("status").setValue(job_status);
//            JobRef.child("timestamp").setValue(g_timestamp);
//            JobRef.child("title").setValue(job_title);
//            JobRef.child("workhours").setValue(work_hours);
//            JobRef.child("workplace").setValue(work_place);
//            JobRef.child("date").setValue(saveCurrentDate);
//            JobRef.child("time").setValue(saveCurrentTime);

        }
    }

    private void SendUserToViewJobActivity() {
        Intent viewJobIntent = new Intent(EditJobActivity.this, ViewJobActivity.class);
        viewJobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        viewJobIntent.putExtra("jobId", jobId);
        startActivity(viewJobIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private String capitalize(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }
}