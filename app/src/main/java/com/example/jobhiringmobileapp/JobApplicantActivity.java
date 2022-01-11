package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobhiringmobileapp.notifications.Token;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class JobApplicantActivity extends AppCompatActivity {

    private CircleImageView applicantProfile;
    private TextView applicantName, applicantUsername, dateTime, jobStatus;
    private String jobId;
    private DatabaseReference JobRef, UserRef;
    private JobApplicantAdapter jobApplicantAdapter;
    private RecyclerView jobApplicantList;
    private List<String> jobApplicantUid;
    private ArrayList<Users> jobApplicantArrayList;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_applicant);

        Intent viewJobIntent = getIntent();
        jobId = viewJobIntent.getStringExtra("jobId");

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.job_applicant_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Job Applicant");

        jobApplicantList = (RecyclerView) findViewById(R.id.all_job_applicant_list);
        jobApplicantList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        jobApplicantList.setLayoutManager(linearLayoutManager);

        LoadJobApplicant();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent viewJobIntent = new Intent(JobApplicantActivity.this, ViewJobActivity.class);
        viewJobIntent.putExtra("jobId", jobId);
        viewJobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(viewJobIntent);
        finish();
    }

    private void LoadJobApplicant() {
        jobApplicantUid = new ArrayList<>();

        JobRef.child(jobId).child("Applicants").orderByChild("request_type").equalTo("apply").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobApplicantUid.clear();
                if(snapshot.exists()){
                    for (DataSnapshot ds: snapshot.getChildren()){
                        String userId = ds.getKey();

                        jobApplicantUid.add(userId);
                    }

                    fetchMultipleElements(jobApplicantUid);

                }
                else{
                    Toast.makeText(JobApplicantActivity.this, "There is no applicant.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchMultipleElements(List<String> jobApplicantUid) {
        jobApplicantArrayList = new ArrayList<>();

        if(jobApplicantUid.size() > 0){
            getUidInfo(jobApplicantUid, 0);
        }
    }

    private void getUidInfo(List<String> jobApplicantUid, int index) {
        ValueEventListener singleFetcher = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int nextIndex;
                if(snapshot.exists()) {
                    Users model = snapshot.getValue(Users.class);
                    jobApplicantArrayList.add(model);
                    nextIndex = index + 1;

                    if (nextIndex < jobApplicantUid.size()) {

                        //Step 4: if not, call the recursive function and passing new index
                        getUidInfo(jobApplicantUid, nextIndex);
                    }
                    else{
                        if (jobApplicantArrayList.size() > 0) {
                            jobApplicantAdapter = new JobApplicantAdapter(JobApplicantActivity.this, jobApplicantArrayList, jobId);
                            jobApplicantList.setAdapter(jobApplicantAdapter);
                        }
                    }
                }

                if(jobApplicantArrayList.size() == 0){
                    Toast.makeText(JobApplicantActivity.this, "There is no applicant.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        UserRef.child(jobApplicantUid.get(index)).addListenerForSingleValueEvent(singleFetcher);
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