package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference friendsRef, userRef;
    private FirebaseAuth mAuth;

    private Toolbar mToolbar;
    private String online_user_id;

    private FirebaseRecyclerAdapter<Users, FriendsViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            online_user_id = mFirebaseUser.getUid();
        }
        friendsRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Friends").child(online_user_id);
        userRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Friends");

        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUserStatus("online");
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        updateUserStatus("offline");
        if (firebaseRecyclerAdapter!= null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUserStatus("offline");
    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        userRef.child(online_user_id).child("userState").updateChildren(currentStateMap);
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(friendsRef, Users.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Users model) {

                holder.friendsDate.setText("Friends Since: " + model.getDate());

                final String userIDs = getRef(position).getKey();

                userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String fullName = dataSnapshot.child("fullname").getValue().toString();
                            final String userRole = dataSnapshot.child("role").getValue().toString();
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

                            holder.myName.setText(fullName);

                            if (dataSnapshot.child("profileimage").exists()) {
                                final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile).fit().into(holder.myImage);
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{
                                          fullName + "'s Profile",
                                            "Send Message"
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                profileIntent.putExtra("visit_user_id", userIDs);
                                                profileIntent.putExtra("role", userRole);
                                                startActivity(profileIntent);
                                            }
                                            if(which == 1){
                                                Intent communicateIntent = new Intent(FriendsActivity.this, CommunicateActivity.class);
                                                communicateIntent.putExtra("visit_user_id", userIDs);
                                                communicateIntent.putExtra("fullName", fullName);
                                                startActivity(communicateIntent);
                                                finish();
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
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        TextView myName, friendsDate;
        CircleImageView myImage;
        ImageView onlineStatusView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            myName = itemView.findViewById(R.id.all_users_full_name);
            friendsDate = itemView.findViewById(R.id.all_users_status);
            myImage = itemView.findViewById(R.id.all_users_profile_image);
            onlineStatusView = itemView.findViewById(R.id.all_user_online_icon);
        }
    }
}