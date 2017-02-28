package tech.rithm.webknockers;

import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.rithm.webknockers.data.ChatRoomContract;
import tech.rithm.webknockers.models.WebInstanceId;
import tech.rithm.webknockers.services.MyFirebaseMessagingService;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ChatRoomAdapter.ChatRoomAdapterOnClickHandler,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int ID_CHAT_LOADER = 88;
    public static final String[] CHAT_ROOM_PROJECTION = {
            ChatRoomContract.ChatEntry.COLUMN_DATE,
            ChatRoomContract.ChatEntry.COLUMN_LAST_READ,
            ChatRoomContract.ChatEntry.COLUMN_WEBKNOCKER_TABLE
    };
    public static final int INDEX_CHAT_DATE = 0;
    public static final int INDEX_LAST_READ = 1;
    public static final int INDEX_CHAT_TABLE = 2;

    private static final String CHAT_ACTION = "tech.rithm.webknocker.chat_action";
    private static final String ANONYMOUS = "anonymous";
    private BroadcastReceiver chatReciever;

    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference mFireDatabase;
    private String mUsername;
    private String mPhotoUrl;
    private FirebaseUser mFirebaseUser;
    private GoogleSignInAccount acct;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;


    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.instance_recycler) RecyclerView mRecyclerView;
    @BindView(R.id.no_chat_rooms) TextView mNoChatRooms;
    private ChatRoomAdapter mChatRoomAdapter;
    private int mPosition = RecyclerView.NO_POSITION;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setIcon(R.drawable.webknocker_header);
        getSupportActionBar().setTitle("");

        mUsername = ANONYMOUS;
        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();

        if ( mFirebaseUser == null ) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if ( mFirebaseUser.getPhotoUrl() != null ) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mChatRoomAdapter = new ChatRoomAdapter(this, this);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mChatRoomAdapter);

        chatReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String token = intent.getStringExtra(MyFirebaseMessagingService.TOKEN);
                Toast.makeText(MainActivity.this, "NEW WEBSITE INSTANCE MESSAGE -\n" + token, Toast.LENGTH_LONG)
                        .show();

                Intent start_init_chat = new Intent(MainActivity.this, InitChatActivity.class);
                start_init_chat.putExtra(InitChatActivity.TOKEN, token);
                startActivity(start_init_chat);
            }
        };

        getSupportLoaderManager().initLoader(ID_CHAT_LOADER, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(chatReciever,
                new IntentFilter(MyFirebaseMessagingService.CHAT_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(ID_CHAT_LOADER, null, this);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(chatReciever);
        super.onStop();
    }

    @Override
    public void onClick(String chat_room) {
        Intent chatRoom = new Intent(MainActivity.this, ChatActivity.class);
        Uri uriForChatRoom = ChatRoomContract.ChatEntry.buildChatUriWithWebknockerTable(chat_room);
        chatRoom.setData(uriForChatRoom);
        startActivity(chatRoom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 69:

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case ID_CHAT_LOADER:
                Uri chatQuery = ChatRoomContract.ChatEntry.CONTENT_URI;
                String  sortOrder = ChatRoomContract.ChatEntry.COLUMN_DATE + " ASC";
                return new CursorLoader(this,
                        chatQuery,
                        CHAT_ROOM_PROJECTION,
                        null,
                        null,
                        sortOrder);
            default:throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.getCount() == 0){
            mRecyclerView.setVisibility(View.INVISIBLE);
            mNoChatRooms.setVisibility(View.VISIBLE);
        }

        mChatRoomAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mChatRoomAdapter.swapCursor(null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d("TAG", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
