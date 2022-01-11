package com.example.jobhiringmobileapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EmployerUserActivity extends AppCompatActivity {

    private DatabaseReference UserRef;
    private EmployerUserAdapter employerUserAdapter;
    private RecyclerView employerList;
    private ArrayList<Users> employerArrayList;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_user);

        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.employer_user_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Employer");

        employerList = (RecyclerView) findViewById(R.id.all_employer_list);
        employerList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        employerList.setLayoutManager(linearLayoutManager);

        LoadEmployer(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type here to search (Job Name)");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                LoadEmployer(newText.toLowerCase());

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void LoadEmployer(String search) {
        employerArrayList = new ArrayList<>();
        if (search != null){
            Query searchEmployer = UserRef.orderByChild("fullname").startAt(search).endAt(search + "\uf8ff");
            searchEmployer.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    employerArrayList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("role").getValue().toString().equals("Employer")) {
                            Users model = ds.getValue(Users.class);
                            employerArrayList.add(model);
                        }
                    }
                    employerUserAdapter = new EmployerUserAdapter(EmployerUserActivity.this, employerArrayList);
                    employerList.setAdapter(employerUserAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            UserRef.orderByChild("role").equalTo("Employer").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    employerArrayList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Users model = ds.getValue(Users.class);

                            employerArrayList.add(model);
                        }
                        employerUserAdapter = new EmployerUserAdapter(EmployerUserActivity.this, employerArrayList);
                        employerList.setAdapter(employerUserAdapter);

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
}