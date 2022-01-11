package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JHAPPActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_j_h_a_p_p);

        Thread thread = new Thread(){

            public void run(){
                try{
                    sleep(4000);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
                    if(mFirebaseUser != null) {
                        final String currentUserID = mFirebaseUser.getUid();
                        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
                        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                /*sending to setup*/
                                if (!dataSnapshot.hasChild(currentUserID) || !dataSnapshot.child(currentUserID).hasChild("fullname") ||
                                        !dataSnapshot.child(currentUserID).hasChild("username") || !dataSnapshot.child(currentUserID).hasChild("barangay") ||
                                        !dataSnapshot.child(currentUserID).hasChild("role")) {
                                    SendUserToSetupActivity();
                                }
                                else{
                                    UserRef.child(currentUserID).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String user_role = dataSnapshot.getValue().toString();
                                            if (user_role.equals("Applicant")){
                                                SendUserToApplicantActivity();
                                            }
                                            else if (user_role.equals("Employer")){
                                                SendUserToEmployerActivity();
                                            }
                                            else {
                                                Toast.makeText(JHAPPActivity.this, "Invalid login.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    else{
                        SendUserToMainActivity();
                    }

                }
            }
        };thread.start();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(JHAPPActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToSetupActivity() {

        Intent setupIntent = new Intent(JHAPPActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToApplicantActivity() {
        Intent applicantIntent = new Intent(JHAPPActivity.this, ApplicantActivity.class);
        applicantIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity((applicantIntent));
        finish();
    }

    private void SendUserToEmployerActivity() {

        Intent employerIntent = new Intent(JHAPPActivity.this, EmployerActivity.class);
        employerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(employerIntent);
        finish();
    }
}