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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.HolderGroupChatList>{

    private Context context;
    private ArrayList<GroupChatList> groupChatLists;
    private FirebaseAuth mAuth;
    private String messageSenderID;

    public GroupChatListAdapter(Context context, ArrayList<GroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_groups_layout, parent, false);

        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            messageSenderID = mFirebaseUser.getUid();
        }


        GroupChatList model = groupChatLists.get(position);

        String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.groupLastMessageTv.setText("");

        holder.groupNameTv.setText(groupTitle);

        loadLastMessage(model, holder);

        try{
            Picasso.get().load(groupIcon).placeholder(R.drawable.group_icon).fit().into(holder.groupIconCv);
        }
        catch (Exception e){
            holder.groupIconCv.setImageResource(R.drawable.group_icon);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupChatIntent = new Intent(context, GroupChatActivity.class);
                groupChatIntent.putExtra("groupId", groupId);
                context.startActivity(groupChatIntent);
            }
        });

    }

    private void loadLastMessage(GroupChatList model, HolderGroupChatList holder) {
        DatabaseReference GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");

        GroupRef.child(model.getGroupId()).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String message = ""+ds.child("message").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String sender = ""+ds.child("sender").getValue();
                    String time = ""+ds.child("time").getValue();

                    if(sender.equals(messageSenderID)){
                        holder.groupLastMessageTv.setText("You: " + message);
                    }
                    else{
                        DatabaseReference UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

                        UserRef.child(sender).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final String fullName = "" + snapshot.child("fullname").getValue().toString();
                                holder.groupLastMessageTv.setText(capitalize(fullName) + ": " + message);
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
        return groupChatLists.size();
    }

    public class HolderGroupChatList extends RecyclerView.ViewHolder{

        public CircleImageView groupIconCv;
        public TextView groupNameTv, groupLastMessageTv;
        public ImageView UpdateOnlineStatusIv;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconCv = itemView.findViewById(R.id.all_groups_image);
            groupNameTv = itemView.findViewById(R.id.all_groups_name);
            groupLastMessageTv = itemView.findViewById(R.id.all_groups_status);


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
