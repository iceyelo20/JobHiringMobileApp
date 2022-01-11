package com.example.jobhiringmobileapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class ApplicantProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText userName, applicantStatus, applicantProfName, dateBirth;
    private Button UpdateApplicantDetails, MyAppliedJob, MyFriends;
    private CircleImageView applicantProfileImage;
    private ProgressDialog loadingBar;
    private DatabaseReference profileApplicantRef, AppliedRef, postApplicantRef;
    private FirebaseAuth mAuth;

    private StorageReference UserProfileImageRef;

    private String currentUserID, buttonText, userBarangay, userGender;
    private int countFriends = 0, countJobs = 0;
    final static int Gallery_Pick = 1;

    private String[] itemsBarangay = {"Abelo", "Alas-as", "Balete","Baluk-baluk", "Bancoro", "Bangin", "Calangay", "Hipit", "Maabud North", "Maabud South", "Munlawin", "Pansipit", "Poblacion", "Pulang-Bato", "Santo Niño", "Sinturisan", "Tagudtod", "Talang"};
    private String[] itemsGender = {"Male", "Female"};
    private AutoCompleteTextView autoCompleteTextView, autoCompleteTvGender;
    private ArrayAdapter<String> adapterBarangay, adapterGender;
    private Calendar myCalendar = Calendar.getInstance();

    private TextInputLayout genderTIL, profileTIL, usernameTIL, fullNameTIL, dateBirthTIL, barangayTIL;

    Boolean ExistingFullName = false;
    Boolean ExistingUsername = false;

    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    private String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applicant_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }
        profileApplicantRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users").child(currentUserID);
        AppliedRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Applied");
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        postApplicantRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Posts");

        mToolbar = (Toolbar) findViewById(R.id.applicant_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile Details");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.applicant_profile_username);
        applicantStatus = (EditText) findViewById(R.id.applicant_profile_status);
        applicantProfName = (EditText) findViewById(R.id.applicant_profile_full_name);
        dateBirth = (EditText) findViewById(R.id.setupDOB);
        UpdateApplicantDetails = (Button) findViewById(R.id.update_account_applicant_profile_buttons);
//        MyFriends = (Button) findViewById(R.id.my_friends_button);
        MyAppliedJob = (Button) findViewById(R.id.my_applied_job_button);
        applicantProfileImage = (CircleImageView) findViewById(R.id.applicant_profile_profile_image);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete_txt);
        autoCompleteTvGender = (AutoCompleteTextView) findViewById(R.id.auto_complete_gender_txt);
        genderTIL = (TextInputLayout) findViewById(R.id.textInputLayoutGender);
        profileTIL = (TextInputLayout) findViewById(R.id.textInputLayoutStatus);
        usernameTIL = (TextInputLayout) findViewById(R.id.textInputLayoutUserName);
        fullNameTIL = (TextInputLayout) findViewById(R.id.textInputLayoutProfileName);
        dateBirthTIL = (TextInputLayout) findViewById(R.id.textInputLayoutDOB);
        barangayTIL = (TextInputLayout) findViewById(R.id.textInputLayoutBarangay);

        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        loadingBar = new ProgressDialog(this);
//        MyFriends.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SendUserToFriendsActivity();
//            }
//        });
        MyAppliedJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyAppliedJobActivity();
            }
        });
//        friendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    countFriends = (int) dataSnapshot.getChildrenCount();
//                    MyFriends.setText("Friends (" + Integer.toString(countFriends) + ")");
//                }
//                else{
//                    MyFriends.setText("No Friends");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

