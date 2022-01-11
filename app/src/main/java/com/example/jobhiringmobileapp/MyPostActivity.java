package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostRef, UserRef, LikeRef;

    private String currentUserID;
    Boolean LikeChecker = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseRecyclerAdapter<Posts, MyPostViewHolder> firebaseRecyclerAdapter;


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
        setContentView(R.layout.activity_my_post);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }

        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Posts");
        LikeRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Likes");

        mToolbar = (Toolbar) findViewById(R.id.my_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Posts");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_my_post_layout);

        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts() {

        Query myPostQuery = PostRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(myPostQuery, Posts.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyPostViewHolder holder, int position, @NonNull Posts model) {

                final String PostKey = getRef(position).getKey();

                holder.username.setText(capitalize(model.getFullname()));
                holder.PostTime.setText("  " + model.getTime());
                holder.PostDate.setText("  " + model.getDate());
                holder.PostDescription.setText(model.getDescription());

                try{
                    Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.ProfileImage);
                }
                catch (Exception e){
                    holder.ProfileImage.setImageResource(R.drawable.profile);
                }

                Picasso.get().load(model.getPostimage()).fit().into(holder.PostImage);

                holder.setLikeButtonStatus(PostKey);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MyPostActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
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
                        Intent clickCommentIntent = new Intent(MyPostActivity.this, CommentsActivity.class);
                        clickCommentIntent.putExtra("PostKey", PostKey);
                        startActivity(clickCommentIntent);
                    }
                });
            }

            @NonNull
            @Override
            public MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                MyPostViewHolder viewHolder = new MyPostViewHolder(view);
                return viewHolder;
            }
        };

        myPostsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firebaseRecyclerAdapter.startListening();
                firebaseRecyclerAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    public static class MyPostViewHolder extends RecyclerView.ViewHolder{

        TextView username, PostTime, PostDate, PostDescription;
        CircleImageView ProfileImage;
        ImageView PostImage;
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfDislike;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public MyPostViewHolder(@NonNull View itemView) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
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