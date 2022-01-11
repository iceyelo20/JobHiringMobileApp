package com.example.jobhiringmobileapp;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.HolderGroupMessages> {

    private Context context;
    private List<GroupMessages> groupMessages;

    private String messageSenderID;
    private FirebaseAuth mAuth;

    private DatabaseReference userDatabaseRef;

    public GroupMessagesAdapter(Context context, List<GroupMessages> groupMessages) {
        this.context = context;
        this.groupMessages = groupMessages;
    }

    public class HolderGroupMessages extends RecyclerView.ViewHolder{

        public TextView SenderMessageText, ReceiverMessageText, ReceiverFullName, SenderName;
        public CircleImageView receiverProfileImage;

        public HolderGroupMessages(@NonNull View itemView) {
            super(itemView);

            SenderMessageText = (TextView) itemView.findViewById(R.id.group_sender_message_text);
            SenderName = (TextView) itemView.findViewById(R.id.group_user_you);
            ReceiverMessageText = (TextView) itemView.findViewById(R.id.group_receiver_message_text);
            ReceiverFullName = (TextView) itemView.findViewById(R.id.group_user_full_name);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.group_message_profile_image);
        }
    }

    @NonNull
    @Override
    public HolderGroupMessages onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_messages_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new HolderGroupMessages(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupMessages holder, int position) {
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            messageSenderID = mFirebaseUser.getUid();
        }

        GroupMessages model = groupMessages.get(position);
        String timestamp = model.getTimestamp();
        String senderUid = model.getSender();
        String fromMessageType = model.getType();

        userDatabaseRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        userDatabaseRef.orderByKey().equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String fullName = ""+ds.child("fullname").getValue();
                    String profileImage = ""+ds.child("profileimage").getValue();

                    holder.ReceiverFullName.setText(capitalize(fullName));

                    try{
                        Picasso.get().load(profileImage).placeholder(R.drawable.profile).fit().into(holder.receiverProfileImage);
                    }
                    catch (Exception e){
                        holder.receiverProfileImage.setImageResource(R.drawable.profile);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (fromMessageType.equals("text")){
            holder.ReceiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.ReceiverFullName.setVisibility(View.INVISIBLE);

            if (senderUid.equals(messageSenderID)){
                holder.SenderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.SenderMessageText.setTextColor(Color.WHITE);
                holder.SenderMessageText.setGravity(Gravity.LEFT);
                holder.SenderMessageText.setText(model.getMessage());
            }
            else{
                holder.SenderMessageText.setVisibility(View.INVISIBLE);
                holder.SenderName.setVisibility(View.INVISIBLE);
                holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.ReceiverFullName.setVisibility(View.VISIBLE);

                holder.ReceiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.ReceiverMessageText.setTextColor(Color.WHITE);
                holder.ReceiverMessageText.setGravity(Gravity.LEFT);
                holder.ReceiverMessageText.setText(model.getMessage());
            }
        }


    }


    @Override
    public int getItemCount() {
        return groupMessages.size();
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
