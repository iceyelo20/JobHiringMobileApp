package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobApplyActivity extends AppCompatActivity {

    private Button uploadPdf, applyJob;

    private StorageReference PDFRef;
    private TextView tvName, tvtUsername;
    private DatabaseReference JobRef, UserRef, AppliedRef;
    private String jobId, currentUserID, saveCurrentDate, saveCurrentTime;

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_apply);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        mToolbar = (Toolbar) findViewById(R.id.applicant_apply_job_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Apply Now");

        Intent applicantViewJobIntent = getIntent();
        jobId = applicantViewJobIntent.getStringExtra("jobId");
        applyJob = (Button) findViewById(R.id.applicant_apply_job);
        uploadPdf = (Button) findViewById(R.id.uploadDocument);
        tvName = (TextView) findViewById(R.id.applicant_name);
        tvtUsername = (TextView) findViewById(R.id.applicant_username);

        PDFRef = FirebaseStorage.getInstance().getReference().child("Applicant PDF");
        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");
        AppliedRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        LoadInfo();

        applyJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApplyWithoutPDF();

            }
        });

        uploadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPDF();
            }
        });

    }

    private void ApplyWithoutPDF() {

        String g_timestamp = ""+System.currentTimeMillis();
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        final ProgressDialog progressDialog = new ProgressDialog(JobApplyActivity.this);
        progressDialog.setTitle("Applying for the job.");
        progressDialog.show();

        PDF pdf = new PDF("none", "none", "apply", currentUserID, saveCurrentDate, saveCurrentTime, g_timestamp, jobId);
        JobRef.child(jobId).child("Applicants").child(currentUserID).child("request_type").setValue("apply").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    AppliedRef.child(currentUserID+jobId).setValue(pdf);
                    Toast.makeText(JobApplyActivity.this, "Successfully applied for the job.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    SendUserToApplicantViewJob();
                }
                else{
                    String message = task.getException().getMessage();
                    progressDialog.dismiss();
                    Toast.makeText(JobApplyActivity.this, ""+message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void LoadInfo() {
        UserRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String fullname = ""+snapshot.child("fullname").getValue();
                    String username = ""+snapshot.child("username").getValue();

                    tvName.setText(capitalize(fullname));
                    tvtUsername.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SelectPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "PDF FILE SELECT"), 12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 12 && resultCode == RESULT_OK && data != null && data.getData() != null){
            uploadPdf.setText(data.getDataString().substring(data.getDataString().lastIndexOf("/") + 1));

            applyJob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadPDF(data.getData());
                }
            });
        }
    }

    private void uploadPDF(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading PDF.");
        progressDialog.setMessage("Please wait, while the PDF is uploading.");
        progressDialog.show();

        String g_timestamp = ""+System.currentTimeMillis();
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        StorageReference reference = PDFRef.child(currentUserID + System.currentTimeMillis() + ".pdf");

        reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri uri = uriTask.getResult();

                PDF pdf = new PDF(uploadPdf.getText().toString(), uri.toString(), "apply", currentUserID, saveCurrentDate, saveCurrentTime, g_timestamp, jobId);
                JobRef.child(jobId).child("Applicants").child(currentUserID).child("request_type").setValue("apply").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            AppliedRef.child(currentUserID+jobId).setValue(pdf);
                        }
                    }
                });


                Toast.makeText(JobApplyActivity.this, "File uploaded successfully.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

                SendUserToApplicantViewJob();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                double progress = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("File uploaded..." + (int) progress + "%");
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

    private void SendUserToApplicantViewJob() {
        Intent applicantJobViewIntent = new Intent(JobApplyActivity.this, ApplicantViewJobActivity.class);
        applicantJobViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        applicantJobViewIntent.putExtra("jobId", jobId);
        startActivity(applicantJobViewIntent);
        finish();
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