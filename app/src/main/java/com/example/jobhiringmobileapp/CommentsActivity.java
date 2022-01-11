package com.example.jobhiringmobileapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private EditText commentInputText;
    private RecyclerView commentsList;
    private ProgressDialog loadingBar;

    private DatabaseReference UserRef, DPostRef;
    private FirebaseAuth mAuth;

    private FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter;

    private String Post_Key, currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        DPostRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Posts").child(Post_Key).child("Comments");


        commentsList = (RecyclerView) findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);
        loadingBar = new ProgressDialog(this);

        commentInputText = (EditText) findViewById(R.id.comment_input);
        postCommentButton = (ImageButton) findViewById(R.id.post_comment_button);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidateComment();


            }
        });

        DisplayAllComments();
    }

    private void DisplayAllComments() {
        Query SortCommentsInDescendingOrder = DPostRef.orderByChild("serverposttimestamp");

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(SortCommentsInDescendingOrder, Comments.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                holder.myUserName.setText("@" + model.getUsername() + " ");
                holder.myComment.setText(model.getComment());
                holder.myDate.setText(" Date: " +model.getDate());
                holder.myTime.setText(" Time: " + model.getTime());

            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout,parent, false);
                CommentsActivity.CommentsViewHolder viewHolder = new CommentsActivity.CommentsViewHolder(view);
                return viewHolder;
            }
        };

        commentsList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();

    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{

        TextView myUserName, myComment, myTime, myDate;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            myUserName = itemView.findViewById(R.id.comment_username);
            myComment = itemView.findViewById(R.id.comment_text);
            myTime = itemView.findViewById(R.id.comment_time);
            myDate = itemView.findViewById(R.id.comment_date);
        }
    }

    private void ValidateComment() {

        String commentText = commentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please write a comment.", Toast.LENGTH_SHORT).show();

        }
        else{
            loadingBar.setTitle("Add Comment");
            loadingBar.setMessage("Please wait, while we are adding your comment.");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:s");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            final String RandomKey = currentUserId + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", currentUserId);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);

            UserRef.child(currentUserId).child("username").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String UserName = dataSnapshot.getValue().toString();
                        commentsMap.put("username", UserName);
                        commentsMap.put("serverposttimestamp", ServerValue.TIMESTAMP);
                        DPostRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){
                                    commentInputText.setText("");
                                    Toast.makeText(CommentsActivity.this, "You have commented successfully.", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(CommentsActivity.this, "Error occurred. Try again.", Toast.LENGTH_SHORT).show();
                                }
                                loadingBar.dismiss();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter!= null) {
            firebaseRecyclerAdapter.stopListening();
        }

    }
}