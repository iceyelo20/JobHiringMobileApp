package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {

    private RecyclerView addParticipantList;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference GroupRef, UserRef;
    private String currentUserID, groupId, myGroupRole;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Users> usersList;
    private AddParticipantAdapter addParticipantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");


        mToolbar = (Toolbar) findViewById(R.id.group_add_participant_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add Participant");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addParticipantList = (RecyclerView) findViewById(R.id.group_add_participant_list);
        linearLayoutManager = new LinearLayoutManager(GroupParticipantAddActivity.this);
        addParticipantList.setHasFixedSize(true);
        addParticipantList.setLayoutManager(linearLayoutManager);

        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();


    }

    private void getAllUsers() {
        usersList = new ArrayList<>();

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Users users = ds.getValue(Users.class);

                    if(!currentUserID.equals(users.getUid())){
                        usersList.add(users);
                    }
                }
                addParticipantAdapter = new AddParticipantAdapter(GroupParticipantAddActivity.this, usersList, ""+groupId, ""+myGroupRole);
                addParticipantList.setAdapter(addParticipantAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        GroupRef.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    getSupportActionBar().setTitle("Add Participant");

                    GroupRef.child(groupId).child("Participants").child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                myGroupRole = ""+snapshot.child("role").getValue();
                                getSupportActionBar().setTitle(groupTitle + " (" + myGroupRole + ")");

                                getAllUsers();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
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