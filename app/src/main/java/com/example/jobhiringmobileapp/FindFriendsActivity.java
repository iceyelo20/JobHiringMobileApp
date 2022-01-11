package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;

    private RecyclerView searchResultList;

    private FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter;

    private DatabaseReference allUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        searchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        searchResultList.setLayoutManager(manager);
        searchResultList.setHasFixedSize(true);


        searchButton = (ImageButton) findViewById(R.id.search_friends);
        searchInputText = (EditText) findViewById(R.id.search_box_input);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = searchInputText.getText().toString();
                if(TextUtils.isEmpty(searchBoxInput)) {
                    Toast.makeText(FindFriendsActivity.this, "Please search a valid input.", Toast.LENGTH_SHORT).show();
                }
                else{
                    SearchFriends(searchBoxInput);
                }
            }
        });
    }

    private void SearchFriends(String searchBoxInput) {

        Query searchPeopleAndFriendsQuery = allUserRef.orderByChild("fullname")
                .startAt(searchBoxInput.toUpperCase()).endAt(searchBoxInput + "\uf8ff");


        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchPeopleAndFriendsQuery, FindFriends.class)
                .build();


        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull FindFriends model) {
                holder.myName.setText(model.getFullname());
                holder.myStatus.setText(model.getStatus());

                try{
                    Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).fit().into(holder.myImage);
                }
                catch (Exception e){
                    holder.myImage.setImageResource(R.drawable.profile);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String visit_user_id = getRef(position).getKey();
                        allUserRef.child(visit_user_id).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    final String userRole = dataSnapshot.getValue().toString();

                                    Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                                    profileIntent.putExtra("visit_user_id", visit_user_id);
                                    profileIntent.putExtra("role", userRole);
                                    startActivity(profileIntent);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        searchResultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        
    }

    public static class  FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView myName, myStatus;
        CircleImageView myImage;

        public FindFriendsViewHolder (@NonNull View itemView)
        {
            super(itemView);

            myName = itemView.findViewById(R.id.all_users_full_name);
            myStatus = itemView.findViewById(R.id.all_users_status);
            myImage = itemView.findViewById(R.id.all_users_profile_image);
        }
    }
}