package com.example.jobhiringmobileapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhiringmobileapp.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity{

    private String groupId, messageSenderID, myGroupRole;

    private Toolbar mToolbar;
    private ImageButton SendMessageButton, SendImageFileButton;
    private EditText userMessageInput;
    private LinearLayoutManager linearLayoutManager;
    private GroupMessagesAdapter groupMessagesAdapter;
    private final List<GroupMessages> groupMessages = new ArrayList<>();
    private RecyclerView groupChatMessagesList;

    private TextView groupName;
    private CircleImageView groupImage;
    private ImageView groupAddParticipant, groupChatOptions;

    private DatabaseReference GroupRef, groupParticipants, tokenList, UserRef;

    private final List<String> groupParticipantList = new ArrayList<>();
    private List<String> groupParticipantTokens;

    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime, tokenIds;

    Boolean CheckIfMessage = false;
    Boolean CheckIfCall = false;

    private static final HashMap<String, String> getRemoteMessageHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "key=AAAAkVd6jWY:APA91bGaDfuSoVPV6er1FOLE99ZWMIxcckYHazRDlVzT2DbMVVs_XLDtL40cSMWi82Oah7PrzRc7Xt2nHhXdg6RYc5AZ4eLpJgCKfk9hvUTzbTZc5aQva6LiVzMWRttwsTtWGSR3OQWO");

        return headers;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            messageSenderID = mFirebaseUser.getUid();
        }

        GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");

        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        Intent getGroupChatIntent = getIntent();
        groupId = getGroupChatIntent.getStringExtra("groupId");

        groupParticipants =  FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups").child(groupId).child("Participants");
        tokenList =  FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");

        InitializeFields();

        fetchGroupInfo();

        FetchGroupMessages();

        loadMyGroupRole();

        groupChatOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupParticipants.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        CheckIfCall = true;
                        groupParticipantList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if(!ds.child("uid").getValue().equals(messageSenderID)) {
                                String userId = ds.child("uid").getValue().toString();
                                groupParticipantList.add(userId);
                            }
                        }

                        fetchMultipleElements(groupParticipantList, null);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        groupAddParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupParticipantAddIntent = new Intent(GroupChatActivity.this, GroupParticipantAddActivity.class);
                groupParticipantAddIntent.putExtra("groupId", groupId);
                startActivity(groupParticipantAddIntent);
            }
        });
    }

    private void loadMyGroupRole() {
        GroupRef.child(groupId).child("Participants").orderByChild("uid").equalTo(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 for(DataSnapshot ds: dataSnapshot.getChildren()){
                     myGroupRole = ""+ds.child("role").getValue();
                 }
                 if(myGroupRole.equals("creator") || myGroupRole.equals("admin")){
                     groupAddParticipant.setVisibility(View.VISIBLE);
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void FetchGroupMessages() {

        GroupRef.child(groupId).child("Messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                if(dataSnapshot.exists()){
                    GroupMessages model = dataSnapshot.getValue(GroupMessages.class);
                    groupMessages.add(model);
                    groupMessagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendMessage() {
        String message = userMessageInput.getText().toString();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please type a message first.", Toast.LENGTH_SHORT).show();
        }
        else{
            String timestamp = ""+System.currentTimeMillis();


            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", ""+messageSenderID);
            hashMap.put("message", ""+message);
            hashMap.put("timestamp", ""+timestamp);
            hashMap.put("time", ""+saveCurrentTime);
            hashMap.put("date", ""+saveCurrentDate);
            hashMap.put("type", ""+"text");

            GroupRef.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    userMessageInput.setText("");
                    CheckIfMessage = true;
                    groupParticipants.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            groupParticipantList.clear();
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                if(!ds.child("uid").getValue().equals(messageSenderID)) {
                                    String userId = ds.child("uid").getValue().toString();
                                    groupParticipantList.add(userId);
                                }
                            }

                            fetchMultipleElements(groupParticipantList, message);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Toast.makeText(GroupChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GroupChatActivity.this, "Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchMultipleElements(List<String> groupParticipantList, String message) {
        groupParticipantTokens = new ArrayList<>();

        if(groupParticipantList.size() > 0){
            getTokenByUid(groupParticipantList, 0, message);
        }
    }

    private void getTokenByUid(final List<String> groupParticipantList, final int index, String message) {
        ValueEventListener singleFetcher = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int nextIndex;
                if(snapshot.exists()) {
                    Token token = snapshot.getValue(Token.class);
                    groupParticipantTokens.add(token.getToken());
                    nextIndex = index + 1;

                    if (nextIndex < groupParticipantList.size()) {

                        //Step 4: if not, call the recursive function and passing new index
                        getTokenByUid(groupParticipantList, nextIndex, message);
                    }
                }
                else{
                    nextIndex = index + 1;
                    if (nextIndex < groupParticipantList.size()) {
                        //Step 4: if not, call the recursive function and passing new index
                        getTokenByUid(groupParticipantList, nextIndex, message);
                    }
                }

                if(groupParticipantTokens.size() == 0){
                    if(CheckIfCall.equals(true)) {
                        CharSequence options[] = new CharSequence[]{
                                "Audio Call", "Video Call"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                        builder.setTitle("Select Option");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Toast.makeText(GroupChatActivity.this, "There is no online user.", Toast.LENGTH_SHORT).show();
                                }
                                if (which == 1) {
                                    Toast.makeText(GroupChatActivity.this, "There is no online user.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.show();

                        CheckIfCall = false;
                    }
                }

                if(nextIndex >= groupParticipantList.size()) {
                    if (groupParticipantTokens.size() > 0) {

                        if (CheckIfCall.equals(true)) {
                            CharSequence options[] = new CharSequence[]{
                                    "Audio Call", "Video Call"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                            builder.setTitle("Select Option");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        Intent intent = new Intent(GroupChatActivity.this, OutgoingInvitationActivity.class);
                                        intent.putExtra("selectedUsers", new Gson().toJson(groupParticipantTokens));
                                        intent.putExtra("type", "audio");
                                        intent.putExtra("isMultiple", true);
                                        startActivity(intent);
                                    }
                                    if (which == 1) {
                                        Intent intent = new Intent(GroupChatActivity.this, OutgoingInvitationActivity.class);
                                        intent.putExtra("selectedUsers", new Gson().toJson(groupParticipantTokens));
                                        intent.putExtra("type", "video");
                                        intent.putExtra("isMultiple", true);
                                        startActivity(intent);
                                    }
                                }
                            });
                            builder.show();

                            CheckIfCall = false;
                        }
                        else if (CheckIfMessage.equals(true)){
                            
                            CheckIfMessage = false;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        tokenList.child(groupParticipantList.get(index)).addListenerForSingleValueEvent(singleFetcher);
    }

    private void fetchGroupInfo() {
        GroupRef.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();

                    groupName.setText(groupTitle);
                    try{
                        Picasso.get().load(groupIcon).placeholder(R.drawable.group_icon).fit().into(groupImage);
                    }
                    catch (Exception e){
                        groupImage.setImageResource(R.drawable.group_icon);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {

        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.group_chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        groupName = (TextView) findViewById(R.id.custom_group_name);
        groupImage = (CircleImageView) findViewById(R.id.custom_group_image);
        groupAddParticipant = (ImageView) findViewById(R.id.custom_group_add_participant);
        groupAddParticipant.setVisibility(View.INVISIBLE);

        groupChatOptions = (ImageView) findViewById(R.id.group_options);

        SendMessageButton = (ImageButton) findViewById(R.id.group_chat_send_message_button);
        userMessageInput = (EditText) findViewById(R.id.group_chat_input_message);

        groupMessagesAdapter = new GroupMessagesAdapter(GroupChatActivity.this,groupMessages);
        groupChatMessagesList = (RecyclerView) findViewById(R.id.group_chat_list_user);
        groupChatMessagesList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        groupChatMessagesList.setHasFixedSize(true);
        groupChatMessagesList.setLayoutManager(linearLayoutManager);
        groupChatMessagesList.setAdapter(groupMessagesAdapter);
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