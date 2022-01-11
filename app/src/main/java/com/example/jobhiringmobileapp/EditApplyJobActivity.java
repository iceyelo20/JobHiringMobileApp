package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

public class EditApplyJobActivity extends AppCompatActivity {

    private Button uploadPdf, updateApplyJob;

    private StorageReference PDFRef;
    private TextView tvName, tvtUsername;
    private DatabaseReference JobRef, UserRef, AppliedRef;
    private String jobId, currentUserID, saveCurrentDate, saveCurrentTime, requestType;
    private ImageButton cancelApply;

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_apply_job);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        mToolbar = (Toolbar) findViewById(R.id.applicant_edit_apply_job_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Your Application");

        Intent applyJobIntent = getIntent();
        jobId = applyJobIntent.getStringExtra("jobId");
        requestType = applyJobIntent.getStringExtra("request_type");

        updateApplyJob = (Button) findViewById(R.id.applicant_update_apply_job);
        uploadPdf = (Button) findViewById(R.id.uploadDocument);
        tvName = (TextView) findViewById(R.id.applicant_name);
        tvtUsername = (TextView) findViewById(R.id.applicant_username);
        cancelApply = (ImageButton) findViewById(R.id.job_cancel_apply);

        AppliedRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");

        PDFRef = FirebaseStorage.getInstance().getReference().child("Applicant PDF");

        LoadExistingApply();

        updateApplyJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApplyWithoutPDF();

            }
        });

        cancelApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Validation();
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

        final ProgressDialog progressDialog = new ProgressDialog(EditApplyJobActivity.this);
        progressDialog.setTitle("Re-applying for the job.");
        progressDialog.show();

        PDF pdf = new PDF("none", "none", "apply", currentUserID, saveCurrentDate, saveCurrentTime, g_timestamp, jobId);
        JobRef.child(jobId).child("Applicants").child(currentUserID).child("request_type").setValue("apply").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    AppliedRef.child(currentUserID+jobId).setValue(pdf);
                    Toast.makeText(EditApplyJobActivity.this, "Successfully applied for the job.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    SendUserToApplicantViewJob();
                }
                else {
                    String message = task.getException().getMessage();
                    progressDialog.dismiss();
                    Toast.makeText(EditApplyJobActivity.this, ""+message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void Validation() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EditApplyJobActivity.this);
        builder.setTitle("Remove").setMessage("Do you want to remove your application for this job?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JobRef.child(jobId).child("Applicants").child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            AppliedRef.child(currentUserID + jobId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(EditApplyJobActivity.this, "Your application removed successfully.", Toast.LENGTH_SHORT).show();
                                        Intent removeApplyJobIntent = new Intent(EditApplyJobActivity.this, AppliedJobActivity.class);
                                        removeApplyJobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity((removeApplyJobIntent));
                                        finish();
                                    }
                                    else{
                                        String message = task.getException().getMessage();
                                        Toast.makeText(EditApplyJobActivity.this, ""+message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            String message = task.getException().getMessage();
                            Toast.makeText(EditApplyJobActivity.this, ""+message, Toast.LENGTH_SHORT).show();
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

            updateApplyJob.setEnabled(true);
            updateApplyJob.setBackgroundResource(R.drawable.button);
            updateApplyJob.setOnClickListener(new View.OnClickListener() {
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

                PDF pdf = new PDF(uploadPdf.getText().toString(), uri.toString(), requestType, currentUserID, saveCurrentDate, saveCurrentTime, g_timestamp, jobId);
                JobRef.child(jobId).child("Applicants").child(currentUserID).child("request_type").setValue(requestType).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            AppliedRef.child(currentUserID+jobId).setValue(pdf).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(EditApplyJobActivity.this, "Request follow up successfully.", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();

                                        SendUserToApplicantViewJob();
                                    }
                                    else{
                                        String message = task.getException().getMessage();
                                        progressDialog.dismiss();
                                        Toast.makeText(EditApplyJobActivity.this, ""+message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            String message = task.getException().getMessage();
                            progressDialog.dismiss();
                            Toast.makeText(EditApplyJobActivity.this, ""+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                double progress = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("File uploaded..." + (int) progress + "%");
            }
        });
    }

    private void LoadExistingApply() {
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

        AppliedRef.child(currentUserID+jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String pdfName = ""+snapshot.child("name").getValue();
                    String pdfUrl = ""+snapshot.child("url").getValue();
                    String requestType = ""+snapshot.child("request_type").getValue();

                    if(!pdfName.equals("none") || !pdfUrl.equals("none")){
                        updateApplyJob.setEnabled(false);
                        updateApplyJob.setBackgroundResource(R.drawable.disable_button);
                        uploadPdf.setText(pdfName);
                        uploadPdf.setEnabled(false);
                        uploadPdf.setBackgroundResource(R.drawable.disable_button);
                    }
                    if(requestType.equals("approved") && (pdfName.equals("none") || pdfUrl.equals("none"))){
                        uploadPdf.setText("Upload Resume");
                        uploadPdf.setEnabled(true);
                        uploadPdf.setBackgroundResource(R.drawable.button);
                        updateApplyJob.setEnabled(false);
                        updateApplyJob.setBackgroundResource(R.drawable.disable_button);
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

    private void SendUserToApplicantViewJob() {
        Intent applicantJobViewIntent = new Intent(EditApplyJobActivity.this, AppliedJobActivity.class);
        applicantJobViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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