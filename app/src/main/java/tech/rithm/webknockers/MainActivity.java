package tech.rithm.webknockers;

import android.net.Uri;
import android.support.annotation.NonNull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.rithm.webknockers.data.ChatRoomContract;
import tech.rithm.webknockers.services.MyFirebaseMessagingService;

public class MainActivity extends AppCompatActivity implements
        ChatRoomListingFragment.Callback,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String CHAT_FRAGMENT_TAG = "CFTAG";
    private boolean mTwoPane;

    public static final String[] CHAT_ROOM_PROJECTION = {
            ChatRoomContract.ChatEntry.COLUMN_DATE,
            ChatRoomContract.ChatEntry.COLUMN_LAST_READ,
            ChatRoomContract.ChatEntry.COLUMN_WEBKNOCKER_TABLE
    };
    public static final int INDEX_CHAT_DATE = 0;
    public static final int INDEX_LAST_READ = 1;
    public static final int INDEX_CHAT_TABLE = 2;

    private BroadcastReceiver chatReciever;

    private FragmentManager fm;

    @BindView(R.id.toolbar) Toolbar mToolbar;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                goSettings();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        fm = getSupportFragmentManager();
        ChatRoomListingFragment fragment_listing;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        fragment_listing = (ChatRoomListingFragment) fm.findFragmentById(R.id.chat_listing_container);
        if (fragment_listing == null) {
            fragment_listing = new ChatRoomListingFragment();
            fm.beginTransaction().add(R.id.chat_listing_container, fragment_listing)
                    .commit();
        }

        if (findViewById(R.id.chat_room_container) != null) {
            Fragment fragment_chat = fm.findFragmentById(R.id.chat_room_container);
            if (fragment_chat == null) {
                fragment_chat = new ChatActivityFragment();
                fm.beginTransaction().add(R.id.chat_room_container, fragment_chat)
                .commit();
            }
            mTwoPane = true;
        }
        else {
            mTwoPane = false;
        }

        FirebaseUser mFirebaseUser;
        FirebaseAuth auth;

        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();

        if ( mFirebaseUser == null ) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        chatReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String token = intent.getStringExtra(MyFirebaseMessagingService.TOKEN);
                Toast.makeText(MainActivity.this, R.string.new_web_instance_msg, Toast.LENGTH_LONG)
                        .show();

                Intent start_init_chat = new Intent(MainActivity.this, InitChatActivity.class);
                start_init_chat.putExtra(InitChatActivity.TOKEN, token);
                startActivity(start_init_chat);
            }
        };
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
        ChatActivityFragment caf = (ChatActivityFragment)getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT_TAG);
        if(caf != null){
            if (caf.getArguments() != null && caf.getArguments().containsKey(ChatActivityFragment.DETAIL_URI)){
                Uri uri = caf.getArguments().getParcelable(ChatActivityFragment.DETAIL_URI);
                caf.onChatRoomChanged(uri);
            }
        }
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(chatReciever);
        super.onStop();
    }

    @Override
    public void onChatSelected(Uri uri){
        if (mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(ChatActivityFragment.DETAIL_URI, uri);
            ChatActivityFragment fragment = new ChatActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.chat_room_container, fragment, CHAT_FRAGMENT_TAG).commit();
        } else {
            Intent chatRoom = new Intent(this, ChatActivity.class);
            chatRoom.setData(uri);
            startActivity(chatRoom);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.google_firebase_error , Toast.LENGTH_SHORT).show();
    }
}
