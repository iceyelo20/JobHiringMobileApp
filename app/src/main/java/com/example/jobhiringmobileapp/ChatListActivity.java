package com.example.jobhiringmobileapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.jobhiringmobileapp.listeners.UsersListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListActivity extends AppCompatActivity implements UsersListener{

    private RecyclerView myChatList;
    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference messagesRef, userRef;
    private FirebaseAuth mAuth;
    private List<String> chatUid;

    private CircleImageView CreateNewGroupButton;

    private Toolbar mToolbar;
    private String messageSenderID;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Users> chatList;
    private ImageView imageConference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            messageSenderID = mFirebaseUser.getUid();
        }

        messagesRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Messages").child(messageSenderID);
        userRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.chat_list_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Chat List");

        CreateNewGroupButton = (CircleImageView) findViewById(R.id.chat_list_create_new_group_button);
        imageConference = findViewById(R.id.imageConference);
        imageConference.setVisibility(View.GONE);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        myChatList = (RecyclerView) findViewById(R.id.chat_list);
        myChatList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myChatList.setLayoutManager(linearLayoutManager);

        CreateNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{
                        "View Group", "Create New Group"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatListActivity.this);
                builder.setTitle("Group");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            SendUserToViewGroup();
                        }
                        else{
                            SendUserToCreateGroup();
                        }
                    }
                });
                builder.show();


            }
        });

        loadChats();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatList.clear();
                chatListAdapter = new ChatListAdapter(ChatListActivity.this, chatList, ChatListActivity.this);
                myChatList.setAdapter(chatListAdapter);
                loadChats();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type here to search (Full Name)");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                DisplayAllApplicants(newText.toLowerCase());

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void DisplayAllApplicants(String search) {
        chatList = new ArrayList<>();
        Query searchApplicant = userRef.orderByChild("fullname").startAt(search).endAt(search + "\uf8ff");
        searchApplicant.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("role").getValue().toString().equals("Applicant")) {
                        Users model = ds.getValue(Users.class);

                        chatList.add(model);
                    }
                }
                chatListAdapter = new ChatListAdapter(ChatListActivity.this, chatList, ChatListActivity.this);
                myChatList.setAdapter(chatListAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUserStatus("offline");
    }

    private void SendUserToCreateGroup() {
        Intent createGroupIntent = new Intent(ChatListActivity.this, CreateGroupActivity.class);
        createGroupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(createGroupIntent);
    }

    private void SendUserToViewGroup() {
        Intent viewGroupIntent = new Intent(ChatListActivity.this, ViewGroupActivity.class);
        viewGroupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(viewGroupIntent);
    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        userRef.child(messageSenderID).child("userState").updateChildren(currentStateMap);
    }

    private void loadChats() {
        chatUid = new ArrayList<>();

        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatUid.clear();
                if(snapshot.exists()){
                    for (DataSnapshot ds: snapshot.getChildren()){
                        String userId = ds.getKey();

                        chatUid.add(userId);
                    }

                    fetchMultipleElements(chatUid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchMultipleElements(List<String> chatUid) {
        chatList = new ArrayList<>();
        if(chatUid.size() > 0){
            getUidInfo(chatUid, 0);
        }
    }

    private void getUidInfo(List<String> chatUid, int index) {
        ValueEventListener singleFetcher = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int nextIndex;
                if(snapshot.exists()) {
                    Users model = snapshot.getValue(Users.class);
                    chatList.add(model);
                    nextIndex = index + 1;

                    if (nextIndex < chatUid.size()) {

                        //Step 4: if not, call the recursive function and passing new index
                        getUidInfo(chatUid, nextIndex);
                    }
                    else{
                        if (chatList.size() > 0) {
                            chatListAdapter = new ChatListAdapter(ChatListActivity.this, chatList, ChatListActivity.this);
                            myChatList.setAdapter(chatListAdapter);
                        }
                    }
                }

                if(chatList.size() == 0){
                    Toast.makeText(ChatListActivity.this, "There is no chat list.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userRef.child(chatUid.get(index)).addListenerForSingleValueEvent(singleFetcher);
    }



//    private void loadChats() {
//        chatList = new ArrayList<>();
//
//        messagesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                chatList.clear();
//                for(DataSnapshot ds: dataSnapshot.getChildren()){
//                    final String uidKeys = ds.getKey();
//
//                    userRef.child(uidKeys).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            Users users = snapshot.getValue(Users.class);
//                            chatList.add(users);
//
//                            chatListAdapter = new ChatListAdapter(ChatListActivity.this, chatList, ChatListActivity.this);
//                            myChatList.setAdapter(chatListAdapter);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//
//                }
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    @Override
    public void initiateVideoMeeting(Users users) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
        tokenRef.child(users.getUid()).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Intent videoMeetIntent = new Intent(ChatListActivity.this, OutgoingInvitationActivity.class);
                    videoMeetIntent.putExtra("user", users.getUid());
                    videoMeetIntent.putExtra("type", "video");
                    startActivity(videoMeetIntent);
                }
                else{
                    Toast.makeText(ChatListActivity.this, users.getFullname() + " is not available right now.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void initiateAudioMeeting(Users users) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
        tokenRef.child(users.getUid()).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Intent audioMeetIntent = new Intent(ChatListActivity.this, OutgoingInvitationActivity.class);
                    audioMeetIntent.putExtra("user", users.getUid());
                    audioMeetIntent.putExtra("type", "audio");
                    startActivity(audioMeetIntent);
                }
                else{
                    Toast.makeText(ChatListActivity.this, users.getFullname() + " is not available right now.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if(isMultipleUsersSelected){
            imageConference.setVisibility(View.VISIBLE);
            imageConference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatListActivity.this, OutgoingInvitationActivity.class);
                    intent.putExtra("selectedUsers", new Gson().toJson(chatListAdapter.getSelectedUsers()));
                    intent.putExtra("type", "video");
                    intent.putExtra("isMultiple", true);
                    startActivity(intent);
                }
            });
        }
        else{
            imageConference.setVisibility(View.GONE);
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

    private void SendUserToEmployerActivity() {
        Intent employerIntent = new Intent(ChatListActivity.this, EmployerActivity.class);
        startActivity(employerIntent);
    }

    private void SendUserToApplicantActivity() {
        Intent applicantIntent = new Intent(ChatListActivity.this, ApplicantActivity.class);
        startActivity(applicantIntent);
    }

}