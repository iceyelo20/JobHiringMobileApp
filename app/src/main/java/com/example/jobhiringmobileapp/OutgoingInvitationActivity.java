package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.example.jobhiringmobileapp.network.ApiClient;
import com.example.jobhiringmobileapp.network.ApiService;
import com.example.jobhiringmobileapp.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private ImageView imageMeetingType, imageStopInvitation;
    private String receiverID, senderID;
    private TextView textFullName, textUsername;
    private CircleImageView textFirstChar;
    private DatabaseReference UserRef, TokenRef;

    private String fullname;
    private String username;
    private String profileimage;

    private String inviterToken = null, meetingRoom = null, meetingType = null;

    private FirebaseAuth mAuth;

    private int rejectionCount = 0;
    private int totalReceivers = 0;

    private Ringtone r;

    private static final String REMOTE_MSG_TYPE = "type";
    private static final String REMOTE_MSG_INVITATION = "invitation";
    private static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    private static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    private static final String REMOTE_MSG_DATA = "data";
    private static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    private static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    private static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    private static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";

    private static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";

    private static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "key=AAAAkVd6jWY:APA91bGaDfuSoVPV6er1FOLE99ZWMIxcckYHazRDlVzT2DbMVVs_XLDtL40cSMWi82Oah7PrzRc7Xt2nHhXdg6RYc5AZ4eLpJgCKfk9hvUTzbTZc5aQva6LiVzMWRttwsTtWGSR3OQWO");

        return headers;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            senderID = mFirebaseUser.getUid();
        }

        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        TokenRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");



        imageMeetingType = (ImageView) findViewById(R.id.imageMeetingType);
        imageStopInvitation = (ImageView) findViewById(R.id.imageStopInvitation);

        meetingType = getIntent().getStringExtra("type");

        if(meetingType != null){
            if(meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }
            else{
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        }


        textFullName = (TextView) findViewById(R.id.textFullName);
        textUsername = (TextView) findViewById(R.id.textUsername);
        textFirstChar = (CircleImageView) findViewById(R.id.textFirstChar);

        receiverID = getIntent().getStringExtra("user");

        if(receiverID != null){
            UserRef.child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        String username = dataSnapshot.child("username").getValue().toString();
                        String profileimage = ""+dataSnapshot.child("profileimage").getValue();
                        textFullName.setText(capitalize(fullname));
                        textUsername.setText("@"+username);

                        try{
                            Picasso.get().load(profileimage).placeholder(R.drawable.profile).fit().into(textFirstChar);
                        }
                        catch (Exception e){
                            textFirstChar.setImageResource(R.drawable.profile);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        imageStopInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent().getBooleanExtra("isMultiple", false)){
                    Type type = new TypeToken<ArrayList<String>>(){}.getType();
                    ArrayList<String> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);


                    cancelInvitation(null, receivers);
                }
                else{
                    if(receiverID != null){
                        TokenRef.child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if( dataSnapshot.exists()){
                                    Token token = dataSnapshot.getValue(Token.class);

                                    cancelInvitation(token.getToken(), null);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful() && task.getResult()!=null){
                    inviterToken = task.getResult();

                    if( meetingType != null){
                        if(getIntent().getBooleanExtra("isMultiple", false)){
                            Type type = new TypeToken<ArrayList<String>>(){}.getType();
                            ArrayList<String> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                            if(receivers != null){
                                totalReceivers = receivers.size();
                            }

                            initiateMeeting(meetingType, null, receivers);

                        }
                        else{
                            TokenRef.child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        Token token = dataSnapshot.getValue(Token.class);
                                        totalReceivers = 1;
                                        initiateMeeting(meetingType, token.getToken(), null);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                }
            }
        });

    }

    private void initiateMeeting(String meetingType, String receiverToken, ArrayList<String> receivers){
        UserRef.child(senderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);

                fullname = capitalize(users.getFullname());
                username = users.getUsername();
                profileimage = users.getProfileimage();

                try {

                    JSONArray tokens = new JSONArray();
                    if(receiverToken != null){
                        tokens.put(receiverToken);
                    }

                    if (receivers != null && receivers.size() > 0){
                        for (int i = 0; i < receivers.size(); i++){
                            tokens.put(receivers.get(i));

                        }
                        textUsername.setVisibility(View.GONE);
                        textFullName.setText("Calling one to many...");
                    }

                    JSONObject body = new JSONObject();
                    JSONObject data = new JSONObject();

                    data.put(REMOTE_MSG_TYPE, REMOTE_MSG_INVITATION);
                    data.put(REMOTE_MSG_MEETING_TYPE, meetingType);
                    data.put("fullname", fullname);
                    data.put("username", username);
                    data.put("profileimage", profileimage);
                    data.put(REMOTE_MSG_INVITER_TOKEN, inviterToken);

                    meetingRoom = senderID + "_" + UUID.randomUUID().toString().substring(0, 5);

                    data.put(REMOTE_MSG_MEETING_ROOM, meetingRoom);

                    body.put(REMOTE_MSG_DATA, data);
                    body.put(REMOTE_MSG_REGISTRATION_IDS, tokens);


                    sendRemoteMessage(body.toString(), REMOTE_MSG_INVITATION);

                }
                catch (Exception e){
                    Toast.makeText(OutgoingInvitationActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(REMOTE_MSG_INVITATION)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation sent successful.", Toast.LENGTH_SHORT).show();
                    }
                    else if(type.equals(REMOTE_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else{
                    Toast.makeText(OutgoingInvitationActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(OutgoingInvitationActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken, ArrayList<String> receivers){
        try {

            JSONArray tokens = new JSONArray();

            if(receiverToken != null) {
                tokens.put(receiverToken);
            }

            if(receivers != null && receivers.size() > 0){
                for (int i = 0; i < receivers.size(); i++){
                    tokens.put(receivers.get(i));
                }
            }
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(REMOTE_MSG_TYPE, REMOTE_MSG_INVITATION_RESPONSE);
            data.put(REMOTE_MSG_INVITATION_RESPONSE, REMOTE_MSG_INVITATION_CANCELLED);

            body.put(REMOTE_MSG_DATA, data);

            body.put(REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), REMOTE_MSG_INVITATION_RESPONSE);

        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){

                if(type.equals(REMOTE_MSG_INVITATION_ACCEPTED)){
                    try{
                        URL serverURL = new URL("https://meet.jit.si");

                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if(meetingType.equals("audio")){
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this, builder.build());
                        finish();
                    }
                    catch (Exception e){
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else if(type.equals(REMOTE_MSG_INVITATION_REJECTED)){
                    rejectionCount += 1;
                    if (rejectionCount == totalReceivers){
                        Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
            }
        }
    };

    private String capitalize(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(OutgoingInvitationActivity.this, notification);
        r.play();
        LocalBroadcastManager.getInstance(OutgoingInvitationActivity.this).registerReceiver(invitationResponseReceiver, new IntentFilter(REMOTE_MSG_INVITATION_RESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        r.stop();
        LocalBroadcastManager.getInstance(OutgoingInvitationActivity.this).unregisterReceiver(invitationResponseReceiver);
    }
}