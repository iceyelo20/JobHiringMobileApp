package com.example.jobhiringmobileapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.jobhiringmobileapp.notifications.Token;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class ApplicantActivity extends AppCompatActivity{

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView applicantPostList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
//    private ImageButton AddNewPostButton;

    private FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, PostRef, LikeRef;

    private String currentUserID;
    Boolean LikeChecker = false;

    private int REQUEST_CODE_BATTERY_OPTIMIZATION = 1;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseRecyclerAdapter!= null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_applicant);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();

            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", currentUserID);
            editor.apply();
        }


        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Posts");
        LikeRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Likes");

        mToolbar = (Toolbar) findViewById(R.id.applicant_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("JHAPP");

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

//        AddNewPostButton = (ImageButton) findViewById(R.id.applicant_add_new_post_button);

        drawerLayout = (DrawerLayout) findViewById(R.id.applicant_drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(ApplicantActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.applicant_navigation_view);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        applicantPostList = (RecyclerView) findViewById(R.id.all_users_post_list);
        applicantPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        applicantPostList.setLayoutManager(linearLayoutManager);


        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        try{
                            Picasso.get().load(image).placeholder(R.drawable.profile).fit().into(NavProfileImage);
                        }
                        catch (Exception e){
                            NavProfileImage.setImageResource(R.drawable.profile);
                        }
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(capitalize(fullname));
                    }
                    else {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(capitalize(fullname));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

//        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SendUserToPostActivity();
//            }
//        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful() && task.getResult()!=null){
                    updateToken(task.getResult());
                }
            }
        });

        DisplayAllUsersPosts();

        checkForBatteryOptimization();

    }

    public void updateToken(String token){
        DatabaseReference tokenRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
        Token mToken = new Token(token);
        tokenRef.child(currentUserID).setValue(mToken);

        UserRef.child(currentUserID).child("token").setValue(token);
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

        UserRef.child(currentUserID).child("userState").updateChildren(currentStateMap);
    }


    private void DisplayAllUsersPosts() {

        Query SortPostInDescendingOrder = PostRef.orderByChild("serverposttimestamp");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(SortPostInDescendingOrder, Posts.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                final String PostKey = getRef(position).getKey();

                holder.username.setText(capitalize(model.getFullname()));
                holder.PostTime.setText("  " + model.getTime());
                holder.PostDate.setText("  " + model.getDate());
                holder.PostDescription.setText(model.getDescription());

                try{
                    Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).fit().into(holder.ProfileImage);
                }
                catch (Exception e){
                    holder.ProfileImage.setImageResource(R.drawable.profile);
                }

                Picasso.get().load(model.getPostimage()).fit().into(holder.PostImage);

                holder.setLikeButtonStatus(PostKey);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(ApplicantActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        firebaseRecyclerAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
                        startActivity(clickPostIntent);
                    }
                });

                holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;

                        LikeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (LikeChecker.equals(true)){
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                        LikeRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }
                                    else{
                                        LikeRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });


                holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickCommentIntent = new Intent(ApplicantActivity.this, CommentsActivity.class);
                        clickCommentIntent.putExtra("PostKey", PostKey);
                        startActivity(clickCommentIntent);
                    }
                });

            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout, viewGroup, false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;
            }
        };

        applicantPostList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firebaseRecyclerAdapter.startListening();
                firebaseRecyclerAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        updateUserStatus("online");
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{

        TextView username, PostTime, PostDate, PostDescription;
        CircleImageView ProfileImage;
        ImageView PostImage;
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfDislike;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.post_username);
            PostTime = (TextView) itemView.findViewById(R.id.post_time);
            PostDate = (TextView) itemView.findViewById(R.id.post_date);
            likePostButton =  (ImageButton) itemView.findViewById(R.id.like_button);
            commentPostButton = (ImageButton) itemView.findViewById(R.id.comment_button);
            displayNoOfDislike = (TextView) itemView.findViewById(R.id.display_no_of_like);
            PostDescription = (TextView) itemView.findViewById(R.id.post_description);
            ProfileImage = (CircleImageView) itemView.findViewById(R.id.post_profile_image);
            PostImage = (ImageView) itemView.findViewById(R.id.post_image);

            LikesRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        if (countLikes == 0){
                            displayNoOfDislike.setText("");
                        }else {
                            displayNoOfDislike.setText(Integer.toString(countLikes));
                        }
                    }else{
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        if (countLikes == 0){
                            displayNoOfDislike.setText("");
                        }else {
                            displayNoOfDislike.setText(Integer.toString(countLikes));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }



    }

    private void SendUserToPostActivity() {
        Intent postIntent = new Intent(ApplicantActivity.this, PostActivity.class);
        startActivity(postIntent);
    }


    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(ApplicantActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {

        /*user menu*/
        switch (item.getItemId()){

            case R.id.applicant_nav_profile:
                SendUserToApplicantProfileActivity();
                break;

            case R.id.applicant_nav_employer:
                SendUserToEmployerUserActivity();
                break;

//            case R.id.applicant_nav_friends:
//                SendUserToFriendActivity();
//                break;
//
//            case R.id.applicant_nav_find_friends:
//                SendUserToFindFriendsActivity();
//                break;

            case R.id.applicant_nav_find_job:
                SendUserToJobActivity();
                break;

            case R.id.applicant_nav_messages:
                SendUserToChatListActivity();
                break;

            case R.id.applicant_nav_group_messages:
                SendUserToGroupChatActivity();
                break;

            case R.id.applicant_nav_settings:
                if (GoogleSignIn.getLastSignedInAccount(ApplicantActivity.this) == null){
                    SendUserToChangePasswordActivity();
                }
                else{
                    Toast.makeText(this, "You are login in using google account. Can't change password.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.applicant_nav_logout:
                updateUserStatus("offline");
                UserRef.child(currentUserID).child("token").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            UserRef.child(currentUserID).child("userState").child("type").setValue("offline");
                        }
                        else {
                            String message = task.getException().getMessage();
                            Toast.makeText(ApplicantActivity.this, ""+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                DatabaseReference logoutToken = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
                logoutToken.child(currentUserID).removeValue();
                GoogleSignInOptions gso = new GoogleSignInOptions.
                        Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                        build();
                GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(this,gso);
                googleSignInClient.signOut();
                mAuth.signOut();
                SendUserToMainActivity();
                Toast.makeText(this, "Logout successfully.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void checkForBatteryOptimization(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager =(PowerManager) getSystemService(POWER_SERVICE);
            if(!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(ApplicantActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled. It can interrupt running background services.");
                builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATION){
            checkForBatteryOptimization();
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

    private void SendUserToChangePasswordActivity() {
        Intent changePasswordIntent = new Intent(ApplicantActivity.this, ChangePasswordActivity.class);
        changePasswordIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(changePasswordIntent);
    }

    private void SendUserToJobActivity() {
        Intent jobIntent = new Intent(ApplicantActivity.this, JobActivity.class);
        jobIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(jobIntent);
    }

    private void SendUserToEmployerUserActivity() {
        Intent employerUserIntent = new Intent(ApplicantActivity.this, EmployerUserActivity.class);
        employerUserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(employerUserIntent);
    }

    private void SendUserToFriendActivity() {
        Intent friendsIntent = new Intent(ApplicantActivity.this, FriendsActivity.class);
        friendsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(friendsIntent);
    }

    private void SendUserToApplicantProfileActivity() {
        Intent applicantProfileIntent = new Intent(ApplicantActivity.this, ApplicantProfileActivity.class);
        applicantProfileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(applicantProfileIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent friendsIntent = new Intent(ApplicantActivity.this, FindFriendsActivity.class);
        friendsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(friendsIntent);
    }

    private void SendUserToChatListActivity() {
        Intent chatListIntent = new Intent(ApplicantActivity.this, ChatListActivity.class);
        chatListIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(chatListIntent);
    }

    private void SendUserToGroupChatActivity() {
        Intent groupListIntent = new Intent(ApplicantActivity.this, ViewGroupActivity.class);
        groupListIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(groupListIntent);
    }

}