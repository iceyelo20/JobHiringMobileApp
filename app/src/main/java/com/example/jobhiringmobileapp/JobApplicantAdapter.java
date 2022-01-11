package com.example.jobhiringmobileapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class JobApplicantAdapter extends RecyclerView.Adapter<JobApplicantAdapter.JobApplicantViewHolder> {

    private Context context;
    private ArrayList<Users> applicantArrayList;
    private String jobId;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference AppliedRef;

    public JobApplicantAdapter(Context context, ArrayList<Users> applicantArrayList, String jobId) {
        this.context = context;
        this.applicantArrayList = applicantArrayList;
        this.jobId = jobId;
    }

    @NonNull
    @Override
    public JobApplicantAdapter.JobApplicantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_job_applicant_layout, parent, false);
        AppliedRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");
        return new JobApplicantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobApplicantAdapter.JobApplicantViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Users users = applicantArrayList.get(position);


        String date = users.getDate();
        String fullname = users.getFullname();
        String profileimage = users.getProfileimage();
        String username = users.getUsername();
        String uid = users.getUid();

        holder.applicantJobName.setText(capitalize(fullname));
        holder.applicantJobUsername.setText(username);

        AppliedRef.child(uid+jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String date = ""+snapshot.child("date").getValue();
                    String time = ""+snapshot.child("time").getValue();
                    String status = ""+snapshot.child("request_type").getValue();
                    holder.dateTime.setText(date + " " + time);
                    if(status.equals("apply")) {
                        holder.applicantJobStatus.setText("Applied");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try{
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).fit().into(holder.applicantJobProfile);
        }
        catch (Exception e){
            holder.applicantJobProfile.setImageResource(R.drawable.profile);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectedApplicantIntent = new Intent(context, SelectedApplicantActivity.class);
                selectedApplicantIntent.putExtra("jobId", jobId);
                selectedApplicantIntent.putExtra("request_type", "apply");
                selectedApplicantIntent.putExtra("uid", uid);
                selectedApplicantIntent.putExtra("fullname", fullname);
                context.startActivity(selectedApplicantIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return applicantArrayList.size();
    }

    public class JobApplicantViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView applicantJobProfile;
        private TextView applicantJobName, applicantJobUsername, dateTime, applicantJobStatus;

        public JobApplicantViewHolder(@NonNull View itemView) {
            super(itemView);

            applicantJobProfile = itemView.findViewById(R.id.job_applicant_profile);
            applicantJobName = itemView.findViewById(R.id.tvApplicantName);
            applicantJobUsername = itemView.findViewById(R.id.tvApplicantUsername);
            dateTime = itemView.findViewById(R.id.tvDateTime);
            applicantJobStatus = itemView.findViewById(R.id.tvJobStatus);
        }
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