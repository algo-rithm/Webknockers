package tech.rithm.webknockers.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import tech.rithm.webknockers.R;
import tech.rithm.webknockers.backend.registration.Registration;

/**
 * Created by rithm on 2/13/2017.
 */

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService ###";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent( Intent intent ) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            //InstanceID instanceID = InstanceID.getInstance(this);
            //String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                   // GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            String token = FirebaseInstanceId.getInstance().getToken();

            Log.i(TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token);

            subscribeTopics(token);

            storeToken(token);

            sharedPreferences.edit().putBoolean("SENT_TOKEN", true).apply();
        } catch ( Exception e ) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }


    }

    private void sendRegistrationToServer( String token ) throws IOException {
        Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                .setRootUrl("https://taboochat-8fc33.appspot.com/_ah/api/");
        Registration registration = builder.build();

        registration.register(token).execute();
    }

    private void storeToken(String token){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        myRef.child("app_instance_token").setValue(token);
    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for ( String topic : TOPICS ) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
