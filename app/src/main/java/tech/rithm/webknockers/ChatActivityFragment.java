package tech.rithm.webknockers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.rithm.webknockers.data.ChatRoomContract;
import tech.rithm.webknockers.models.WebChatMessage;

/**
 * Created by rithm on 2/25/2017.
 */

public class ChatActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>  {
    public static final String DETAIL_URI = "URI";
    private static final String MESSAGES = "/messages";
    private static final String TIMESTAMP = "timeStamp";

    private Context mContext;

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

    private Cursor mCursor;
    private Uri mUri;
    private String mTable;
    private String mUsername;
    private String mPhotoUrl;
    @BindView(R.id.messageRecyclerView) RecyclerView mMessageRecyclerView;
    @BindView(R.id.messageEditText) EditText mMessageEditText;
    @BindView(R.id.sendButton) Button mSendButton;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

    private boolean loadEmpty = false;

    public ChatActivityFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mContext = getActivity();

        mUri = getActivity().getIntent().getData();
        if (mUri == null){
            Bundle arguments = getArguments();
            if( arguments != null) {
                mUri = arguments.getParcelable(ChatActivityFragment.DETAIL_URI);
            } else {
                loadEmpty = true;
                return;
            }
        }

        mUsername = "ANONYMOUS";
        mFireBase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if ( mFirebaseUser == null ) {
            startActivity(new Intent(getActivity(), SignInActivity.class));
            getActivity().finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null){
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
    }

    private ValueEventListener lastReadListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!mUri.toString().equals("")) {
                Long time = 0L;
                for (DataSnapshot messageSnapShot : dataSnapshot.getChildren()) {
                    time = (Long) messageSnapShot.child(TIMESTAMP).getValue();
                }
                ContentValues chatValues = new ContentValues();
                chatValues.put(ChatRoomContract.ChatEntry.COLUMN_LAST_READ, time);
                ContentResolver chatResolver = getActivity().getApplicationContext().getContentResolver();
                chatResolver.update(mUri, chatValues, null, null);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(loadEmpty) return super.onCreateView(inflater,container,savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);
        ButterKnife.bind(this, view);

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

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!loadEmpty) {
            getLoaderManager().initLoader(ID_CHAT_LOADER, null, this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        DatabaseReference myTimeRef = FirebaseDatabase.getInstance().getReference(mTable + MESSAGES);
        myTimeRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long time = 0L;
                for (DataSnapshot messageSnapShot: dataSnapshot.getChildren()) {
                    time = (Long) messageSnapShot.child(TIMESTAMP).getValue();
                }
                ContentValues chatValues = new ContentValues();
                chatValues.put(ChatRoomContract.ChatEntry.COLUMN_LAST_READ, time);
                ContentResolver chatResolver = mContext.getContentResolver();
                if (mUri != null) chatResolver.update(mUri, chatValues, null, null);
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

    public void onChatRoomChanged(Uri uri) {
        mUri = uri;
        getLoaderManager().restartLoader(ID_CHAT_LOADER, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursor = data;
        if (data != null && data.moveToFirst()) {
            mTable = data.getString(INDEX_CHAT_TABLE);
        }
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        FireBaseChatAdapter mFireAdapter = new FireBaseChatAdapter(WebChatMessage.class, R.layout.item_message,
                FireBaseChatAdapter.FireViewHolder.class, mFireBase.child(mTable + MESSAGES), getActivity());
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
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

                    return new CursorLoader(getActivity(),
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
