package tech.rithm.webknockers.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import tech.rithm.webknockers.data.ChatRoomContract.*;

import static tech.rithm.webknockers.data.ChatRoomContract.CONTENT_AUTHORITY;
import static tech.rithm.webknockers.data.ChatRoomContract.PATH_CHAT;

/**
 * Created by rithm on 2/18/2017.
 */

public class ChatRoomProvider extends ContentProvider {

    private static final String TAG = "**Cont PROV**";

    public static final int CODE_CHAT = 777;
    public static final int CODE_CHAT_WITH_TABLE = 999;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ChatRoomDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, PATH_CHAT, CODE_CHAT);
        matcher.addURI(authority, PATH_CHAT + "/*", CODE_CHAT_WITH_TABLE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ChatRoomDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query( @NonNull Uri uri, String[] projection, String selection,
                         String[] selectionArgs, String sortOrder ) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_CHAT_WITH_TABLE: {
                String webknocker_table = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{webknocker_table};
                cursor = mOpenHelper.getReadableDatabase().query(
                        ChatEntry.TABLE_NAME,
                        projection,
                        ChatEntry.COLUMN_WEBKNOCKER_TABLE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_CHAT: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        ChatEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long id=0;
        switch (sUriMatcher.match(uri)) {
            case CODE_CHAT:
                id = db.insert(ChatEntry.TABLE_NAME, null, values);
        }

        Log.d(TAG, "<-- " + id + " inserted -->");
        if (id != 0) {
            return new Uri.Builder().authority(CONTENT_AUTHORITY).appendPath(String.valueOf(id)).build();
        } else {
            throw new RuntimeException("did not insert");
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;
        if (selection == null) selection = "1";

        switch (sUriMatcher.match(uri)) {
            case CODE_CHAT:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        ChatEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_CHAT_WITH_TABLE:
                String web_row = uri.getLastPathSegment();
                String[] selectionArguments = new String[] {web_row};
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        ChatEntry.TABLE_NAME,
                        ChatEntry.COLUMN_WEBKNOCKER_TABLE + " = ? ",
                        selectionArguments);
                break;
            default:
                throw new UnsupportedOperationException("Unknown ur: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not implementing");
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        throw new RuntimeException("Not implementing");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String chat_table = uri.getLastPathSegment();
        String[] selectionArguments = new String[]{chat_table};

        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case CODE_CHAT_WITH_TABLE:
                rowsUpdated = db.update(ChatEntry.TABLE_NAME, values, ChatEntry.COLUMN_WEBKNOCKER_TABLE + " = ? ", selectionArguments);
                break;
            default:{
                throw new UnsupportedOperationException("Unknown URI");
            }
        }

        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
