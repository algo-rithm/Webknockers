package tech.rithm.webknockers;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.rithm.webknockers.data.ChatRoomContract;
import tech.rithm.webknockers.models.WebChatMessage;
import tech.rithm.webknockers.services.InitChatAsync;
import tech.rithm.webknockers.widget.ChatWidgetProvider;

/**
 * Created by rithm on 2/17/2017.
 */

public class InitChatActivity extends AppCompatActivity {

    public static final String TOKEN = "com.webknockers.token";
    private static final String MESSAGE_CHILD = "/messages";

    private DatabaseReference mFireBase;
    private String webInstanceToken;
    private String userName;
    private String userPhotoUrl;

    @BindView(R.id.greeting_message_text) EditText greeting_msg_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_init_chat);
        ButterKnife.bind(this);

        webInstanceToken = getIntent().getStringExtra(TOKEN);
        if (webInstanceToken == null) {
            Uri uri = getIntent().getData();
            webInstanceToken = uri.getLastPathSegment();
        }

        FirebaseUser mFirebaseUser;
        FirebaseAuth mAuth;
        mFireBase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        if ( mFirebaseUser == null ) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            userName = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null){
                userPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String greeting = sharedPrefs.getString(getString(R.string.key_greeting), getString(R.string.greeting_message));
        greeting_msg_et.setText(greeting);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.bttn_init_chat)
    public void initChat() {
        String greeting_msg = greeting_msg_et.getText().toString();
        long dateTimeMillis = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        ContentValues chatValues = new ContentValues();
        chatValues.put(ChatRoomContract.ChatEntry.COLUMN_DATE, dateTimeMillis);
        chatValues.put(ChatRoomContract.ChatEntry.COLUMN_LAST_READ, dateTimeMillis);
        chatValues.put(ChatRoomContract.ChatEntry.COLUMN_WEBKNOCKER_TABLE, uuid);
        chatValues.put(ChatRoomContract.ChatEntry.COLUMN_HAS_NEW_MSG, 0);
        chatValues.put(ChatRoomContract.ChatEntry.COLUMN_NUM_MSGS, 1);
        ContentResolver chatResolver = getContentResolver();
            chatResolver.insert(ChatRoomContract.ChatEntry.CONTENT_URI, chatValues);

        WebChatMessage webChatMessage = new WebChatMessage(greeting_msg,
                userName, userPhotoUrl, dateTimeMillis);
        mFireBase.child(uuid + MESSAGE_CHILD).push().setValue(webChatMessage);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, ChatWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appIds, R.id.chat_widget_list);

        String[] params = {uuid, webInstanceToken, uuid};
        new InitChatAsync().execute(params);

        Intent chatRooms = new Intent(InitChatActivity.this, MainActivity.class);
        startActivity(chatRooms);
        finish();
    }
}
