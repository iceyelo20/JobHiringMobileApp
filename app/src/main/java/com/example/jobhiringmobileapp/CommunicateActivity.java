package com.example.jobhiringmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.jobhiringmobileapp.listeners.UsersListener;
import com.example.jobhiringmobileapp.network.ApiClient;
import com.example.jobhiringmobileapp.network.ApiService;
import com.example.jobhiringmobileapp.notifications.Data;
import com.example.jobhiringmobileapp.notifications.Sender;
import com.example.jobhiringmobileapp.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class CommunicateActivity extends AppCompatActivity implements UsersListener {

    private Toolbar CommunicateToolbar;
    private ImageButton SendMessageButton, SendImageFileButton;
    private EditText userMessageInput;
    private RecyclerView userMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    public ImageButton audioCall, videoCall;

    private RequestQueue requestQueue;

    private boolean notify = false;

    private String messageReceiverID, messageReceiverName, messageSenderID, saveCurrentDate, saveCurrentTime;

    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef, UserRef;
    private FirebaseAuth mAuth;

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "key=AAAAkVd6jWY:APA91bGaDfuSoVPV6er1FOLE99ZWMIxcckYHazRDlVzT2DbMVVs_XLDtL40cSMWi82Oah7PrzRc7Xt2nHhXdg6RYc5AZ4eLpJgCKfk9hvUTzbTZc5aQva6LiVzMWRttwsTtWGSR3OQWO");

        return headers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            messageSenderID = mFirebaseUser.getUid();
        }

        RootRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();

        InitializeFields();

        DisplayReceiverInfo();

        UserRef.child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Users users = dataSnapshot.getValue(Users.class);

                    audioCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initiateAudioMeeting(users);
                        }
                    });

                    videoCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initiateVideoMeeting(users);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        FetchMessages();
    }


    private void FetchMessages() {


        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SendMessage() {

        notify = true;

        updateUserStatus("online");

        String messageText = userMessageInput.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please type a message first.", Toast.LENGTH_SHORT).show();
        }
        else{
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                    .push();

            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommunicateActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();

                        UserRef.child(messageSenderID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    Users users = dataSnapshot.getValue(Users.class);
                                    if (notify) {
                                        sendNotification(messageReceiverID, capitalize(users.getFullname()), messageText);
                                    }
                                }
                                notify = false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        userMessageInput.setText("");
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(CommunicateActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }

    private void sendNotification(final String messageReceiverID, final String fullname, final String messageText) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
        Query query = allTokens.orderByKey().equalTo(messageReceiverID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(messageSenderID, capitalize(fullname) + ": " + messageText, "New Message", messageReceiverID, R.drawable.profile);

                    Sender sender = new Sender(data, token.getToken());

                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(getRemoteMessageHeaders(), senderJsonObj.toString()).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                                if(response.isSuccessful()){
                                    Log.d("TAG", "onResponse: "+senderJsonObj.toString());
                                }
                                else{
                                    Toast.makeText(CommunicateActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                Toast.makeText(CommunicateActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });

                    }
                    catch (Exception e){
                        Toast.makeText(CommunicateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


//                    try {
//                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
//                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
//                            }
//                        }, new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());
//                            }
//                        }){
//                            @Override
//                            public Map<String, String> getHeaders() throws AuthFailureError {
//                                Map<String, String> headers = new HashMap<>();
//                                headers.put("Content-Type", "application/json");
//                                headers.put("Authorization", "key=AAAAkVd6jWY:APA91bGaDfuSoVPV6er1FOLE99ZWMIxcckYHazRDlVzT2DbMVVs_XLDtL40cSMWi82Oah7PrzRc7Xt2nHhXdg6RYc5AZ4eLpJgCKfk9hvUTzbTZc5aQva6LiVzMWRttwsTtWGSR3OQWO");
//
//                                return super.getHeaders();
//                            }
//                        };
//                        requestQueue.add(jsonObjectRequest);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        UserRef.child(messageSenderID).child("userState").updateChildren(currentStateMap);
    }

    private void DisplayReceiverInfo() {

        if(getIntent().getExtras().get("fullName") != null){
            messageReceiverName = getIntent().getExtras().get("fullName").toString();
            receiverName.setText(capitalize(messageReceiverName));
        }
        else{
            UserRef.child(messageReceiverID).child("fullname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        messageReceiverName = dataSnapshot.getValue().toString();
                        receiverName.setText(capitalize(messageReceiverName));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String profileImage = ""+dataSnapshot.child("profileimage").getValue();
                    try{
                        Picasso.get().load(profileImage).placeholder(R.drawable.profile).fit().into(receiverProfileImage);
                    }
                    catch (Exception e){
                        receiverProfileImage.setImageResource(R.drawable.profile);
                    }

                    final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                    final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(type.equals("online")){
                        userLastSeen.setText("Online");
                    }
                    else{
                        userLastSeen.setText("last seen: " + lastTime + " " + lastDate);
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {

        CommunicateToolbar = (Toolbar) findViewById(R.id.communicate_bar_layout);
        setSupportActionBar(CommunicateToolbar);
        getSupportActionBar().setTitle("");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.communicate_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        audioCall = (ImageButton) findViewById(R.id.communicate_audio_call);
        videoCall = (ImageButton) findViewById(R.id.communicate_video_call);

        receiverName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.messages_list_users);
        userMessageList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);

    }

    @Override
    public void initiateVideoMeeting(Users users) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
        tokenRef.child(users.getUid()).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Intent videoMeetIntent = new Intent(CommunicateActivity.this, OutgoingInvitationActivity.class);
                    videoMeetIntent.putExtra("user", users.getUid());
                    videoMeetIntent.putExtra("type", "video");
                    startActivity(videoMeetIntent);
                }
                else{
                    Toast.makeText(CommunicateActivity.this, capitalize(users.getFullname()) + " is not available right now.", Toast.LENGTH_SHORT).show();
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
                    Intent audioMeetIntent = new Intent(CommunicateActivity.this, OutgoingInvitationActivity.class);
                    audioMeetIntent.putExtra("user", users.getUid());
                    audioMeetIntent.putExtra("type", "audio");
                    startActivity(audioMeetIntent);
                }
                else{
                    Toast.makeText(CommunicateActivity.this, capitalize(users.getFullname()) + " is not available right now.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {

    }

    private String capitalize(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    private void currentUser(String userId){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userId);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser(messageSenderID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentUser("none");
    }
}