package com.example.jobhiringmobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhiringmobileapp.notifications.Token;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class FinishedHiredApplicantActivity extends AppCompatActivity {

    private String jobId;
    private DatabaseReference JobRef, UserRef, TokenRef;
    private FinishedHiredApplicantAdapter jobApplicantHiredAdapter;
    private RecyclerView jobApplicantHiredList;
    private List<String> jobApplicantHiredUid;
    private List<String> hiredTokens;
    public ImageButton audioCall, videoCall;
    private ArrayList<Users> jobApplicantHiredArrayList;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_hired_applicant);

        Intent finishedRecruitmentIntent = getIntent();
        jobId = finishedRecruitmentIntent.getStringExtra("jobId");

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        TokenRef =  FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");

        mToolbar = (Toolbar) findViewById(R.id.finished_hired_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Hired Applicant");

        jobApplicantHiredList = (RecyclerView) findViewById(R.id.all_finished_applicant_list);
        jobApplicantHiredList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        jobApplicantHiredList.setLayoutManager(linearLayoutManager);

        audioCall = (ImageButton) findViewById(R.id.communicate_audio_call);
        videoCall = (ImageButton) findViewById(R.id.communicate_video_call);

        LoadJobApplicant();
    }

    private void LoadJobApplicant() {
        jobApplicantHiredUid = new ArrayList<>();

        JobRef.child(jobId).child("Hired").orderByChild("request_type").equalTo("approved").addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Toast.makeText(FinishedHiredApplicantActivity.this, "There is no hired applicant.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchMultipleElements(List<String> jobApplicantHiredUid) {
        jobApplicantHiredArrayList = new ArrayList<>();
        hiredTokens = new ArrayList<>();

        if(jobApplicantHiredUid.size() > 0){
            getUidInfo(jobApplicantHiredUid, 0);
            getTokenUid(jobApplicantHiredUid, 0);
        }
    }

    private void getTokenUid(List<String> jobApplicantHiredUid, int index) {
        ValueEventListener singleFetcher = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int nextIndex;
                if(snapshot.exists()) {
                    Token model = snapshot.getValue(Token.class);
                    hiredTokens.add(model.getToken());
                    nextIndex = index + 1;
                    if (nextIndex < jobApplicantHiredUid.size()) {

                        //Step 4: if not, call the recursive function and passing new index
                        getTokenUid(jobApplicantHiredUid, nextIndex);
                    }
                }
                else{
                    nextIndex = index + 1;
                    if (nextIndex < jobApplicantHiredUid.size()) {
                        //Step 4: if not, call the recursive function and passing new index
                        getTokenUid(jobApplicantHiredUid, nextIndex);
                    }
                }

                if(hiredTokens.size() == 0){
                    audioCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(FinishedHiredApplicantActivity.this, "There is no online applicant.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    videoCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(FinishedHiredApplicantActivity.this, "There is no online applicant.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                if(nextIndex >= jobApplicantHiredUid.size()) {
                    if (hiredTokens.size() > 0) {
                        audioCall.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(FinishedHiredApplicantActivity.this, OutgoingInvitationActivity.class);
                                intent.putExtra("selectedUsers", new Gson().toJson(hiredTokens));
                                intent.putExtra("type", "audio");
                                intent.putExtra("isMultiple", true);
                                startActivity(intent);
                            }
                        });

                        videoCall.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(FinishedHiredApplicantActivity.this, OutgoingInvitationActivity.class);
                                intent.putExtra("selectedUsers", new Gson().toJson(hiredTokens));
                                intent.putExtra("type", "video");
                                intent.putExtra("isMultiple", true);
                                startActivity(intent);
                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        TokenRef.child(jobApplicantHiredUid.get(index)).addListenerForSingleValueEvent(singleFetcher);

    }

    private void getUidInfo(List<String> jobApplicantHiredUid, int index) {
        ValueEventListener singleFetcher = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int nextIndex;
                if(snapshot.exists()) {
                    Users model = snapshot.getValue(Users.class);
                    jobApplicantHiredArrayList.add(model);
                    nextIndex = index + 1;

                    if (nextIndex < jobApplicantHiredUid.size()) {

                        //Step 4: if not, call the recursive function and passing new index
                        getUidInfo(jobApplicantHiredUid, nextIndex);
                    }
                    else{
                        if (jobApplicantHiredArrayList.size() > 0) {
                            jobApplicantHiredAdapter = new FinishedHiredApplicantAdapter(FinishedHiredApplicantActivity.this, jobApplicantHiredArrayList, jobId);
                            jobApplicantHiredList.setAdapter(jobApplicantHiredAdapter);
                        }
                    }
                }

                if(jobApplicantHiredArrayList.size() == 0){
                    Toast.makeText(FinishedHiredApplicantActivity.this, "There is no hired applicant.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        UserRef.child(jobApplicantHiredUid.get(index)).addListenerForSingleValueEvent(singleFetcher);
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