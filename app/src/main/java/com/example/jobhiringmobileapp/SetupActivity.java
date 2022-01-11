package com.example.jobhiringmobileapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, dateBirth;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;

    private String currentUserID, userBarangay, userGender;
    final static int Gallery_Pick = 1;

    private String[] itemsBarangay = {"Abelo", "Alas-as", "Balete","Baluk-baluk", "Bancoro", "Bangin", "Calangay", "Hipit", "Maabud North", "Maabud South", "Munlawin", "Pansipit", "Poblacion", "Pulang-Bato", "Santo Niño", "Sinturisan", "Tagudtod", "Talang"};
    private String[] itemsGender = {"Male", "Female"};
    private AutoCompleteTextView autoCompleteTextView, autoCompleteTvGender;
    private ArrayAdapter<String> adapterBarangay, adapterGender;
    private Calendar myCalendar = Calendar.getInstance();

    Boolean ExistingFullName = false;
    Boolean ExistingUsername = false;

    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    private String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }
        UserRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_fullname);
        dateBirth = (EditText) findViewById(R.id.setupDOB);
        SaveInformationButton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete_txt);
        autoCompleteTvGender = (AutoCompleteTextView) findViewById(R.id.auto_complete_gender_txt);

        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        loadingBar = new ProgressDialog(this);

        adapterGender = new ArrayAdapter<String>(this, R.layout.list_item, itemsGender);
        autoCompleteTvGender.setAdapter(adapterGender);

        adapterBarangay = new ArrayAdapter<String>(this, R.layout.list_item, itemsBarangay);
        autoCompleteTextView.setAdapter(adapterBarangay);

        autoCompleteTvGender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userGender = parent.getItemAtPosition(position).toString();
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userBarangay = parent.getItemAtPosition(position).toString();
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
                new DatePickerDialog(SetupActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{
                        "Change Profile Picture"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
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
//        showing the selected profile image
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())  {
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        try{
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                        }
                        catch (Exception e){
                            ProfileImage.setImageResource(R.drawable.profile);
                        }
                    }
                    else{
                        ProfileImage.setImageResource(R.drawable.profile);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        final String sdownloadUri = String.valueOf(downloadUri);

                        UserRef.child("profileimage").setValue(sdownloadUri)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SetupActivity.this, "Profile image upload successfully.", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                                        }
                                        loadingBar.dismiss();
                                    }
                                });
                        }
                });
            }
            else{
                Toast.makeText(this, "Error occurred: Image can't be crop. Try again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }

    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String dob = dateBirth.getText().toString();

        String saveCurrentDate;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        String role = "Applicant";

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
            Toast.makeText(SetupActivity.this, "Username already existed or not valid.", Toast.LENGTH_SHORT).show();
            ExistingUsername = false;
        }
        else if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Name already existed or not valid.", Toast.LENGTH_SHORT).show();
        }
        else if (ExistingFullName.equals(true)){
            Toast.makeText(SetupActivity.this, "Name already existed or not valid.", Toast.LENGTH_SHORT).show();
            ExistingFullName = false;
        }
        else if (TextUtils.isEmpty(userGender)){
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userBarangay)){
            Toast.makeText(this, "Please select your barangay.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Please enter your date of birth.", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are registering your new account.");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("status", "");
            userMap.put("username", username);
            userMap.put("fullname", fullname.toLowerCase());
            userMap.put("barangay", userBarangay);
            userMap.put("role", role);
            userMap.put("gender", userGender);
            userMap.put("dob", dob);
            userMap.put("uid", currentUserID);

            UserRef.child("profileimage").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        UserRef.child("profileimage").setValue("");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            userMap.put("date", saveCurrentDate);
            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        UserRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String user_role = dataSnapshot.getValue(String.class);
                                if (user_role.equals("Applicant")){
                                    SendUserToApplicantActivity();
                                    Toast.makeText(SetupActivity.this, "Account successfully created.", Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }
                                else if (user_role.equals("Employer")){
                                    SendUserToEmployerActivity();
                                    Toast.makeText(SetupActivity.this, "Account successfully created.", Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }

    }

    private void SendUserToEmployerActivity() {
        Intent employerIntent = new Intent(SetupActivity.this, EmployerActivity.class);
        employerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity((employerIntent));
        finish();
    }

    private void SendUserToApplicantActivity() {
        Intent applicantIntent = new Intent(SetupActivity.this, ApplicantActivity.class);
        applicantIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity((applicantIntent));
        finish();
    }

}