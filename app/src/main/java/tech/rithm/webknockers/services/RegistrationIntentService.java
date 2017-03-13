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
import tech.rithm.webknockers.models.AppInstanceId;

/**
 * Created by rithm on 2/13/2017.
 */

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService ###";
    private static final String[] TOPICS = {"global"};
    private final String KEY_SENT_TOKEN = "SENT_TOKEN";
    public static final String ROOT_URL = "https://taboochat-8fc33.appspot.com/_ah/api/";
    private final String CHILD_APP_INSTANCE = "app_instance_tokens";
    private final String CHILD_TOPICS = "/topics/";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent( Intent intent ) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {

            String token = FirebaseInstanceId.getInstance().getToken();

            sendRegistrationToServer(token);

            subscribeTopics(token);

            storeToken(token);

            sharedPreferences.edit().putBoolean(KEY_SENT_TOKEN, true).apply();
        } catch ( Exception e ) {
            Log.d(TAG, getString(R.string.error_token), e);
        }


    }

    private void sendRegistrationToServer( String token ) throws IOException {
        Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                .setRootUrl(ROOT_URL);
        Registration registration = builder.build();

        registration.register(token).execute();
    }

    private void storeToken(String token){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        AppInstanceId appInstanceId = new AppInstanceId();
        appInstanceId.setApp_instance_token(token);
        myRef.child(CHILD_APP_INSTANCE).push().setValue(appInstanceId);
        //myRef.child(CHILD_APP_INSTANCE).setValue(token);
    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for ( String topic : TOPICS ) {
            pubSub.subscribe(token, CHILD_TOPICS + topic, null);
        }
    }
}
