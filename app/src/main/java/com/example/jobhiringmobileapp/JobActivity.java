package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class JobActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView jobList;
    private JobAdapter jobAdapter;
    private ArrayList<Job> jobArrayList;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference JobRef;
    private String currentUserId;
    private FirebaseAuth mAuth;

    private Button appliedJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");

        mToolbar = (Toolbar) findViewById(R.id.job_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Posted Jobs");

        jobList = (RecyclerView) findViewById(R.id.all_jobs_post_list);
        jobList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        jobList.setLayoutManager(linearLayoutManager);

        appliedJob = (Button) findViewById(R.id.applied_jobs);

        LoadPostedJobs(null);

        appliedJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appliedJobIntent = new Intent(JobActivity.this, AppliedJobActivity.class);
                appliedJobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(appliedJobIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type here to search (Job Name)");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                LoadPostedJobs(newText.toLowerCase());

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void LoadPostedJobs(String search) {
        jobArrayList = new ArrayList<>();

        if (search != null) {
            Query searchJob = JobRef.orderByChild("title").startAt(search).endAt(search + "\uf8ff");
            searchJob.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    jobArrayList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (!ds.child("Hired").exists()) {
                            Job model = ds.getValue(Job.class);
                            jobArrayList.add(model);
                        }
                    }
                    jobAdapter = new JobAdapter(JobActivity.this, jobArrayList);
                    jobList.setAdapter(jobAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            JobRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    jobArrayList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (!ds.child("Hired").exists()) {
                            Job model = ds.getValue(Job.class);
                            jobArrayList.add(model);
                        }
                    }
                    jobAdapter = new JobAdapter(JobActivity.this, jobArrayList);
                    jobList.setAdapter(jobAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

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
}