//        show profile info

        UpdateApplicantDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonText  = UpdateApplicantDetails.getText().toString();
                if(buttonText.equals("Edit Account Details")) {
                    applicantStatus.setEnabled(true);
                    applicantProfName.setEnabled(true);
                    autoCompleteTextView.setEnabled(true);
                    autoCompleteTvGender.setEnabled(true);
                    dateBirth.setEnabled(true);
                    userName.setEnabled(true);
                    genderTIL.setEnabled(true);
                    profileTIL.setEnabled(true);
                    usernameTIL.setEnabled(true);
                    fullNameTIL.setEnabled(true);
                    dateBirthTIL.setEnabled(true);
                    barangayTIL.setEnabled(true);
                    UpdateApplicantDetails.setText("Save Account Details");
                }
                else{
                    EditAccountInfo();
                }

            }
        });

        LoadDetails();

        applicantProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{
                        "Change Profile Picture"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ApplicantProfileActivity.this);
                builder.setTitle("Profile Picture");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            if(!checkStoragePermission()){
                                requestStoragePermissions();
                            }
                            else{
                                pickFromGallery();
                            }
                        }
                    }
                });
                builder.show();

            }
        });

    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(this, "Storage permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void LoadDetails() {

        AppliedRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countJobs = (int) dataSnapshot.getChildrenCount();
                    MyAppliedJob.setText("Applied Jobs (" + Integer.toString(countJobs) + ")");
                }else{
                    MyAppliedJob.setText("Applied Jobs");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profileApplicantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("profileimage").exists()) {
                        String applicantProfImage = dataSnapshot.child("profileimage").getValue().toString();
                        try{
                            Picasso.get().load(applicantProfImage).placeholder(R.drawable.profile).fit().into(applicantProfileImage);
                        }
                        catch (Exception e){
                            applicantProfileImage.setImageResource(R.drawable.profile);
                        }
                    }

                    String applicant_profile_status = dataSnapshot.child("status").getValue().toString();

                    String applicant_username= dataSnapshot.child("username").getValue().toString();
                    String applicant_fullname = dataSnapshot.child("fullname").getValue().toString();
                    String applicant_dob = dataSnapshot.child("dob").getValue().toString();
                    String applicant_gender = dataSnapshot.child("gender").getValue().toString();
                    String applicant_barangay = dataSnapshot.child("barangay").getValue().toString();

                    applicantStatus.setText(applicant_profile_status);
                    userName.setText(applicant_username);
                    applicantProfName.setText(capitalize(applicant_fullname));
                    autoCompleteTextView.setText(applicant_barangay);
                    dateBirth.setText(applicant_dob);
                    autoCompleteTvGender.setText(applicant_gender);

                    adapterBarangay = new ArrayAdapter<String>(ApplicantProfileActivity.this, R.layout.list_item, itemsBarangay);
                    autoCompleteTextView.setAdapter(adapterBarangay);
                    autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            userBarangay = parent.getItemAtPosition(position).toString();
                        }
                    });

                    adapterGender = new ArrayAdapter<String>(ApplicantProfileActivity.this, R.layout.list_item, itemsGender);
                    autoCompleteTvGender.setAdapter(adapterGender);
                    autoCompleteTvGender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            userGender = parent.getItemAtPosition(position).toString();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
            }
        };

        dateBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ApplicantProfileActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void updateDate() {
        String myFormat = "MMM dd, yyyy"; //put your date format in which you need to display
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        dateBirth.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

//            cropping image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }


//        after cropping
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your profile image.");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri= result.getUri();

//                filepath with type
                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downUri = task.getResult();
                            final String sdownloadUri = downUri.toString();
                            profileApplicantRef.child("profileimage").setValue(sdownloadUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                postApplicantRef.orderByChild("uid").equalTo(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                            Map<String, Object> map = new HashMap<>();
                                                            map.put("profileimage", sdownloadUri);
                                                            ds.getRef().updateChildren(map);

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });


                                                Toast.makeText(ApplicantProfileActivity.this, "Profile image upload successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(ApplicantProfileActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                            loadingBar.dismiss();
                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(ApplicantProfileActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

//                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                        while (!uriTask.isSuccessful());
//                        Uri downloadUri = uriTask.getResult();
//
//                        final String sdownloadUri = String.valueOf(downloadUri);
//
//                        profileApplicantRef.child("profileimage").setValue(sdownloadUri)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            postApplicantRef.orderByChild("uid").equalTo(currentUserID).addValueEventListener(new ValueEventListener() {
//                                                @Override
//                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
//                                                        Map<String, Object> map = new HashMap<>();
//                                                        map.put("profileimage", sdownloadUri);
//                                                        ds.getRef().updateChildren(map);
//
//                                                    }
//
//                                                }
//
//                                                @Override
//                                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                                }
//                                            });
//                                            Toast.makeText(ApplicantProfileActivity.this, "Profile image upload successfully.", Toast.LENGTH_SHORT).show();
//                                        }
//                                        else {
//                                            String message = task.getException().getMessage();
//                                            Toast.makeText(ApplicantProfileActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
//                                        }
//                                        loadingBar.dismiss();
//                                    }
//                                });
//                    }
//                });
            }
            else{
                Toast.makeText(this, "Error occurred: Image can't be crop. Try again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }

    }

    private void EditAccountInfo() {

        String username = userName.getText().toString();
        String fullname = applicantProfName.getText().toString();
        String dob = dateBirth.getText().toString();
        String barangay = autoCompleteTextView.getText().toString();
        String gender = autoCompleteTvGender.getText().toString();
        String status = applicantStatus.getText().toString();

        DatabaseReference UserRef2 = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        UserRef2.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ExistingUsername = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference UserRef1 = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        UserRef1.orderByChild("fullname").equalTo(fullname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ExistingFullName = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Username already existed or not valid.", Toast.LENGTH_SHORT).show();
        }
        else if(username.contains(" ")){
            Toast.makeText(this, "Username already existed or not valid.", Toast.LENGTH_SHORT).show();
        }
        else if (ExistingUsername.equals(true)){
            Toast.makeText(this, "Username already existed or not valid.", Toast.LENGTH_SHORT).show();
            ExistingUsername = false;
        }
        else if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Name already existed or not valid.", Toast.LENGTH_SHORT).show();
        }
        else if (ExistingFullName.equals(true)){
            Toast.makeText(this, "Name already existed or not valid.", Toast.LENGTH_SHORT).show();
            ExistingFullName = false;
        }
        else if (TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(barangay)){
            Toast.makeText(this, "Please select your barangay.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Please enter your date of birth.", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we are updating your profile image.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateApplicantInfo(username, fullname, dob, barangay, gender, status);

        }
    }

    private void UpdateApplicantInfo(String username, String fullname, String dob, String barangay, String gender, String status) {
        HashMap userMap = new HashMap();
        userMap.put("username", username);
        userMap.put("fullname", fullname.toLowerCase());
        userMap.put("barangay", barangay);
        userMap.put("gender", gender);
        userMap.put("dob", dob);
        userMap.put("status", ""+status);

        profileApplicantRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Toast.makeText(ApplicantProfileActivity.this, "Account profile update successfully.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    userName.setEnabled(false);
                    autoCompleteTextView.setEnabled(false);
                    autoCompleteTvGender.setEnabled(false);
                    applicantProfName.setEnabled(false);
                    dateBirth.setEnabled(false);
                    applicantStatus.setEnabled(false);
                    genderTIL.setEnabled(false);
                    profileTIL.setEnabled(false);
                    usernameTIL.setEnabled(false);
                    fullNameTIL.setEnabled(false);
                    dateBirthTIL.setEnabled(false);
                    barangayTIL.setEnabled(false);

                    UpdateApplicantDetails.setText("Edit Account Details");

                }
                else{
                    Toast.makeText(ApplicantProfileActivity.this, "Error occurred, while updating account profile.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });


    }

    private String capitalize(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(ApplicantProfileActivity.this, FriendsActivity.class);
        friendsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(friendsIntent);
    }

    private void SendUserToMyAppliedJobActivity() {
        Intent myPostIntent = new Intent(ApplicantProfileActivity.this, AppliedJobActivity.class);
        myPostIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myPostIntent);
    }


    private void SendUserToApplicantActivity() {
        Intent applicantIntent = new Intent(ApplicantProfileActivity.this, ApplicantActivity.class);
        applicantIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity((applicantIntent));
        finish();
    }
}