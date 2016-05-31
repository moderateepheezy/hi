package com.exolvetechnologies.hidoctor.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DoctorChatInitActivity extends AppCompatActivity {

    public static final String TAG = "HiDoctor";

    private AppUtils utils;
    private static String chat_Id;
    private static String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_chat_init);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        utils = new AppUtils(this);

        Intent intent = getIntent();

        String conId = intent.getStringExtra("CON_ID");
        type = intent.getStringExtra("TYPE");
        String cat = intent.getStringExtra("CAT");
        String time = intent.getStringExtra("TIME");
        chat_Id = intent.getStringExtra("CHAT_ID");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ChatsActivity.SESSION_ID = conId;
        ChatsActivity.TIME_LEFT = Long.parseLong(time);
        ChatsActivity.CHAT_TYPE = type;
        ChatsActivity.CHAT_CATEGORY_ID = cat;

        if (utils.isConnectedToInternet()) {
            new DoctorJoinChatAsync(chat_Id).execute();
        }
    }

    public class DoctorJoinChatAsync extends AsyncTask<Void, Void, String> {

        String chatId;

        public DoctorJoinChatAsync(String chatId) {
            this.chatId = chatId;
        }

        @Override
        protected String doInBackground(Void... result) {
            SharedPreferences idPrefs = getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
            String userId = idPrefs.getString(LoginActivity.USER_ID, "");
            String url = AppUtils.API_ROOT+"/api/joinchat/"+chatId+"/"+userId;

            Log.i(TAG, "URL: "+url);
            try {
                //joinChat(url);
               return AppUtils.getRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null){
                Log.i(TAG, "RESPONSE "+s);
                try {
                    JSONObject response = new JSONObject(s);
                    boolean isAvailable = response.getBoolean("room_available");
                    //Log.i(TAG, "Status "+isAvailable);
                    if (isAvailable){
                        //Log.i(TAG, "Is Available");
                        if (type.equals("text")){
                            startActivity(new Intent(DoctorChatInitActivity.this, TextChatActivity.class));
                        }else {
                            startActivity(new Intent(DoctorChatInitActivity.this, ChatsActivity.class));
                        }
                    }else {
                        utils.showAlert(DoctorChatInitActivity.this, "Chat Status", "Chat no longer available");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
