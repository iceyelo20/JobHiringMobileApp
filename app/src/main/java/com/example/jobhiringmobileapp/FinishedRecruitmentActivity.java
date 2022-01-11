package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FinishedRecruitmentActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView finishedList;
    private FinishedRecruitmentAdapter finishedRecruitmentAdapter;
    private ArrayList<Job> finishedArrayList;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference JobRef;
    private String currentUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_recruitment);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");

        mToolbar = (Toolbar) findViewById(R.id.finished_recruitment_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Completed Recruitment");

        finishedList = (RecyclerView) findViewById(R.id.all_finished_recruitment_list);
        finishedList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        finishedList.setLayoutManager(linearLayoutManager);

        LoadPostedJobs(null);
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
        finishedArrayList = new ArrayList<>();

        if (search != null){
            Query searchJob = JobRef.orderByChild("title").startAt(search).endAt(search + "\uf8ff");
            searchJob.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    finishedArrayList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("Hired").exists()) {
                            Job model = ds.getValue(Job.class);
                            finishedArrayList.add(model);
                        }
                    }
                    finishedRecruitmentAdapter = new FinishedRecruitmentAdapter(FinishedRecruitmentActivity.this, finishedArrayList);
                    finishedList.setAdapter(finishedRecruitmentAdapter);
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
                    finishedArrayList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("Hired").exists()) {
                            Job model = ds.getValue(Job.class);
                            finishedArrayList.add(model);
                        }
                    }
                    finishedRecruitmentAdapter = new FinishedRecruitmentAdapter(FinishedRecruitmentActivity.this, finishedArrayList);
                    finishedList.setAdapter(finishedRecruitmentAdapter);
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