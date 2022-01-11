package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewJobActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserId, jobId;
    private DatabaseReference JobRef, AppliedRef;
    private Toolbar mToolbar;

    private TextView jobTitle, jobPositionToFill, jobRequirement, jobSalary, jobStatus, jobWorkHours, jobDetail, jobWorkPlace;
    private ImageButton editJob, removeJob;
    private Button jobApplicant, hiredApplicant, doneHiring;
    private ProgressDialog loadingBar;
    private String jobPositionToFillString;

    private int countPositionFilled = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Intent postedJobIntent = getIntent();
        jobId = postedJobIntent.getStringExtra("jobId");

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");
        AppliedRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");

        mToolbar = (Toolbar) findViewById(R.id.view_job_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("View Job");

        jobTitle = (TextView) findViewById(R.id.job_title);
        jobPositionToFill = (TextView) findViewById(R.id.job_position_to_fill);
        jobRequirement = (TextView) findViewById(R.id.job_requirement);
        jobSalary = (TextView) findViewById(R.id.job_salary);
        jobStatus = (TextView) findViewById(R.id.job_status);
        jobWorkHours = (TextView) findViewById(R.id.work_hours_per_day);
        jobDetail = (TextView) findViewById(R.id.job_detail);
        jobWorkPlace = (TextView) findViewById(R.id.work_place);
        editJob = (ImageButton) findViewById(R.id.job_edit_post);
        removeJob = (ImageButton) findViewById(R.id.job_remove_post);
        loadingBar = new ProgressDialog(this);

        jobApplicant = (Button) findViewById(R.id.applicant_post_job);
        hiredApplicant = (Button) findViewById(R.id.hired_applicant_post_job);
        doneHiring = (Button) findViewById(R.id.mark_as_done_post_job);
        doneHiring.setBackgroundResource(R.drawable.disable_button);
        doneHiring.setEnabled(false);

        LoadJobInfo();

        jobApplicant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent jobApplicantIntent = new Intent(ViewJobActivity.this, JobApplicantActivity.class);
                jobApplicantIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                jobApplicantIntent.putExtra("jobId", jobId);
                startActivity((jobApplicantIntent));
            }
        });

        hiredApplicant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent jobApplicantHiredIntent = new Intent(ViewJobActivity.this, JobApplicantHiredActivity.class);
                jobApplicantHiredIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                jobApplicantHiredIntent.putExtra("jobId", jobId);
                startActivity((jobApplicantHiredIntent));
            }
        });

        doneHiring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoneChecker();
            }
        });

        removeJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Validation();
            }
        });

        editJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent jobEditIntent = new Intent(ViewJobActivity.this, EditJobActivity.class);
                jobEditIntent.putExtra("jobId", jobId);
                startActivity(jobEditIntent);
            }
        });



    }

    private void DoneChecker() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ViewJobActivity.this);
        builder.setTitle("Mark As Done").setMessage("Do you want to finish hiring in this job?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadingBar.setTitle("Removing Job.");
                loadingBar.setMessage("Please wait, while removing job.");
                loadingBar.show();
                JobRef.child(jobId).child("Applicants").orderByChild("request_type").equalTo("approved").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            DatabaseReference JobRef1 = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");
                            for (DataSnapshot ds: snapshot.getChildren()){
                                String keyUid = ds.getKey();
                                String request_type = ""+ds.child("request_type").getValue();
                                HashMap hiredMap = new HashMap();
                                hiredMap.put("request_type", request_type);

                                JobRef1.child(jobId).child("Hired").child(keyUid).setValue(hiredMap);
                            }
                            JobRef1.child(jobId).child("Applicants").removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                JobRef.child(jobId).child("Applicants").orderByChild("request_type").equalTo("apply").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            DatabaseReference AppliedRef1 = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");
                            for (DataSnapshot ds: snapshot.getChildren()){
                                String keyUid = ds.getKey();

                                AppliedRef1.child(keyUid+jobId).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                JobRef.child(jobId).child("Applicants").orderByChild("request_type").equalTo("retry").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            DatabaseReference AppliedRef1 = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");
                            for (DataSnapshot ds: snapshot.getChildren()){
                                String keyUid = ds.getKey();

                                AppliedRef1.child(keyUid+jobId).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                loadingBar.dismiss();
                Toast.makeText(ViewJobActivity.this, "Successfully mark as done.", Toast.LENGTH_SHORT).show();
                Intent jobManagementIntent = new Intent(ViewJobActivity.this, PostedJobsActivity.class);
                jobManagementIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity((jobManagementIntent));
                finish();

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void Validation() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ViewJobActivity.this);
        builder.setTitle("Remove").setMessage("Do you want to remove this posted job?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadingBar.setTitle("Removing Job.");
                loadingBar.setMessage("Please wait, while removing job.");
                loadingBar.show();
                JobRef.child(jobId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            AppliedRef.orderByChild("jobId").equalTo(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            ds.getRef().removeValue();
                                        }
                                        loadingBar.dismiss();
                                        Toast.makeText(ViewJobActivity.this, "Job remove successfully.", Toast.LENGTH_SHORT).show();
                                        Intent postedJobIntent = new Intent(ViewJobActivity.this, PostedJobsActivity.class);
                                        postedJobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity((postedJobIntent));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });;
                        }
                        else{
                            String message = task.getException().getMessage();
                            loadingBar.dismiss();
                            Toast.makeText(ViewJobActivity.this, ""+message, Toast.LENGTH_SHORT).show();
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

    private void LoadJobInfo() {
        JobRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String jobTitleString = ""+snapshot.child("title").getValue();
                    jobPositionToFillString = ""+snapshot.child("positiontofill").getValue();
                    String jobRequirementString = ""+snapshot.child("requirement").getValue();
                    String jobSalaryString = ""+snapshot.child("salary").getValue();
                    String jobStatusString = ""+snapshot.child("status").getValue();
                    String jobWorkHoursString = ""+snapshot.child("workhours").getValue();
                    String jobDetailString = ""+snapshot.child("detail").getValue();
                    String jobWorkPlaceString = ""+snapshot.child("workplace").getValue();
                    String jobPostDate = ""+snapshot.child("date").getValue();
                    String jobPostTime = ""+snapshot.child("time").getValue();


                    jobTitle.setText(capitalize(jobTitleString));
                    jobPositionToFill.setText(jobPositionToFillString);
                    jobRequirement.setText(jobRequirementString);
                    jobSalary.setText(jobSalaryString);
                    jobStatus.setText(jobStatusString);
                    jobWorkHours.setText(jobWorkHoursString + " hour/s");
                    jobDetail.setText(jobDetailString);
                    jobWorkPlace.setText(jobWorkPlaceString);

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
                    String jobPositionToFillInt = String.valueOf(Integer.parseInt(jobPositionToFillString) - countPositionFilled);

                    if (jobPositionToFillInt.equals("0")){
                        doneHiring.setEnabled(true);
                        jobPositionToFill.setText("Completed");
                        doneHiring.setBackgroundResource(R.drawable.delete_button);
                    }
                    else{
                        jobPositionToFill.setText(jobPositionToFillInt);
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