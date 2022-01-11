package com.example.jobhiringmobileapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppliedJobAdapter extends RecyclerView.Adapter<AppliedJobAdapter.AppliedJobViewHolder> {

    private Context context;
    private ArrayList<PDF> appliedJobArrayList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference JobRef;

    public AppliedJobAdapter(Context context, ArrayList<PDF> appliedJobArrayList) {
        this.context = context;
        this.appliedJobArrayList = appliedJobArrayList;
    }

    @NonNull
    @Override
    public AppliedJobAdapter.AppliedJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_job_applied_layout, parent, false);

        return new AppliedJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppliedJobAdapter.AppliedJobViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        PDF appliedJob = appliedJobArrayList.get(position);

        String date = appliedJob.getDate();
        String jobId = appliedJob.getJobId();
        String name = appliedJob.getName();
        String request_type = appliedJob.getRequest_type();
        String time = appliedJob.getTime();
        String timestamp = appliedJob.getTimestamp();
        String uid = appliedJob.getUid();
        String url = appliedJob.getUrl();

        JobRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Jobs");

        JobRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Job job = snapshot.getValue(Job.class);
                    holder.jobTitle.setText(capitalize(job.getTitle()));
                    holder.jobWorkPlace.setText(job.getWorkplace());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.jobDateTime.setText(date + " " + time);

        if(request_type.equals("apply")){
            holder.jobStatus.setText("Pending");
        }
        else if(request_type.equals("retry")){
            holder.jobStatus.setText("Try Again");
        }
        else if(request_type.equals("approved")){
            holder.jobStatus.setText("Approved");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewAppliedJobIntent = new Intent(context, EditApplyJobActivity.class);
                viewAppliedJobIntent.putExtra("jobId", jobId);
                viewAppliedJobIntent.putExtra("request_type", request_type);
                context.startActivity(viewAppliedJobIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appliedJobArrayList.size();
    }

    public class AppliedJobViewHolder extends RecyclerView.ViewHolder{

        ImageView appLogo;
        TextView jobTitle, jobWorkPlace, jobStatus, jobDateTime;

        public AppliedJobViewHolder(@NonNull View itemView) {
            super(itemView);

            appLogo = itemView.findViewById(R.id.posted_job_icon);
            jobTitle = itemView.findViewById(R.id.tvJobTitle);
            jobWorkPlace = itemView.findViewById(R.id.tvJobWorkPlace);
            jobStatus = itemView.findViewById(R.id.tvJobStatus);
            jobDateTime = itemView.findViewById(R.id.tvDateTime);
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
