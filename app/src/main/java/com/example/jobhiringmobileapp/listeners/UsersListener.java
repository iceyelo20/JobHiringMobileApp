package com.example.jobhiringmobileapp.listeners;

import com.example.jobhiringmobileapp.Users;

public interface UsersListener {

    void initiateVideoMeeting(Users users);

    void initiateAudioMeeting(Users users);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);
}
