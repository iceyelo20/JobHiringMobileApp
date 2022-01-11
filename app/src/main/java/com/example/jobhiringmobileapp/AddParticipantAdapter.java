package com.example.jobhiringmobileapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddParticipantAdapter extends RecyclerView.Adapter<AddParticipantAdapter.AddParticipantViewHolder> {

    private Context context;
    private ArrayList<Users> usersList;
    private FirebaseAuth mAuth;
    private String groupId, myGroupRole;

    public AddParticipantAdapter(Context context, ArrayList<Users> usersList, String groupId, String myGroupRole) {
        this.context = context;
        this.usersList = usersList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public AddParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_participant_display_layout,parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new AddParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddParticipantViewHolder holder, int position) {


        Users users = usersList.get(position);
        String name = users.getFullname();
        String username = users.getUsername();
        String image = users.getProfileimage();
        String uid = users.getUid();

        holder.nameTv.setText(capitalize(name));
        holder.usernameTv.setText(username);
        try{
            Picasso.get().load(image).placeholder(R.drawable.profile).fit().into(holder.participantImage);
        }
        catch (Exception e){
            holder.participantImage.setImageResource(R.drawable.profile);
        }

        checkIfAlreadyExists(users, holder);

         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 DatabaseReference GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");
                 GroupRef.child(groupId).child("Participants").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         if(dataSnapshot.exists()){
                             String hisPreviousRole = ""+dataSnapshot.child("role").getValue();

                             String[] options;

                             AlertDialog.Builder builder = new AlertDialog.Builder(context);
                             builder.setTitle("Choose Option");
                             if(myGroupRole.equals("creator")){
                                 if(hisPreviousRole.equals("admin")){
                                     options = new String[]{"Remove Admin", "Remove User"};
                                     builder.setItems(options, new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {

                                             if(which==0){
                                                 removeAdmin(users);
                                             }
                                             else{
                                                 removeParticipant(users);
                                             }
                                         }
                                     }).show();
                                 }
                                 else if(hisPreviousRole.equals("participant")){
                                    options = new String[]{"Make Admin", "Remove User"};
                                     builder.setItems(options, new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {

                                             if(which==0){
                                                 makeAdmin(users);
                                             }
                                             else{
                                                 removeParticipant(users);
                                             }
                                         }
                                     }).show();
                                 }
                             }
                             else if(myGroupRole.equals("admin")){
                                 if(hisPreviousRole.equals("creator")){
                                     Toast.makeText(context, "Creator of the Group.", Toast.LENGTH_SHORT).show();
                                 }
                                 else if (hisPreviousRole.equals("admin")){
                                     options = new String[]{"Remove Admin", "Remove User"};
                                     builder.setItems(options, new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {

                                             if(which==0){
                                                 removeAdmin(users);
                                             }
                                             else{
                                                 removeParticipant(users);
                                             }
                                         }
                                     }).show();
                                 }
                                 else if (hisPreviousRole.equals("participant")){
                                     options = new String[]{"Make Admin", "Remove User"};
                                     builder.setItems(options, new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {

                                             if(which==0){
                                                 makeAdmin(users);
                                             }
                                             else{
                                                 removeParticipant(users);
                                             }
                                         }
                                     }).show();

                                 }

                             }
                         }
                         else{
                             AlertDialog.Builder builder = new AlertDialog.Builder(context);
                             builder.setTitle("Add Participant").setMessage("Add this user in this group").setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     addParticipant(users);
                                 }
                             }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.dismiss();
                                 }
                             }).show();
                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {

                     }
                 });
             }
         });
    }

    private void addParticipant(Users users) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", users.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);

        DatabaseReference GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");
        GroupRef.child(groupId).child("Participants").child(users.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Added successfully.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(Users users) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");

        DatabaseReference GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");
        GroupRef.child(groupId).child("Participants").child(users.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "The user is now admin.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeParticipant(Users users) {
        DatabaseReference GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");
        GroupRef.child(groupId).child("Participants").child(users.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Successfully remove the user.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(Users users) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");

        DatabaseReference GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");
        GroupRef.child(groupId).child("Participants").child(users.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "The user is no longer admin.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyExists(Users users, AddParticipantViewHolder holder) {
        DatabaseReference GroupRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Groups");
        GroupRef.child(groupId).child("Participants").child(users.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String hisRole = ""+dataSnapshot.child("role").getValue();
                    holder.statusTv.setText(hisRole);
                }
                else{
                    holder.statusTv.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class AddParticipantViewHolder extends RecyclerView.ViewHolder{

        TextView nameTv, usernameTv, statusTv;
        CircleImageView participantImage;
        ImageView onlineStatusView;

        public AddParticipantViewHolder(@NonNull View itemView) {
            super(itemView);

            participantImage = itemView.findViewById(R.id.all_participants_profile_image);
            nameTv = itemView.findViewById(R.id.all_participants_full_name);
            usernameTv = itemView.findViewById(R.id.all_participants_username);
            statusTv = itemView.findViewById(R.id.all_participants_status);
            onlineStatusView = itemView.findViewById(R.id.all_participants_online_icon);


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
