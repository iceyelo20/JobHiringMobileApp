package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, applicantStatus, applicantProfName, applicantCountry;
    private CircleImageView applicantProfileImage;
    private Button SendFriendReqButton, DeclineFriendReqButton, SendMessageButton;

    private DatabaseReference friendRequestRef, UserRef, friendRef;
    private FirebaseAuth mAuth;

    private String applicant_fullname;

    private String senderUserID, receiverUserID, userRole, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            senderUserID = mFirebaseUser.getUid();
        }

        Intent selectedApplicantIntent = getIntent();
        receiverUserID = selectedApplicantIntent.getStringExtra("visit_user_id");
        userRole = selectedApplicantIntent.getStringExtra("role");

        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("FriendRequests");
        friendRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Friends");

        InitializeFields();

        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
        DeclineFriendReqButton.setEnabled(false);
        SendMessageButton.setVisibility(View.INVISIBLE);


        if(userRole.equals("Employer") || userRole.equals("Applicant")){
            SendMessageButton.setVisibility(View.VISIBLE);
            SendFriendReqButton.setVisibility(View.INVISIBLE);
        }
        if(userRole.equals("Employer") && senderUserID.equals(receiverUserID)){
            SendMessageButton.setVisibility(View.INVISIBLE);
            SendFriendReqButton.setVisibility(View.INVISIBLE);
        }
        if (senderUserID.equals(receiverUserID)){
            DeclineFriendReqButton.setVisibility(View.INVISIBLE);
            SendFriendReqButton.setVisibility(View.INVISIBLE);
        }
        if(!senderUserID.equals(receiverUserID) && !userRole.equals("Employer")){
            SendFriendReqButton.setVisibility(View.VISIBLE);
            SendFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendReqButton.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friend")){
                        SendFriendRequestToPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")) {
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friend")){
                        UnFriendAnExistingFriend();
                    }
                }
            });
        }

        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("profileimage").exists()) {
                        String applicantProfImage = ""+dataSnapshot.child("profileimage").getValue();
                        try{
                            Picasso.get().load(applicantProfImage).placeholder(R.drawable.profile).fit().into(applicantProfileImage);
                        }
                        catch (Exception e){
                            applicantProfileImage.setImageResource(R.drawable.profile);
                        }
                    }
                    String applicant_username= dataSnapshot.child("username").getValue().toString();
                    String applicant_status = ""+dataSnapshot.child("status").getValue().toString();

                    applicant_fullname = dataSnapshot.child("fullname").getValue().toString();
                    String applicant_country = dataSnapshot.child("barangay").getValue().toString();

                    applicantStatus.setText(applicant_status);
                    userName.setText("@" + applicant_username);
                    applicantProfName.setText(capitalize(applicant_fullname));
                    applicantCountry.setText("Barangay: " + applicant_country);

                    MaintainRequestButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent communicateIntent = new Intent(PersonProfileActivity.this, CommunicateActivity.class);
                communicateIntent.putExtra("visit_user_id", receiverUserID);
                communicateIntent.putExtra("fullName", applicant_fullname);
                startActivity(communicateIntent);
                finish();
            }
        });
    }

    private void UnFriendAnExistingFriend() {
        friendRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                SendFriendReqButton.setText("Send Friend Request");

                                                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        friendRef.child(senderUserID).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRef.child(receiverUserID).child(senderUserID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                friendRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){
                                                                    friendRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){

                                                                                        SendFriendReqButton.setEnabled(true);
                                                                                        CURRENT_STATE = "friend";
                                                                                        SendFriendReqButton.setText("Unfriend");

                                                                                        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendReqButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelFriendRequest() {
        friendRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                SendFriendReqButton.setText("Send Friend Request");

                                                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintainRequestButton() {
        friendRequestRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)){
                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        SendFriendReqButton.setText("Cancel Friend Request");

                        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                        DeclineFriendReqButton.setEnabled(false);
                    }
                    else if(request_type.equals("received")){
                        CURRENT_STATE = "request_received";
                        SendFriendReqButton.setText("Accept Friend Request");

                        DeclineFriendReqButton.setVisibility(View.VISIBLE);
                        DeclineFriendReqButton.setEnabled(true);

                        DeclineFriendReqButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }

                }
                else{
                    friendRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)){
                                CURRENT_STATE = "friend";
                                SendFriendReqButton.setText("Unfriend");

                                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                DeclineFriendReqButton.setEnabled(false);
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

    private void SendFriendRequestToPerson() {
        friendRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                SendFriendReqButton.setText("Cancel Friend Request");

                                                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void InitializeFields() {
        userName = (TextView) findViewById(R.id.person_profile_username);
        applicantStatus = (TextView) findViewById(R.id.person_profile_status);
        applicantProfName = (TextView) findViewById(R.id.person_full_name);
        applicantCountry = (TextView) findViewById(R.id.person_country);
        applicantProfileImage = (CircleImageView) findViewById(R.id.person_profile_image);

        SendMessageButton = (Button) findViewById(R.id.person_send_message_button);
        SendFriendReqButton = (Button) findViewById(R.id.person_friend_request_button);
        DeclineFriendReqButton = (Button) findViewById(R.id.person_decline_friend_request_button);

        CURRENT_STATE = "not_friend";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private String capitalize(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }
}