package tech.rithm.webknockers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import tech.rithm.webknockers.models.WebChatMessage;

/**
 * Created by rithm on 2/18/2017.
 */

public class FireBaseChatAdapter extends FirebaseRecyclerAdapter<WebChatMessage, FireBaseChatAdapter.FireViewHolder> {

    private Context mContext;

    public FireBaseChatAdapter(Class modelClass, int layout, Class viewHolderClass, DatabaseReference snapshots, Context context) {
        super(modelClass, layout, viewHolderClass, snapshots);
        mContext = context;
    }

    @Override
    protected void populateViewHolder(FireBaseChatAdapter.FireViewHolder chatVH, WebChatMessage chatMessage, int position) {
        chatVH.sender.setText(chatMessage.getName());
        chatVH.message.setText(chatMessage.getText());
        if(chatMessage.getPhotoUrl() != null){
            Glide.with(mContext).load(chatMessage.getPhotoUrl()).into(chatVH.profileImg);
        }

    }

    public static class FireViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public TextView sender;
        public ImageView profileImg;

        public FireViewHolder(View v) {
            super(v);
            message = (TextView) itemView.findViewById(R.id.message);
            sender = (TextView) itemView.findViewById(R.id.sender);
            profileImg = (ImageView) itemView.findViewById(R.id.profile);
        }
    }
}
