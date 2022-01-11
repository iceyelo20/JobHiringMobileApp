package com.example.jobhiringmobileapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private Context context;
    private ArrayList<Users> applicantArrayList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference UserRef;

    public UsersAdapter(Context context, ArrayList<Users> applicantArrayList) {
        this.context = context;
        this.applicantArrayList = applicantArrayList;
    }

    @NonNull
    @Override
    public UsersAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Users model = applicantArrayList.get(position);

        String date = model.getDate();
        String fullname = model.getFullname();
        String profileimage = model.getProfileimage();
        String username = model.getUsername();
        String uid = model.getUid();
        String role = model.getRole();

        holder.usersDate.setText("Account created: " + model.getDate());
        holder.myUserName.setText(capitalize(fullname));

        UserRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String type;
                    if(dataSnapshot.hasChild("userState")){
                        type = dataSnapshot.child("userState").child("type").getValue().toString();

                        if(type.equals("online")){
                            holder.onlineStatusView.setVisibility(View.VISIBLE);
                        }
                        else{
                            holder.onlineStatusView.setVisibility(View.INVISIBLE);
                        }
                    }

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CharSequence options[] = new CharSequence[]{
                                    capitalize(fullname) + "'s Profile",
                                    "Send Message"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Select Option");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(which == 0){
                                        Intent profileIntent = new Intent(context, PersonProfileActivity.class);
                                        profileIntent.putExtra("visit_user_id", uid);
                                        profileIntent.putExtra("role", role);
                                        context.startActivity(profileIntent);
                                    }
                                    if(which == 1){
                                        Intent communicateIntent = new Intent(context, CommunicateActivity.class);
                                        communicateIntent.putExtra("visit_user_id", uid);
                                        communicateIntent.putExtra("fullName", fullname);
                                        context.startActivity(communicateIntent);
                                    }
                                }
                            });
                            builder.show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try{
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).fit().into(holder.myUserImage);
        }
        catch (Exception e){
            holder.myUserImage.setImageResource(R.drawable.profile);
        }

    }

    @Override
    public int getItemCount() {
        return applicantArrayList.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        TextView myUserName, usersDate;
        CircleImageView myUserImage;
        ImageView onlineStatusView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            myUserName = itemView.findViewById(R.id.all_users_full_name);
            usersDate = itemView.findViewById(R.id.all_users_status);
            myUserImage = itemView.findViewById(R.id.all_users_profile_image);
            onlineStatusView = itemView.findViewById(R.id.all_user_online_icon);
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
