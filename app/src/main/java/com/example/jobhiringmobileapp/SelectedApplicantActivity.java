package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobhiringmobileapp.network.ApiClient;
import com.example.jobhiringmobileapp.network.ApiService;
import com.example.jobhiringmobileapp.notifications.Data;
import com.example.jobhiringmobileapp.notifications.Sender;
import com.example.jobhiringmobileapp.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class SelectedApplicantActivity extends AppCompatActivity {

    private String jobId, uid, currentUserId, saveCurrentDate, saveCurrentTime, fullName, requestType;
    private Toolbar mToolbar;
    private TextView tvApplicant, jobApplicantName, textApplicantDetail, tvDateOFBirth, dateOfBirth, tvBarangay, barangay, tvGender, gender, textOthers;
    private Button disapproveApplicant, approveApplicant, others, viewProfile;
    private CircleImageView applicantProfileImage;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference UserRef, AppliedRef, JobRef;
    private boolean notify = false;

    private int countPositionFilled = 0;
    private String jobPositionToFillString;
    private String jobPositionToFillInt;

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "key=AAAAkVd6jWY:APA91bGaDfuSoVPV6er1FOLE99ZWMIxcckYHazRDlVzT2DbMVVs_XLDtL40cSMWi82Oah7PrzRc7Xt2nHhXdg6RYc5AZ4eLpJgCKfk9hvUTzbTZc5aQva6LiVzMWRttwsTtWGSR3OQWO");

        return headers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_applicant);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Intent jobApplicantIntent = getIntent();
        jobId = jobApplicantIntent.getStringExtra("jobId");
        uid = jobApplicantIntent.getStringExtra("uid");
        requestType = jobApplicantIntent.getStringExtra("request_type");
        fullName = jobApplicantIntent.getStringExtra("fullname");

        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        AppliedRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");
        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");

        mToolbar = (Toolbar) findViewById(R.id.selected_applicant_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Applicant");

        applicantProfileImage = (CircleImageView) findViewById(R.id.applicantProfileImage);
        tvApplicant = (TextView) findViewById(R.id.tvApplicant);
        jobApplicantName = (TextView) findViewById(R.id.jobApplicantName);
        textApplicantDetail = (TextView) findViewById(R.id.textApplicantDetail);
        tvDateOFBirth = (TextView) findViewById(R.id.tvDateOFBirth);
        dateOfBirth = (TextView) findViewById(R.id.dateOfBirth);
        tvBarangay = (TextView) findViewById(R.id.tvBarangay);
        barangay = (TextView) findViewById(R.id.barangay);
        tvGender = (TextView) findViewById(R.id.tvGender);
        gender = (TextView) findViewById(R.id.gender);
        textOthers = (TextView) findViewById(R.id.textOthers);
        disapproveApplicant = (Button) findViewById(R.id.disapproveApplicant);
        approveApplicant = (Button) findViewById(R.id.approveApplicant);
        others = (Button) findViewById(R.id.others);
        viewProfile = (Button) findViewById(R.id.viewProfile);
        loadingBar = new ProgressDialog(this);

        disapproveApplicant.setEnabled(false);
        disapproveApplicant.setBackgroundResource(R.drawable.disable_button);
        LoadApplicantApplication();

        approveApplicant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(approveApplicant.getText().toString().equals("Approve")) {
                    if (jobPositionToFillInt.equals("0")){
                        Toast.makeText(SelectedApplicantActivity.this, "All positions are already filled.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        ApproveApplicant();
                    }
                }
                else{
                    ContactApplicant();
                }
            }
        });

        disapproveApplicant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DisapproveApplicant();

            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(requestType.equals("apply")) {
            Intent jobApplicantIntent = new Intent(SelectedApplicantActivity.this, JobApplicantActivity.class);
            jobApplicantIntent.putExtra("jobId", jobId);
            jobApplicantIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(jobApplicantIntent);
            finish();
        }
        else if (requestType.equals("approved")){
            Intent jobApplicantHiredIntent = new Intent(SelectedApplicantActivity.this, JobApplicantHiredActivity.class);
            jobApplicantHiredIntent.putExtra("jobId", jobId);
            jobApplicantHiredIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(jobApplicantHiredIntent);
            finish();
        }else if (requestType.equals("completed")){
            Intent jobApplicantHiredIntent = new Intent(SelectedApplicantActivity.this, FinishedHiredApplicantActivity.class);
            jobApplicantHiredIntent.putExtra("jobId", jobId);
            jobApplicantHiredIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(jobApplicantHiredIntent);
            finish();
        }
    }

    private void DisapproveApplicant() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SelectedApplicantActivity.this);
        builder.setTitle("Disapprove").setMessage("Do you want to disapprove this this applicant?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadingBar.setTitle("Disapproving");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                String g_timestamp = ""+System.currentTimeMillis();
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                saveCurrentTime = currentTime.format(calForTime.getTime());
                JobRef.child(jobId).child("Applicants").child(uid).child("request_type").setValue("retry").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            HashMap applicantMap = new HashMap();
                            applicantMap.put("request_type", "retry");
                            applicantMap.put("name", "none");
                            applicantMap.put("timestamp", g_timestamp);
                            applicantMap.put("date", saveCurrentDate);
                            applicantMap.put("time", saveCurrentTime);
                            applicantMap.put("url", "none");
                            AppliedRef.child(uid+jobId).updateChildren(applicantMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        approveApplicant.setText("Approve");
                                        disapproveApplicant.setBackgroundResource(R.drawable.disable_button);
                                        disapproveApplicant.setEnabled(false);
                                        loadingBar.dismiss();
                                        Toast.makeText(SelectedApplicantActivity.this, "Successfully disapprove the applicant", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingBar.dismiss();
                                    Toast.makeText(SelectedApplicantActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void ContactApplicant() {
        Intent communicateIntent = new Intent(SelectedApplicantActivity.this, CommunicateActivity.class);
        communicateIntent.putExtra("visit_user_id", uid);
        communicateIntent.putExtra("fullName", fullName);
        startActivity(communicateIntent);
    }

    private void ApproveApplicant() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SelectedApplicantActivity.this);
        builder.setTitle("Approve").setMessage("Do you want to approve this this applicant?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {




                loadingBar.setTitle("Approving");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                String g_timestamp = ""+System.currentTimeMillis();
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                saveCurrentTime = currentTime.format(calForTime.getTime());
                JobRef.child(jobId).child("Applicants").child(uid).child("request_type").setValue("approved").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            HashMap applicantMap = new HashMap();
                            applicantMap.put("request_type", "approved");
                            applicantMap.put("timestamp", g_timestamp);
                            applicantMap.put("date", saveCurrentDate);
                            applicantMap.put("time", saveCurrentTime);
                            AppliedRef.child(uid+jobId).updateChildren(applicantMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        approveApplicant.setText("Contact");
                                        notify = true;
                                        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    Users users = dataSnapshot.getValue(Users.class);
                                                    if (notify) {
                                                        sendNotification(uid, capitalize(users.getFullname()), " approved your job application.");
                                                    }
                                                }
                                                notify = false;
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                        loadingBar.dismiss();
                                        Toast.makeText(SelectedApplicantActivity.this, "Successfully approve the applicant", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingBar.dismiss();
                                    Toast.makeText(SelectedApplicantActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            String e = task.getException().getMessage();
                            Toast.makeText(SelectedApplicantActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void sendNotification(final String uid, final String fullname, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
        Query query = allTokens.orderByKey().equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(currentUserId, capitalize(fullname) + message, "Job Notification", uid, R.drawable.group_icon);

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
                                    Toast.makeText(SelectedApplicantActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                Toast.makeText(SelectedApplicantActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });

                    }
                    catch (Exception e){
                        Toast.makeText(SelectedApplicantActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void LoadApplicantApplication() {

        JobRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    jobPositionToFillString = ""+snapshot.child("positiontofill").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        JobRef.child(jobId).child("Applicants").orderByChild("request_type").equalTo("approved").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    countPositionFilled = (int) snapshot.getChildrenCount();
                    jobPositionToFillInt = String.valueOf(Integer.parseInt(jobPositionToFillString) - countPositionFilled);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        UserRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name = ""+snapshot.child("fullname").getValue();
                    String image = ""+snapshot.child("profileimage").getValue();
                    String dob = ""+snapshot.child("dob").getValue();
                    String barangayString = ""+snapshot.child("barangay").getValue();
                    String genderString = ""+snapshot.child("gender").getValue();
                    String role = ""+snapshot.child("role").getValue();

                    try{
                        Picasso.get().load(image).placeholder(R.drawable.profile).fit().into(applicantProfileImage);
                    }
                    catch (Exception e){
                        applicantProfileImage.setImageResource(R.drawable.profile);
                    }

                    jobApplicantName.setText(capitalize(name));
                    dateOfBirth.setText(dob);
                    barangay.setText(barangayString);
                    gender.setText(genderString);

                    viewProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(SelectedApplicantActivity.this, PersonProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", uid);
                            profileIntent.putExtra("role", "Employer");
                            startActivity(profileIntent);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        AppliedRef.child(uid+jobId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    PDF appliedJob = snapshot.getValue(PDF.class);

                    if(appliedJob.getRequest_type().equals("approved")){
                        approveApplicant.setText("Contact");
                        if (!requestType.equals("completed")) {
                            disapproveApplicant.setEnabled(true);
                            disapproveApplicant.setBackgroundResource(R.drawable.delete_button);
                        }
                    }
                    else if (appliedJob.getRequest_type().equals("apply")){
                        disapproveApplicant.setEnabled(true);
                        disapproveApplicant.setBackgroundResource(R.drawable.delete_button);
                    }

                    if(appliedJob.getName().equals("none") || appliedJob.getUrl().equals("none")){
                        others.setText("No Resume");
                        others.setEnabled(false);
                    }
                    else {
                        others.setText(appliedJob.getName());

                        others.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setType("application/pdf");
                                intent.setData(Uri.parse(appliedJob.getUrl()));
                                startActivity(intent);
                            }
                        });
                    }
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
            if(requestType.equals("apply")) {
                Intent jobApplicantIntent = new Intent(SelectedApplicantActivity.this, JobApplicantActivity.class);
                jobApplicantIntent.putExtra("jobId", jobId);
                jobApplicantIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(jobApplicantIntent);
                finish();
            }
            else if (requestType.equals("approved")){
                Intent jobApplicantHiredIntent = new Intent(SelectedApplicantActivity.this, JobApplicantHiredActivity.class);
                jobApplicantHiredIntent.putExtra("jobId", jobId);
                jobApplicantHiredIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(jobApplicantHiredIntent);
                finish();
            }else if (requestType.equals("completed")){
                Intent jobApplicantHiredIntent = new Intent(SelectedApplicantActivity.this, FinishedHiredApplicantActivity.class);
                jobApplicantHiredIntent.putExtra("jobId", jobId);
                jobApplicantHiredIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(jobApplicantHiredIntent);
                finish();
            }
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