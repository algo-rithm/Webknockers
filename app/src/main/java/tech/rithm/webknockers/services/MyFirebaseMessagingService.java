package tech.rithm.webknockers.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import tech.rithm.webknockers.InitChatActivity;
import tech.rithm.webknockers.R;
import tech.rithm.webknockers.data.ChatRoomContract;

/**
 * Created by rithm on 2/13/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFMService";
    public static final String CHAT_ACTION = "tech.rithm.webknocker.chat_action";
    public static final String MSG_ID = "msg_id";
    public static final String MSG = "msg";
    public static final String TOKEN = "token";
    private static int mId = 1;

    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d(TAG, "FCM Notification Message: " + remoteMessage.getNotification());
        Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());

        String id = remoteMessage.getMessageId();
        String msg = remoteMessage.getData().get("msg");
        String token = remoteMessage.getData().get("token");

        if ( msg.equals("WantToChitChat")) {
            Log.d(TAG, "SENDING Intent to UI");

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }


            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(getApplicationContext(), InitChatActivity.class);
            Uri uri = ChatRoomContract.ChatEntry.buildChatUriWithWebknockerTable(token);
            notificationIntent.putExtra(TOKEN, token);
            notificationIntent.setData(uri);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_CLEAR_TASK );
            PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Webknockers")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("New WEB instance!!"))
                    .setContentText("New WEB instance!!");
            mBuilder.setContentIntent(contentIntent);
            notificationManager.notify( ++mId, mBuilder.build());


            Intent local = new Intent();
            local.setAction(CHAT_ACTION);
            local.putExtra(MSG_ID, id);
            local.putExtra(MSG, msg);
            local.putExtra(TOKEN, token);
            LocalBroadcastManager.getInstance(this).sendBroadcast(local);

        }
    }
}
