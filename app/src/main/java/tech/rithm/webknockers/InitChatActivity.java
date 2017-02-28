package tech.rithm.webknockers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

/**
 * Created by rithm on 2/17/2017.
 */

public class InitChatActivity extends AppCompatActivity {

    public static final String TOKEN = "com.webknockers.token";
    private static final String TAG = "**InitChatActvy**";

    private DatabaseReference mFireBase;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
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


        Log.d(TAG, "onCreate - " + webInstanceToken);
        mFireBase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        if ( mFirebaseUser == null ) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            userName = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null){
                userPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //webInstanceToken = getIntent().getStringExtra(TOKEN);
        Log.d(TAG, "onNewIntent - " + webInstanceToken);
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

        Log.d(TAG, "inserting row");
        ContentValues chatValues = new ContentValues();
        chatValues.put(ChatRoomContract.ChatEntry.COLUMN_DATE, dateTimeMillis);
        chatValues.put(ChatRoomContract.ChatEntry.COLUMN_LAST_READ, dateTimeMillis);
        chatValues.put(ChatRoomContract.ChatEntry.COLUMN_WEBKNOCKER_TABLE, uuid);
        ContentResolver chatResolver = getContentResolver();
            chatResolver.insert(ChatRoomContract.ChatEntry.CONTENT_URI, chatValues);

        WebChatMessage webChatMessage = new WebChatMessage(greeting_msg,
                userName, userPhotoUrl, dateTimeMillis);
        mFireBase.child(uuid + "/messages").push().setValue(webChatMessage);


        Log.d(TAG, "sending msg to website...");
        String[] params = {uuid, webInstanceToken, uuid};
        new InitChatAsync().execute(params);

        Intent chatRooms = new Intent(InitChatActivity.this, MainActivity.class);
        startActivity(chatRooms);
        finish();

/*
        Intent chatRoom = new Intent(InitChatActivity.this, ChatActivity.class);
        Uri uriForChatRoom = ChatRoomContract.ChatEntry.buildChatUriWithWebknockerTable(uuid);
        chatRoom.setData(uriForChatRoom);
        startActivity(chatRoom);
        finish();

        */

    }
}
