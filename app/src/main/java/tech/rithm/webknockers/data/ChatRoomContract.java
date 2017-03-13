package tech.rithm.webknockers.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rithm on 2/18/2017.
 */

public class ChatRoomContract {

    public static final String CONTENT_AUTHORITY = "com.webknockers.android.chat";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CHAT = "chat";

    public static final class ChatEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CHAT)
                .build();

        public static final String TABLE_NAME = "chat";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LAST_READ = "last_read";
        public static final String COLUMN_HAS_NEW_MSG = "has_new_msg";
        public static final String COLUMN_NUM_MSGS = "num_msgs";
        public static final String COLUMN_WEBKNOCKER_TABLE = "webknocker_table";

        public static Uri buildChatUriWithWebknockerTable(String table) {
            return CONTENT_URI.buildUpon().appendPath(table).build();
        }
    }
}
