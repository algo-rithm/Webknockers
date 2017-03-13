package tech.rithm.webknockers;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.rithm.webknockers.data.ChatRoomContract;

/**
 * Created by rithm on 2/25/2017.
 */

public class ChatRoomListingFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ChatRoomAdapter.ChatRoomAdapterOnClickHandler{

    public interface Callback {
        public void onChatSelected(Uri chatUri);
    }

    private static final String TAG = "**FRAG LISTING***";

    private static final int ID_CHAT_LOADER = 88;
    public static final String[] CHAT_ROOM_PROJECTION = {
            ChatRoomContract.ChatEntry.COLUMN_DATE,
            ChatRoomContract.ChatEntry.COLUMN_LAST_READ,
            ChatRoomContract.ChatEntry.COLUMN_WEBKNOCKER_TABLE
    };
    public static final int INDEX_CHAT_DATE = 0;
    public static final int INDEX_LAST_READ = 1;
    public static final int INDEX_CHAT_TABLE = 2;

    @BindView(R.id.instance_recycler) RecyclerView mRecyclerView;
    private ChatRoomAdapter mChatRoomAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    @BindView(R.id.no_chat_rooms) TextView mNoChatRooms;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(false);
        mChatRoomAdapter = new ChatRoomAdapter(getActivity(), this);
        getActivity().getSupportLoaderManager().initLoader(ID_CHAT_LOADER, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mChatRoomAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(ID_CHAT_LOADER, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(String chat_room) {
        Uri uriForChatRoom = ChatRoomContract.ChatEntry.buildChatUriWithWebknockerTable(chat_room);
        ((Callback) getActivity()).onChatSelected(uriForChatRoom);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case ID_CHAT_LOADER:
                Uri chatQuery = ChatRoomContract.ChatEntry.CONTENT_URI;
                String  sortOrder = ChatRoomContract.ChatEntry.COLUMN_DATE + " ASC";
                return new CursorLoader(getActivity(),
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
}
