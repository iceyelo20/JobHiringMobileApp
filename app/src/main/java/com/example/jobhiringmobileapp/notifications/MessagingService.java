package com.example.jobhiringmobileapp.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.jobhiringmobileapp.AppliedJobActivity;
import com.example.jobhiringmobileapp.CommunicateActivity;
import com.example.jobhiringmobileapp.IncomingInvitationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    private static final String REMOTE_MSG_TYPE = "type";
    private static final String REMOTE_MSG_INVITATION = "invitation";
    private static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    private static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    private static final String REMOTE_MSG_DATA = "data";
    private static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    private static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    private static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    private static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";

    private static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            updateToken(token);
        }

    }

    private void updateToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference tokenRef = FirebaseDatabase.getInstance("https://job-hiring-mobile-app-e1345-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Tokens");
        Token token1 = new Token(token);
        tokenRef.child(user.getUid()).setValue(token1);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String type = remoteMessage.getData().get(REMOTE_MSG_TYPE);

        if(type != null){
            if(type.equals(REMOTE_MSG_INVITATION)){
                Intent incomingIntent = new Intent(this, IncomingInvitationActivity.class);
                incomingIntent.putExtra(REMOTE_MSG_MEETING_TYPE, remoteMessage.getData().get(REMOTE_MSG_MEETING_TYPE));
                incomingIntent.putExtra("fullname", remoteMessage.getData().get("fullname"));
                incomingIntent.putExtra("username", remoteMessage.getData().get("username"));
                incomingIntent.putExtra("profileimage", remoteMessage.getData().get("profileimage"));
                incomingIntent.putExtra(REMOTE_MSG_INVITER_TOKEN, remoteMessage.getData().get(REMOTE_MSG_INVITER_TOKEN));
                incomingIntent.putExtra(REMOTE_MSG_MEETING_ROOM, remoteMessage.getData().get(REMOTE_MSG_MEETING_ROOM));
                incomingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(incomingIntent);
            }
            else if(type.equals(REMOTE_MSG_INVITATION_RESPONSE)){
                Intent intent = new Intent(REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(REMOTE_MSG_INVITATION_RESPONSE, remoteMessage.getData().get(REMOTE_MSG_INVITATION_RESPONSE));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }


        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String saveCurrentUser = sp.getString("Current_USERID", "None");

        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = preferences.getString("currentuser", "none");

        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        String title = remoteMessage.getData().get("title");

        if(user != null && sent != null && title != null) {
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null && sent.equals(fUser.getUid())) {
                if (!saveCurrentUser.equals(user)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (title.equals("New Message")){
                            if (!currentUser.equals(user)) {
                                sendOAndAboveNotification(remoteMessage);
                            }
                        }
                        else{
                            senOAndAboveJobNotification(remoteMessage);
                        }
                    } else {
                        if (title.equals("New Message")) {
                            if (!currentUser.equals(user)) {
                                sendNormalNotification(remoteMessage);
                            }
                        }
                        else{
                            sendNormalJobNotification(remoteMessage);
                        }
                    }
                }
            }
        }
    }

    private void sendNormalJobNotification(RemoteMessage remoteMessage){
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, AppliedJobActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =  new NotificationCompat.Builder(this).setSmallIcon(Integer.parseInt(icon)).setContentText(body).setContentTitle(title)
                .setAutoCancel(true).setSound(defSoundUri).setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0 ;

        if(i>0){
            j=i;
        }
        notificationManager.notify(j,builder.build());
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, CommunicateActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =  new NotificationCompat.Builder(this).setSmallIcon(Integer.parseInt(icon)).setContentText(body).setContentTitle(title)
                .setAutoCancel(true).setSound(defSoundUri).setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0 ;

        if(i>0){
            j=i;
        }
        notificationManager.notify(j,builder.build());
    }

    private void senOAndAboveJobNotification(RemoteMessage remoteMessage){
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, AppliedJobActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotifications(title, body, pIntent, defSoundUri, icon);

        int j = 0 ;

        if(i>0){
            j=1;
        }
        notification1.getManager().notify(j,builder.build());
    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, CommunicateActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotifications(title, body, pIntent, defSoundUri, icon);

        int j = 0 ;

        if(i>0){
            j=1;
        }
        notification1.getManager().notify(j,builder.build());

    }

}
