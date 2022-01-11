package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class JobManagementActivity extends AppCompatActivity {

    private Button addNewJob, postedJob, successfulHiredJob;
    private String currentUserID;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_management);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        mToolbar = (Toolbar) findViewById(R.id.job_management_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Job Management");

        addNewJob = (Button) findViewById(R.id.add_new_job);
        postedJob = (Button) findViewById(R.id.posted_jobs);
        successfulHiredJob = (Button) findViewById(R.id.successful_hired_jobs);

        addNewJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addJobIntent = new Intent(JobManagementActivity.this, AddJobActivity.class);
                addJobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(addJobIntent);
            }
        });

        postedJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postedJobIntent = new Intent(JobManagementActivity.this, PostedJobsActivity.class);
                postedJobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(postedJobIntent);
            }
        });

        successfulHiredJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent finishedRecruitmentIntent = new Intent(JobManagementActivity.this, FinishedRecruitmentActivity.class);
                finishedRecruitmentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(finishedRecruitmentIntent);
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
}