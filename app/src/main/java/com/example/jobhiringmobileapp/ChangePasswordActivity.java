package com.example.jobhiringmobileapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText UserOldPassword, UserPassword, UserConfirmPassword;
    private Button ChangePasswordButton;
    private ProgressDialog loadingBar;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mToolbar = (Toolbar) findViewById(R.id.change_password_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Change Password");

        UserOldPassword = (EditText) findViewById(R.id.old_password);
        UserPassword = (EditText) findViewById(R.id.password);
        UserConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        ChangePasswordButton = (Button) findViewById(R.id.change_password_button);
        loadingBar = new ProgressDialog(this);

        ChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Validation();
            }
        });
    }

    private void Validation() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        final String oldPass = UserOldPassword.getText().toString();
        final String confirmPass = UserConfirmPassword.getText().toString();
        final String newPass = UserPassword.getText().toString();

        if (TextUtils.isEmpty(oldPass)){
            Toast.makeText(this, "Please input the old password.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confirmPass)){
            Toast.makeText(this, "Please input the confirm password.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(newPass)){
            Toast.makeText(this, "Please input the new password.", Toast.LENGTH_SHORT).show();
        }
        else if (!newPass.equals(confirmPass)){
            Toast.makeText(this, "The new password and confirm password does not match.", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Changing Password");
            loadingBar.setMessage("Please wait, while we are changing your password.");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPass);

            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChangePasswordActivity.this, "Password Successfully Changed.", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Old password does not match to the new password.", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }
}