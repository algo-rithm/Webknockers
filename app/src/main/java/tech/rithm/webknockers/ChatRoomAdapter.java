package tech.rithm.webknockers;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tech.rithm.webknockers.data.ChatRoomContract;
import tech.rithm.webknockers.widget.ChatWidgetProvider;

import static tech.rithm.webknockers.R.color.colorPrimaryDark;

/**
 * Created by rithm on 2/18/2017.
 */

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    interface ChatRoomAdapterOnClickHandler {
        void onClick(String table_name);
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView chat_time;
        final TextView count;
        final TextView web_domain;
        final TextView msg_ago;
        final ImageButton delete_bttn;
        final ImageView fist;
        final View view;

        ChatRoomViewHolder(View view) {
            super(view);
            this.view = view;
            final DatabaseReference mFirebase = FirebaseDatabase.getInstance().getReference();

            //table_name = (TextView) view.findViewById(R.id.instance_id);
            chat_time = (TextView) view.findViewById(R.id.main_timestamp);
            delete_bttn = (ImageButton) view.findViewById(R.id.main_bttn_del);
            count = (TextView) view.findViewById(R.id.count);
            msg_ago = (TextView) view.findViewById(R.id.msg_ago);
            fist = (ImageView) view.findViewById(R.id.fist_logo);
            web_domain = (TextView) view.findViewById(R.id.web_domain);

            view.setOnClickListener(this);

            delete_bttn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);
                    Uri uri = ChatRoomContract.ChatEntry.buildChatUriWithWebknockerTable(mCursor.getString(MainActivity.INDEX_CHAT_TABLE));

                    ContentResolver resolver = mContext.getContentResolver();
                    resolver.delete(uri, null, null);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                    int[] appIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, ChatWidgetProvider.class));
                    appWidgetManager.notifyAppWidgetViewDataChanged(appIds, R.id.chat_widget_list);

                    mFirebase.child(mCursor.getString(MainActivity.INDEX_CHAT_TABLE)).removeValue();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onClick(View v) {
            int  adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            String table_name = mCursor.getString(MainActivity.INDEX_CHAT_TABLE);
            mClickHandler.onClick(table_name);
        }
    }

    private final Context mContext;
    private final ChatRoomAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;

    private final static String DB_MESSAGE_CHILD = "messages";
    private final static String REF_MESSAGE_CHILD = "/messages";
    private final static String DB_WEB_INFO_CHILD = "web-info";
    private final static String DB_TIMESTAMP_CHILD = "timeStamp";
    private final static String DB_SITE_DOMAIN_CHILD = "web_site_domain";

    ChatRoomAdapter(@NonNull Context context, ChatRoomAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public ChatRoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_instance, viewGroup, false);
        view.setFocusable(true);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChatRoomViewHolder chatVH, int position) {
        mCursor.moveToPosition(position);

        final String table_name = mCursor.getString(MainActivity.INDEX_CHAT_TABLE);
        long timeStamp = mCursor.getLong(MainActivity.INDEX_CHAT_DATE);
        long dateTimeMillis = System.currentTimeMillis();
        long createdAgo = dateTimeMillis - timeStamp;
        String timeAgo = TimeUtil.toDuration(createdAgo);

        final long lastRead = mCursor.getLong(MainActivity.INDEX_LAST_READ);

        chatVH.chat_time.setText(timeAgo);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(table_name);
        database.getReference(table_name + REF_MESSAGE_CHILD).addChildEventListener(
            new ChildEventListener() {


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Long last_message = dataSnapshot.child(DB_TIMESTAMP_CHILD).getValue(Long.class);

                if (lastRead == last_message) {
                    Long now = System.currentTimeMillis();
                    Long since = now - last_message;
                    String ago = TimeUtil.toDuration(since);
                    chatVH.msg_ago.setTextColor(Color.GRAY);
                    chatVH.msg_ago.setText(ago);
                    chatVH.fist.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.fist_grey));
                } else {
                    chatVH.msg_ago.setTextColor(mContext.getResources().getColor(colorPrimaryDark));
                    chatVH.msg_ago.setText(mContext.getResources().getText(R.string.new_message));
                    chatVH.fist.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.fist_green));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        myRef.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.child(DB_MESSAGE_CHILD).getChildrenCount();
                chatVH.count.setText(String.valueOf(count));

                if (dataSnapshot.hasChild(DB_WEB_INFO_CHILD)) {
                    String domain = dataSnapshot.child(DB_WEB_INFO_CHILD).child(DB_SITE_DOMAIN_CHILD).getValue(String.class);
                    chatVH.web_domain.setText(domain);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }));


    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }


}
