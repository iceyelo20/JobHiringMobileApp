package com.example.jobhiringmobileapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostedJobAdapter extends RecyclerView.Adapter<PostedJobAdapter.PostedJobViewHolder> {

    private Context context;
    private ArrayList<Job> jobArrayList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference JobRef;

    public PostedJobAdapter(Context context, ArrayList<Job> jobArrayList) {
        this.context = context;
        this.jobArrayList = jobArrayList;
    }

    @NonNull
    @Override
    public PostedJobAdapter.PostedJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posted_job_layout, parent, false);

        return new PostedJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostedJobAdapter.PostedJobViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Job job = jobArrayList.get(position);

        String jobTimestamp = job.getTimestamp();
        String jobId = job.getJobId();
        String jobTitle = job.getTitle();
        String jobWorkPlace = job.getWorkplace();
        String jobSalary = job.getSalary();
        String jobDate = job.getDate();
        String jobTime = job.getTime();

        holder.jobTitle.setText(capitalize(jobTitle));
        holder.jobWorkPlace.setText(jobWorkPlace);
        holder.jobSalary.setText(jobSalary);
        holder.jobDateTime.setText("Posted on "+jobDate+" "+jobTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewJobIntent = new Intent(context, ViewJobActivity.class);
                viewJobIntent.putExtra("jobId", jobId);
                context.startActivity(viewJobIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return jobArrayList.size();
    }

    public class PostedJobViewHolder extends RecyclerView.ViewHolder {

        ImageView appLogo;
        TextView jobTitle, jobWorkPlace, jobSalary, jobDateTime;

        public PostedJobViewHolder(@NonNull View itemView) {
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
