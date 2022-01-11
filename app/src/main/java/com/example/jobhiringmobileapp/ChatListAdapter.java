package com.example.jobhiringmobileapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhiringmobileapp.listeners.UsersListener;
import com.example.jobhiringmobileapp.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private Context context;
    private ArrayList<Users> chatLists;
    private UsersListener usersListener;
    private List<String> selectedUsers;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ChatListAdapter(Context context, ArrayList<Users> chatLists, UsersListener usersListener) {
        this.context = context;
        this.chatLists = chatLists;
        this.usersListener = usersListener;
        selectedUsers = new ArrayList<>();
    }

    public List<String> getSelectedUsers() {
        return selectedUsers;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);

        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Users model = chatLists.get(position);


        final String hisUid = model.getUid();
        final String fullName = model.getFullname();

        holder.myName.setText(capitalize(fullName));

        DatabaseReference userRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        userRef.child(hisUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String userRole = dataSnapshot.child("role").getValue().toString();
                    final String profileImage = ""+dataSnapshot.child("profileimage").getValue();
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

                    try{
                        Picasso.get().load(profileImage).placeholder(R.drawable.profile).fit().into(holder.myImage);
                    }
                    catch (Exception e){
                        holder.myImage.setImageResource(R.drawable.profile);
                    }

                    holder.userContainer.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ColorDrawable viewColor = (ColorDrawable) holder.userContainer.getBackground();
                            int colorId = viewColor.getColor();
                            DatabaseReference tokenRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
                            tokenRef.child(model.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        if (colorId != -9079435) {

                                            Token token = snapshot.getValue(Token.class);
                                            selectedUsers.add(token.getToken());

                                            holder.userContainer.setBackgroundColor(Color.parseColor("#757575"));
                                            usersListener.onMultipleUsersAction(true);

                                        }
                                    }
                                    else {
                                        Toast.makeText(context, "This user is not online.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            return true;
                        }
                    });

                    holder.userContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(selectedUsers.size() > 0) {

                                ColorDrawable viewColor = (ColorDrawable) holder.userContainer.getBackground();
                                int colorId = viewColor.getColor();

                                DatabaseReference tokenRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
                                tokenRef.child(model.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            if (colorId == -9079435) {
                                                Token token = snapshot.getValue(Token.class);
                                                selectedUsers.remove(token.getToken());
                                                holder.userContainer.setBackgroundColor(Color.parseColor("#3B4041"));
                                                if (selectedUsers.size() == 0) {
                                                    usersListener.onMultipleUsersAction(false);
                                                }

                                            } else {

                                                Token token = snapshot.getValue(Token.class);
                                                selectedUsers.add(token.getToken());

                                                holder.userContainer.setBackgroundColor(Color.parseColor("#757575"));

                                            }
                                        }
                                        else{
                                            Toast.makeText(context, "This user is not online.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else{
                                Intent communicateIntent = new Intent(context, CommunicateActivity.class);
                                communicateIntent.putExtra("visit_user_id", hisUid);
                                communicateIntent.putExtra("fullName", fullName);
                                context.startActivity(communicateIntent);
                            }
//                            else {
//
//                                CharSequence options[] = new CharSequence[]{
//                                        fullName + "'s Profile",
//                                        "Send Message", "Audio Call", "Video Call"
//                                };
//
//                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                                builder.setTitle("Select Option");
//
//                                builder.setItems(options, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        if (which == 0) {
//                                            Intent profileIntent = new Intent(context, PersonProfileActivity.class);
//                                            profileIntent.putExtra("visit_user_id", hisUid);
//                                            profileIntent.putExtra("role", userRole);
//                                            context.startActivity(profileIntent);
//                                        }
//                                        if (which == 1) {
//                                            Intent communicateIntent = new Intent(context, CommunicateActivity.class);
//                                            communicateIntent.putExtra("visit_user_id", hisUid);
//                                            communicateIntent.putExtra("fullName", fullName);
//                                            context.startActivity(communicateIntent);
//                                        }
//                                        if (which == 2) {
//                                            usersListener.initiateAudioMeeting(model);
//                                        }
//                                        if (which == 3) {
//                                            usersListener.initiateVideoMeeting(model);
//                                        }
//                                    }
//                                });
//                                builder.show();
//                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loadLastMessage(model, holder);

    }

    private void loadLastMessage(Users model, ChatListViewHolder holder) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Messages").child(currentUserId);
        messagesRef.child(model.getUid()).orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String from = ds.child("from").getValue().toString();
                    String message = ds.child("message").getValue().toString();
                    if(from.equals(currentUserId)){
                        holder.lastMessage.setText("You: " + message);
                    }
                    else{
                        DatabaseReference userRef1 = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
                        userRef1.child(from).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final String fullName = snapshot.child("fullname").getValue().toString();
                                holder.lastMessage.setText(capitalize(fullName) + ": " + message);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatLists.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {
        private TextView myName, lastMessage;
        private CircleImageView myImage;
        private ImageView onlineStatusView;
        private RelativeLayout userContainer;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            myName = itemView.findViewById(R.id.all_users_full_name);
            lastMessage = itemView.findViewById(R.id.all_users_status);
            myImage = itemView.findViewById(R.id.all_users_profile_image);
            onlineStatusView = itemView.findViewById(R.id.all_user_online_icon);
            userContainer = itemView.findViewById(R.id.userContainer);
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
