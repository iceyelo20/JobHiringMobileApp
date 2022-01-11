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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployerUserAdapter extends RecyclerView.Adapter<EmployerUserAdapter.EmployerUserViewHolder> {

    private Context context;
    private ArrayList<Users> employerArrayList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference UserRef;

    public EmployerUserAdapter(Context context, ArrayList<Users> employerArrayList) {
        this.context = context;
        this.employerArrayList = employerArrayList;
    }

    @NonNull
    @Override
    public EmployerUserAdapter.EmployerUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        return new EmployerUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployerUserAdapter.EmployerUserViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }

        Users users = employerArrayList.get(position);


        String date = users.getDate();
        String fullname = users.getFullname();
        String profileimage = users.getProfileimage();
        String username = users.getUsername();
        String uid = users.getUid();
        String role = users.getRole();

        holder.fullName.setText(capitalize(fullname));
        holder.employerStatus.setText("@"+username);

        try{
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).fit().into(holder.profileImage);
        }
        catch (Exception e){
            holder.profileImage.setImageResource(R.drawable.profile);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{
                        capitalize(fullname) + "'s Profile", "Send Message"
                                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Select Option");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent profileIntent = new Intent(context, PersonProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", uid);
                            profileIntent.putExtra("role", role);
                            context.startActivity(profileIntent);
                        }
                        if (which == 1) {
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

    @Override
    public int getItemCount() {
        return employerArrayList.size();
    }

    public class EmployerUserViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView profileImage;
        private TextView fullName, employerStatus;
        private ImageView onlineStatus;

        public EmployerUserViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = (CircleImageView) itemView.findViewById(R.id.all_users_profile_image);
            fullName = (TextView) itemView.findViewById(R.id.all_users_full_name);
            onlineStatus = (ImageView) itemView.findViewById(R.id.all_user_online_icon);
            employerStatus = (TextView) itemView.findViewById(R.id.all_users_status);
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
