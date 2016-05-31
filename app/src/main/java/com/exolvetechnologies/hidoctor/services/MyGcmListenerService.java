package com.exolvetechnologies.hidoctor.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.activities.DoctorChatInitActivity;
import com.exolvetechnologies.hidoctor.activities.MainActivity;
import com.google.android.gms.gcm.GcmListenerService;


public class MyGcmListenerService extends GcmListenerService {


    private static final String TAG = "HiDoctor";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String conId = data.getString("conference_id");
        String chatType = data.getString("chat_type");
        String categoryId = data.getString("category_id");
        String time = data.getString("time");
        String chatId = data.getString("chat_id");

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Conference Id: " + conId);
        Log.i(TAG, "Chat Type: "+chatType);
        Log.d(TAG, "Chat Id: " + chatId);
        Log.d(TAG, "Time: " + time);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message, conId, chatType, categoryId, time, chatId);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String conId, String type, String cat, String time, String chatId) {
        Intent intent = new Intent(this, DoctorChatInitActivity.class);
        intent.putExtra("CON_ID", conId);
        intent.putExtra("TYPE", type);
        intent.putExtra("CAT", cat);
        intent.putExtra("TIME", time);
        intent.putExtra("CHAT_ID", chatId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri = Uri.parse("android.resource://com.exolvetechnologies.hidoctor/" + R.raw.hidoctor);

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("HiDoctor Alert")
                .setContentText(message)
                .setAutoCancel(true)
                        //.setSound(defaultSoundUri)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        //Util.play(this, R.raw.hidoctor);
    }

    /*private static final String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String property = data.getString("property");
        String body     = data.getString("body");

        LogSdk.d(TAG, "GcmListenerService :: From     : " + from);
        LogSdk.d(TAG, "GcmListenerService :: full     : " + data.toString());
        LogSdk.d(TAG, "GcmListenerService :: property : " + property);
        LogSdk.d(TAG, "GcmListenerService :: body     : " + body);

        ooVooSdkSampleShowApp application = (ooVooSdkSampleShowApp) getApplication();

        Intent intent = new Intent(application.getContext(), ChatsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(application.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(application.getContext());

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(property != null ? property : from)
                .setContentTitle(property != null ? property : from)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");


        NotificationManager notificationManager = (NotificationManager) application.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());

    }*/
}
