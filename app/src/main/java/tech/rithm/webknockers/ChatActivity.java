package tech.rithm.webknockers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.rithm.webknockers.data.ChatRoomContract;
import tech.rithm.webknockers.models.WebChatMessage;
import tech.rithm.webknockers.models.WebInstanceId;

/**
 * Created by rithm on 2/18/2017.
 */

public class ChatActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "**Chat Activity**";
    private static final String MESSAGES = "/messages";

    // loaders
    private static final int ID_CHAT_LOADER = 77;
    public static final String[] CHAT_ROOM_PROJECTION = {
            ChatRoomContract.ChatEntry.COLUMN_DATE,
            ChatRoomContract.ChatEntry.COLUMN_WEBKNOCKER_TABLE
    };
    public static final int INDEX_CHAT_DATE = 0;
    public static final int INDEX_CHAT_TABLE = 1;

    // firebase
    private DatabaseReference mFireBase;
    private DatabaseReference myTimeRef;
    private ChildEventListener myTimeRefListener;
    private FireBaseChatAdapter mFireAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private Cursor mCursor;

    private Uri mUri;
    private String mTable;
    private String mUsername;
    private String mPhotoUrl;
    @BindView(R.id.messageRecyclerView) RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    @BindView(R.id.messageEditText) EditText mMessageEditText;
    @BindView(R.id.sendButton) Button mSendButton;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        ButterKnife.bind(this);
        mUri = getIntent().getData();
        mUsername = "ANONYMOUS";

        mFireBase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        if ( mFirebaseUser == null ) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null){
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        getSupportLoaderManager().initLoader(ID_CHAT_LOADER, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTimeRef = FirebaseDatabase.getInstance().getReference(mTable + MESSAGES);
        myTimeRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long time = 0L;
                for (DataSnapshot messageSnapShot: dataSnapshot.getChildren()) {
                    time = (Long) messageSnapShot.child("timeStamp").getValue();
                    Log.d(TAG, "time -->" + time);
                }
                ContentValues chatValues = new ContentValues();
                chatValues.put(ChatRoomContract.ChatEntry.COLUMN_LAST_READ, time);
                ContentResolver chatResolver = getContentResolver();
                chatResolver.update(mUri, chatValues, null, null);
                Log.d(TAG, "*****" + mUri.toString() + "******");
                Log.d(TAG, "****** UPDATED LAST READ ******");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        if (data != null && data.moveToFirst()) {
            mTable = data.getString(INDEX_CHAT_TABLE);
        }
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mFireAdapter = new FireBaseChatAdapter(WebChatMessage.class, R.layout.item_message,
                FireBaseChatAdapter.FireViewHolder.class, mFireBase.child(mTable + MESSAGES), this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFireAdapter);



    }

    @OnClick(R.id.sendButton)
    public void sendMessage() {
        long dateTimeMillis = System.currentTimeMillis();
        WebChatMessage webChatMessage = new WebChatMessage(mMessageEditText.getText().toString(),
                mUsername, mPhotoUrl, dateTimeMillis);
        mFireBase.child(mTable + MESSAGES).push().setValue(webChatMessage);
        mMessageEditText.setText("");
    }

    @Override
    public Loader<Cursor> onCreateLoader( int loaderId, Bundle bundle ) {
        switch(loaderId) {
            case ID_CHAT_LOADER:
                return new CursorLoader(this,
                        mUri,
                        CHAT_ROOM_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
