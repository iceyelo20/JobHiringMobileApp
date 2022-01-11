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
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FinishedRecruitmentAdapter extends RecyclerView.Adapter<FinishedRecruitmentAdapter.FinishedRecruitmentViewHolder> {

    private Context context;
    private ArrayList<Job> finishedArrayList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference JobRef;

    public FinishedRecruitmentAdapter(Context context, ArrayList<Job> finishedArrayList) {
        this.context = context;
        this.finishedArrayList = finishedArrayList;
    }

    @NonNull
    @Override
    public FinishedRecruitmentAdapter.FinishedRecruitmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posted_job_layout, parent, false);

        return new FinishedRecruitmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FinishedRecruitmentAdapter.FinishedRecruitmentViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Job job = finishedArrayList.get(position);

        String jobTimestamp = job.getTimestamp();
        String jobId = job.getJobId();
        String jobTitle = job.getTitle();
        String jobWorkPlace = job.getWorkplace();
        String jobSalary = job.getSalary();
        String jobDate = job.getDate();
        String jobTime = job.getTime();

        holder.jobTitle.setText(capitalize(jobTitle));
        holder.jobWorkPlace.setText(jobWorkPlace);
        holder.jobSalary.setText("Completed");
        holder.jobDateTime.setText("Posted on "+jobDate+" "+jobTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewJobIntent = new Intent(context, FinishedHiredApplicantActivity.class);
                viewJobIntent.putExtra("jobId", jobId);
                context.startActivity(viewJobIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return finishedArrayList.size();
    }

    public class FinishedRecruitmentViewHolder extends RecyclerView.ViewHolder{

        ImageView appLogo;
        TextView jobTitle, jobWorkPlace, jobSalary, jobDateTime;

        public FinishedRecruitmentViewHolder(@NonNull View itemView) {
            super(itemView);

            appLogo = itemView.findViewById(R.id.posted_job_icon);
            jobTitle = itemView.findViewById(R.id.tvJobTitle);
            jobWorkPlace = itemView.findViewById(R.id.tvJobWorkPlace);
            jobSalary = itemView.findViewById(R.id.tvSalary);
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
