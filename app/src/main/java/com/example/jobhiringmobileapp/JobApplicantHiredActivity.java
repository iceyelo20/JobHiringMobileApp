package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class JobApplicantHiredActivity extends AppCompatActivity {

    private String jobId;
    private DatabaseReference JobRef, UserRef;
    private JobApplicantHiredAdapter jobApplicantHiredAdapter;
    private RecyclerView jobApplicantHiredList;
    private List<String> jobApplicantHiredUid;
    private ArrayList<Users> jobApplicantHiredArrayList;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_applicant_hired);

        Intent viewJobIntent = getIntent();
        jobId = viewJobIntent.getStringExtra("jobId");

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.job_applicant_hired_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Hired Applicant");

        jobApplicantHiredList = (RecyclerView) findViewById(R.id.all_job_applicant_hired_list);
        jobApplicantHiredList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        jobApplicantHiredList.setLayoutManager(linearLayoutManager);

        LoadJobApplicant();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent viewJobIntent = new Intent(JobApplicantHiredActivity.this, ViewJobActivity.class);
        viewJobIntent.putExtra("jobId", jobId);
        viewJobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(viewJobIntent);
        finish();
    }

    private void LoadJobApplicant() {
        jobApplicantHiredUid = new ArrayList<>();

        JobRef.child(jobId).child("Applicants").orderByChild("request_type").equalTo("approved").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobApplicantHiredUid.clear();
                if(snapshot.exists()){
                    for (DataSnapshot ds: snapshot.getChildren()){
                        String userId = ds.getKey();

                        jobApplicantHiredUid.add(userId);
                    }
                    fetchMultipleElements(jobApplicantHiredUid);

                }
                else{
                    Toast.makeText(JobApplicantHiredActivity.this, "There is no hired applicant.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchMultipleElements(List<String> jobApplicantUid) {
        jobApplicantHiredArrayList = new ArrayList<>();

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
                    jobApplicantHiredArrayList.add(model);
                    nextIndex = index + 1;

                    if (nextIndex < jobApplicantUid.size()) {

                        //Step 4: if not, call the recursive function and passing new index
                        getUidInfo(jobApplicantUid, nextIndex);
                    }
                    else{
                        if (jobApplicantHiredArrayList.size() > 0) {
                            jobApplicantHiredAdapter = new JobApplicantHiredAdapter(JobApplicantHiredActivity.this, jobApplicantHiredArrayList, jobId);
                            jobApplicantHiredList.setAdapter(jobApplicantHiredAdapter);
                        }
                    }
                }

                if(jobApplicantHiredArrayList.size() == 0){
                    Toast.makeText(JobApplicantHiredActivity.this, "There is no hired applicant.", Toast.LENGTH_SHORT).show();
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