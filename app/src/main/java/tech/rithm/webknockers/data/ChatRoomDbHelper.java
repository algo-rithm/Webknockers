package tech.rithm.webknockers.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tech.rithm.webknockers.data.ChatRoomContract.ChatEntry;

/**
 * Created by rithm on 2/18/2017.
 */

public class ChatRoomDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "chat_rooms.db";

    private static final int DATABASE_VERSION = 2;

    public ChatRoomDbHelper( Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate( SQLiteDatabase sqLiteDatabase ) {
        final String SQL_CREATE_CHAT_TABLE =
            "CREATE TABLE " + ChatEntry.TABLE_NAME  + " (" +
                ChatEntry._ID                       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ChatEntry.COLUMN_DATE               + " INTEGER NOT NULL, " +
                ChatEntry.COLUMN_LAST_READ          + " INTEGER NOT NULL, " +
                ChatEntry.COLUMN_WEBKNOCKER_TABLE   + " TEXT"+ ");";

        sqLiteDatabase.execSQL(SQL_CREATE_CHAT_TABLE);
    }

    @Override
    public void onUpgrade( SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion ) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChatEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
