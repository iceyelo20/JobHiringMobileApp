package com.example.jobhiringmobileapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription;
    private Toolbar mToolbar;
    private Button DeletePostButton, EditPostButton;
    private DatabaseReference ClickPostRef, UserRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private String PostKey, currentUserID, databaseUserID, image, description, saveCurrentDate, saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Posts").child(PostKey);
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users").child(currentUserID);

        PostImage = (ImageView) findViewById(R.id.update_post_image);
        PostDescription = (TextView) findViewById(R.id.update_post_description);
        DeletePostButton = (Button) findViewById(R.id.delete_post_button);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);
        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.click_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post");

        PostDescription.setFocusable(false);
        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();

                    PostDescription.setText(description);
                    Picasso.get().load(image).into(PostImage);

                    if (currentUserID.equals(databaseUserID)){
                        PostDescription.setFocusableInTouchMode(true);
                        DeletePostButton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }

                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    DeleteCurrentPost();
            }
        });

    }

    private void EditCurrentPost() {
        final String sDescription = PostDescription.getText().toString();

        if(TextUtils.isEmpty(sDescription)){
            Toast.makeText(this, "Please write a description.", Toast.LENGTH_SHORT).show();
        }
        else {

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:s");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            ClickPostRef.child("date").setValue(saveCurrentDate);
            ClickPostRef.child("time").setValue(saveCurrentTime + " Edited");
            ClickPostRef.child("description").setValue(sDescription);
            ClickPostRef.child("serverposttimestamp").setValue(ServerValue.TIMESTAMP);

            UserRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String user_role = dataSnapshot.getValue(String.class);
                    if (user_role.equals("Applicant")) {
                        SendUserToApplicantActivity();
                    } else if (user_role.equals("Employer")) {
                        SendUserToEmployerActivity();
                    }
                    Toast.makeText(ClickPostActivity.this, "Post updated successfully.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void DeleteCurrentPost() {

        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Remove").setMessage("Do you want to remove this post?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadingBar.setTitle("Removing Post.");
                loadingBar.setMessage("Please wait, while removing post.");
                loadingBar.show();
                ClickPostRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            UserRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String user_role = dataSnapshot.getValue(String.class);
                                    if (user_role.equals("Applicant")){
                                        SendUserToApplicantActivity();
                                    }
                                    else if (user_role.equals("Employer")){
                                        SendUserToEmployerActivity();
                                    }
                                    Toast.makeText(ClickPostActivity.this, "Post has been deleted.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        else{
                            String message = task.getException().getMessage();
                            loadingBar.dismiss();
                            Toast.makeText(ClickPostActivity.this, ""+message, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToApplicantActivity() {
        Intent applicantIntent = new Intent(ClickPostActivity.this, ApplicantActivity.class);
        applicantIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity((applicantIntent));
        finish();
    }

    private void SendUserToEmployerActivity() {

        Intent employerIntent = new Intent(ClickPostActivity.this, EmployerActivity.class);
        employerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(employerIntent);
        finish();
    }
}