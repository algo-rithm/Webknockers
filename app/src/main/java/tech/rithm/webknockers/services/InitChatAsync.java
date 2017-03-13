package tech.rithm.webknockers.services;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import tech.rithm.webknockers.backend.messaging.Messaging;

/**
 * Created by rithm on 2/17/2017.
 */

public class InitChatAsync extends AsyncTask<String, Void, Void> {

    @Override
    public Void doInBackground(String... params) {

        String message = params[0];
        String to = params[1];
        String from = params[2];

        Messaging.Builder builder = new Messaging.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                .setRootUrl(RegistrationIntentService.ROOT_URL);
        Messaging messager = builder.build();
        try{
            messager.sendToAppMessage(message, to, from).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
