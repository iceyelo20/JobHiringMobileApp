package com.example.jobhiringmobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicantViewJobActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserId, jobId;
    private DatabaseReference JobRef;
    private Toolbar mToolbar;

    private TextView jobTitle, jobPositionToFill, jobRequirement, jobSalary, jobStatus, jobWorkHours, jobDetail, jobWorkPlace;
    private Button jobApplicantApply;

    private int countPositionFilled = 0;
    private String jobPositionToFillString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applicant_view_job);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Intent applicantJobIntent = getIntent();
        jobId = applicantJobIntent.getStringExtra("jobId");

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");

        mToolbar = (Toolbar) findViewById(R.id.applicant_view_job_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("View Job");

        jobTitle = (TextView) findViewById(R.id.job_title);
        jobPositionToFill = (TextView) findViewById(R.id.job_position_to_fill);
        jobRequirement = (TextView) findViewById(R.id.job_requirement);
        jobSalary = (TextView) findViewById(R.id.job_salary);
        jobStatus = (TextView) findViewById(R.id.job_status);
        jobWorkHours = (TextView) findViewById(R.id.work_hours_per_day);
        jobDetail = (TextView) findViewById(R.id.job_detail);
        jobWorkPlace = (TextView) findViewById(R.id.work_place);
        jobApplicantApply = (Button) findViewById(R.id.applicant_apply_job);

        LoadJobInfo();


        jobApplicantApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent applyJobIntent = new Intent(ApplicantViewJobActivity.this, JobApplyActivity.class);
                applyJobIntent.putExtra("jobId", jobId);
                startActivity(applyJobIntent);
            }
        });

    }

    private void LoadJobInfo() {
        JobRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String jobTitleString = ""+snapshot.child("title").getValue();
                    jobPositionToFillString = ""+snapshot.child("positiontofill").getValue();
                    String jobRequirementString = ""+snapshot.child("requirement").getValue();
                    String jobSalaryString = ""+snapshot.child("salary").getValue();
                    String jobStatusString = ""+snapshot.child("status").getValue();
                    String jobWorkHoursString = ""+snapshot.child("workhours").getValue();
                    String jobDetailString = ""+snapshot.child("detail").getValue();
                    String jobWorkPlaceString = ""+snapshot.child("workplace").getValue();
                    String jobPostDate = ""+snapshot.child("date").getValue();
                    String jobPostTime = ""+snapshot.child("time").getValue();

                    jobTitle.setText(capitalize(jobTitleString));
                    jobPositionToFill.setText(jobPositionToFillString);
                    jobRequirement.setText(jobRequirementString);
                    jobSalary.setText(jobSalaryString);
                    jobStatus.setText(jobStatusString);
                    jobWorkHours.setText(jobWorkHoursString + " hour/s");
                    jobDetail.setText(jobDetailString);
                    jobWorkPlace.setText(jobWorkPlaceString);

                    if(snapshot.child("Applicants").child(currentUserId).exists()) {

                        if (snapshot.child("Applicants").child(currentUserId).child("request_type").getValue().toString().equals("apply")) {
                            jobApplicantApply.setText("Applied");
                            jobApplicantApply.setBackgroundResource(R.drawable.disable_button);
                            jobApplicantApply.setEnabled(false);
                        } else if (snapshot.child("Applicants").child(currentUserId).child("request_type").getValue().toString().equals("approved")) {
                            jobApplicantApply.setText("Approved");
                            jobApplicantApply.setBackgroundResource(R.drawable.disable_button);
                            jobApplicantApply.setEnabled(false);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        JobRef.child(jobId).child("Applicants").orderByChild("request_type").equalTo("approved").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    countPositionFilled = (int) snapshot.getChildrenCount();
                    String jobPositionToFillInt = String.valueOf(Integer.parseInt(jobPositionToFillString) - countPositionFilled);

                    if (jobPositionToFillInt.equals("0")){
                        jobPositionToFill.setText("Completed");
                    }
                    else{
                        jobPositionToFill.setText(jobPositionToFillInt);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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