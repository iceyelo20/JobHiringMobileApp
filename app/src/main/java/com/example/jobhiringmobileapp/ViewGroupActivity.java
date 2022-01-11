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
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewGroupActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ArrayList<GroupChatList> groupChatLists;
    private RecyclerView groupChatListRv;
    private GroupChatListAdapter groupChatListAdapter;
    private LinearLayoutManager linearLayoutManager;
    private CircleImageView CreateNewGroupButton;

    private DatabaseReference GroupRef, UserRef;

    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");

        InitializeFields();

        CreateNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{
                        "Create New Group"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ViewGroupActivity.this);
                builder.setTitle("Group");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            SendUserToCreateGroup();
                        }
                    }
                });
                builder.show();


            }
        });

        FetchGroups();
    }

    private void FetchGroups() {
        groupChatLists = new ArrayList<>();

        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatLists.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.child("Participants").child(currentUserID).exists()){
                        GroupChatList model = ds.getValue(GroupChatList.class);
                        groupChatLists.add(model);
                    }
                }
                groupChatListAdapter = new GroupChatListAdapter(ViewGroupActivity.this, groupChatLists);
                groupChatListRv.setAdapter(groupChatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchGroups(String query) {

        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatLists.size();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.child("Participants").child(currentUserID).exists()){
                        if(ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            GroupChatList model = ds.getValue(GroupChatList.class);
                            groupChatLists.add(model);
                        }
                    }
                }
                groupChatListAdapter = new GroupChatListAdapter(ViewGroupActivity.this, groupChatLists);
                groupChatListRv.setAdapter(groupChatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.view_group_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Group List");

        groupChatListRv = (RecyclerView) findViewById(R.id.group_list);
        CreateNewGroupButton = (CircleImageView) findViewById(R.id.chat_list_create_new_group_button);
        linearLayoutManager = new LinearLayoutManager(this);
        groupChatListRv.setHasFixedSize(true);
        groupChatListRv.setLayoutManager(linearLayoutManager);

    }

    private void SendUserToCreateGroup() {
        Intent createGroupIntent = new Intent(ViewGroupActivity.this, CreateGroupActivity.class);
        createGroupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(createGroupIntent);
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