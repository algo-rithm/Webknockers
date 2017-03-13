package tech.rithm.webknockers.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tech.rithm.webknockers.R;
import tech.rithm.webknockers.TimeUtil;
import tech.rithm.webknockers.data.ChatRoomContract;
import tech.rithm.webknockers.models.ChatRoom;

/**
 * Created by rithm on 3/2/2017.
 */

public class ChatWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ChatRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ChatRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final String TAG = "*** WIDGET ***";

    private Context mContext;
    private int mAppWidgetId;
    private List<ChatRoom> mChats = new ArrayList<>();
    private Cursor mCursor;

    public static final String[] CHAT_ROOM_PROJECTION = {
            ChatRoomContract.ChatEntry.COLUMN_DATE,
            ChatRoomContract.ChatEntry.COLUMN_HAS_NEW_MSG,
            ChatRoomContract.ChatEntry.COLUMN_NUM_MSGS,
            ChatRoomContract.ChatEntry.COLUMN_LAST_READ,
            ChatRoomContract.ChatEntry.COLUMN_WEBKNOCKER_TABLE
    };
    public static final int INDEX_CHAT_DATE = 0;
    public static final int INDEX_HAS_NEW_MSG = 1;
    public static final int INDEX_NUM_MSGS = 2;
    public static final int INDEX_LAST_READ = 3;
    public static final int INDEX_CHAT_TABLE = 4;

    private FirebaseDatabase firebase;

    public ChatRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        firebase = FirebaseDatabase.getInstance();


    }

    @Override
    public int getCount() {
        Log.v(TAG, "mChats.size ---> " + mChats.size()  );
        return mChats.size();

    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.v(TAG, "getViewAt");
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.chat_widget_item);
        ChatRoom chatRoom = mChats.get(position);
        Log.v(TAG, chatRoom.toString());

        String table = chatRoom.getTable();


        rv.setTextViewText(R.id.widget_count, String.valueOf(position + 1));
        //int newMsg = chatRoom.getHasNewMSG();

        long timeAgo = chatRoom.getLastReadDate();
        long now = System.currentTimeMillis();
        String timeAgoString = TimeUtil.toDuration(now - timeAgo);



        Date date = new Date(timeAgo);
        rv.setTextViewText(R.id.widget_status, timeAgoString);
        rv.setImageViewResource(R.id.widget_logo, R.mipmap.fist_green);


        Bundle extras = new Bundle();
        extras.putString(ChatWidgetProvider.EXTRA_CHAT_ROOM, table);
        Intent fill = new Intent();
        fill.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_box, fill);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onDataSetChanged() {
        mChats = new ArrayList<>();

        mCursor = mContext.getContentResolver().query(ChatRoomContract.ChatEntry.CONTENT_URI,
                CHAT_ROOM_PROJECTION,
                null,
                null,
                null);

        if (mCursor != null && mCursor.moveToFirst()) {
            while (!mCursor.isAfterLast()) {
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setCreatedDate(mCursor.getLong(INDEX_CHAT_DATE));
                chatRoom.setHasNewMSG(mCursor.getInt(INDEX_HAS_NEW_MSG));
                chatRoom.setLastReadDate(mCursor.getLong(INDEX_LAST_READ));
                chatRoom.setNumMSGS(mCursor.getInt(INDEX_NUM_MSGS));
                chatRoom.setTable(mCursor.getString(INDEX_CHAT_TABLE));
                mChats.add(chatRoom);
                mCursor.moveToNext();
            }
            mCursor.close();
        }

    }

    @Override
    public void onDestroy() {
        mChats = null;
        if (mCursor != null) mCursor.close();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
