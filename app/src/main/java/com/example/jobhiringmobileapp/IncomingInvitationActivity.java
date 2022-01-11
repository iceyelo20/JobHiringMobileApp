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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobhiringmobileapp.network.ApiClient;
import com.example.jobhiringmobileapp.network.ApiService;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    private ImageView imageMeetingType, imageAcceptInvitation, imageRejectInvitation;
    private TextView textFullName, textUsername;
    private CircleImageView textFirstChar;
    private DatabaseReference UserRef;
    private Ringtone r;
    private String senderFullName, senderUsername, meetingType = null;

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
        setContentView(R.layout.activity_incoming_invitation);


        imageMeetingType = (ImageView) findViewById(R.id.incomingImageMeetingType);
        meetingType = getIntent().getStringExtra(REMOTE_MSG_MEETING_TYPE);

        if(meetingType != null){
            if(meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }
            else{
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        }

        textFirstChar = (CircleImageView) findViewById(R.id.incomingFirstChar);
        textFullName = (TextView) findViewById(R.id.textIncomingFullName);
        textUsername = (TextView) findViewById(R.id.textIncomingUsername);

        final String fullName = getIntent().getStringExtra("fullname");
        final String userName = getIntent().getStringExtra("username");
        final String profileImage = getIntent().getStringExtra("profileimage");

        textFullName.setText(capitalize(fullName));
        textUsername.setText("@"+userName);

        try{
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).fit().into(textFirstChar);
        }
        catch (Exception e){
            textFirstChar.setImageResource(R.drawable.profile);
        }

        imageAcceptInvitation = (ImageView) findViewById(R.id.imageAcceptInvitation);
        imageAcceptInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitationResponse(REMOTE_MSG_INVITATION_ACCEPTED, getIntent().getStringExtra(REMOTE_MSG_INVITER_TOKEN));
            }
        });

        imageRejectInvitation = (ImageView) findViewById(R.id.imageRejectInvitation);
        imageRejectInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitationResponse(REMOTE_MSG_INVITATION_REJECTED, getIntent().getStringExtra(REMOTE_MSG_INVITER_TOKEN));
            }
        });


    }

    private void sendInvitationResponse(String type, String receiverToken){
        try {

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(REMOTE_MSG_TYPE, REMOTE_MSG_INVITATION_RESPONSE);
            data.put(REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(REMOTE_MSG_DATA, data);

            body.put(REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(REMOTE_MSG_INVITATION_ACCEPTED)){

                        try {
                            URL serverURL = new URL("https://meet.jit.si");

                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(REMOTE_MSG_MEETING_ROOM));

                            if(meetingType.equals("audio")){
                                builder.setVideoMuted(true);
                            }
                            JitsiMeetActivity.launch(IncomingInvitationActivity.this, builder.build());
                            finish();
                        }
                        catch (Exception e){
                            Toast.makeText(IncomingInvitationActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(IncomingInvitationActivity.this, "Invitation Rejected.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
                else{
                    Toast.makeText(IncomingInvitationActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(IncomingInvitationActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(REMOTE_MSG_INVITATION_CANCELLED)){
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
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
        r = RingtoneManager.getRingtone(IncomingInvitationActivity.this, notification);
        r.play();
        LocalBroadcastManager.getInstance(IncomingInvitationActivity.this).registerReceiver(invitationResponseReceiver, new IntentFilter(REMOTE_MSG_INVITATION_RESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        r.stop();
        LocalBroadcastManager.getInstance(IncomingInvitationActivity.this).unregisterReceiver(invitationResponseReceiver);
    }
}