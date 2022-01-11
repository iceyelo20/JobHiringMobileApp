//package com.example.jobhiringmobileapp.notifications;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.media.Ringtone;
//import android.media.RingtoneManager;
//import android.net.Uri;
//
//public class NotificationReceiver extends BroadcastReceiver {
//    public static Ringtone ringtone;
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        playNotificationSound(context);
//    }
//    public void playNotificationSound(Context context) {
//        try {
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//            Ringtone r = RingtoneManager.getRingtone(context, notification);
//            r.play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
