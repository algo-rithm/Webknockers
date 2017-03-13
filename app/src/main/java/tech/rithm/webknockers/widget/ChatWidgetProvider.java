package tech.rithm.webknockers.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import tech.rithm.webknockers.ChatActivity;
import tech.rithm.webknockers.R;
import tech.rithm.webknockers.data.ChatRoomContract;
import tech.rithm.webknockers.widget.ChatWidgetService;

/**
 * Created by rithm on 3/2/2017.
 */

public class ChatWidgetProvider extends AppWidgetProvider{

    public static final String LAUNCH_CHAT = "tech.rithm.webknockers.LAUNCH_CHAT";
    public static final String EXTRA_CHAT_ROOM = "tech.rithm.webknockers.CHAT_ROOM";

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(LAUNCH_CHAT)) {
            String table = intent.getStringExtra(EXTRA_CHAT_ROOM);

            Intent chatIntent = new Intent(context, ChatActivity.class);
            Uri uri = ChatRoomContract.ChatEntry.buildChatUriWithWebknockerTable(table);
            chatIntent.setData(uri);
            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chatIntent);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent intent = new Intent(context, ChatWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.chat_widget_layout);
            rv.setRemoteAdapter(R.id.chat_widget_list, intent);
            rv.setEmptyView(R.id.chat_widget_list, R.id.chat_widget_empty);

            Intent launchChatIntent = new Intent(context, ChatWidgetProvider.class);
            launchChatIntent.setAction(ChatWidgetProvider.LAUNCH_CHAT);
            launchChatIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            launchChatIntent.setData(Uri.parse(launchChatIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent launchPending = PendingIntent.getBroadcast(context, 0, launchChatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.chat_widget_list, launchPending);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}