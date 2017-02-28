package tech.rithm.webknockers.services;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by rithm on 2/13/2017.
 */

public class MyGCMListenerService extends GcmListenerService {

    private static final String TAG = "### MyGcmLstrSrvce ###";

    @Override
    public void onMessageReceived( String from, Bundle data ) {

        Log.d(TAG, "MESSAGE RECIEVED");
        //Toast.makeText(this, data.get("message").toString(), Toast.LENGTH_LONG ).show();
    }
}