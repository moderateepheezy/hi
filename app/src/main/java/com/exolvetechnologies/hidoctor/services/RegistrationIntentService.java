package com.exolvetechnologies.hidoctor.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.exolvetechnologies.hidoctor.activities.LoginActivity;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;
import com.exolvetechnologies.hidoctor.utilities.QuickstartPreferences;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by oovoo on 9/8/15.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "HiDoctor";
    private static final String SUP = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private static final String SENDER_ID = "749361195973";
    private SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super(SUP);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(SENDER_ID/*getString(R.string.gcm_defaultSenderId)*/,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);


            // Subscribe to topic channels
            subscribeTopics(token);

            if(sendRegistrationToServer(token)) {
                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                sharedPreferences.edit().putString(QuickstartPreferences.TOKEN, token).apply();
                // [END register_for_gcm]
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private boolean sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        boolean isSuccessful = false;
        SharedPreferences idPrefs = getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
        String userId = idPrefs.getString(LoginActivity.USER_ID, "");
        String oldToken = sharedPreferences.getString(QuickstartPreferences.TOKEN, "");
        String url = AppUtils.API_ROOT+"/notifications/regid/"+userId+"/"+token;
        Log.i(TAG, "URL: "+url);
        try {
            if (!token.equals(oldToken)) {
                isSuccessful = sentToServer(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return isSuccessful;
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    private boolean sentToServer(String myUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setReadTimeout(20000 /* milliseconds */);
            //conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            JSONObject res = new JSONObject(readIt(is));
            String success = res.getString("status");
            if (success.equals("success")){
                return true;
            }

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return false;
    }

    public String readIt(InputStream stream) throws IOException{
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader bReader = new BufferedReader(reader);
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = bReader.readLine()) != null) {
            out.append(line);
        }
        Log.i(TAG, "HTTP RESPONSE "+out.toString());
        return out.toString();
    }

    /*private static final String TAG = "RegistrationIntentService";
    
    private static final String SENDER_ID = "522796524817";

    public RegistrationIntentService() {
        super(TAG);

        LogSdk.i(TAG, "RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            ooVooSdkSampleShowApp application = (ooVooSdkSampleShowApp) getApplication();
            ApplicationSettings settings = application.getSettings();
            String username = settings.get(ApplicationSettings.Username);
            String token = settings.get(username);

            if (token == null) {
                InstanceID instanceID = InstanceID.getInstance(this);
                token = instanceID.getToken(SENDER_ID*//*getString(R.string.gcm_defaultSenderId)*//*, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                settings.put(username, token);
                settings.save();
                sendRegistrationToServer(token);
                LogSdk.i(TAG, "GCM Registration Token: " + token);
            }

            sharedPreferences.edit().putBoolean(ApplicationSettings.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception e) {
            LogSdk.e(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(ApplicationSettings.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed
        Intent registrationComplete = new Intent(ApplicationSettings.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    *//**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     *//*
    private void sendRegistrationToServer(String token) {

        ooVooSdkSampleShowApp application = (ooVooSdkSampleShowApp) getApplication();

        application.subscribe(token);
    }*/
}
