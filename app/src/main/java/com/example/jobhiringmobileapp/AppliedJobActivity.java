package com.example.jobhiringmobileapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AppliedJobActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView appliedJobList;
    private AppliedJobAdapter appliedJobAdapter;
    private ArrayList<PDF> appliedJobArrayList;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference AppliedRef;
    private String currentUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applied_job);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        AppliedRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");

        mToolbar = (Toolbar) findViewById(R.id.applied_job_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Applied Jobs");

        appliedJobList = (RecyclerView) findViewById(R.id.all_applied_jobs_post_list);
        appliedJobList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        appliedJobList.setLayoutManager(linearLayoutManager);

        LoadAppliedJob();
    }

    private void LoadAppliedJob() {
        appliedJobArrayList = new ArrayList<>();

        Query appliedQuery = AppliedRef.orderByChild("uid").equalTo(currentUserId);

        appliedQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    appliedJobArrayList.clear();
                    for(DataSnapshot ds: snapshot.getChildren()){
                        PDF model = ds.getValue(PDF.class);
                        appliedJobArrayList.add(model);
                    }
                    appliedJobAdapter = new AppliedJobAdapter(AppliedJobActivity.this, appliedJobArrayList);
                    appliedJobList.setAdapter(appliedJobAdapter);
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
}