package com.exolvetechnologies.hidoctor.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.models.PaymentCategories;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatTypeActivity extends AppCompatActivity {

    public static final String TAG = "HiDoctor";

    private ProgressBar progressBar;
    private Button videoButton, audioButton, textButton;
    private TextView wait;

    private AppUtils utils;

    private String userId;
    private static long textMins;
    private static long audioMins;
    private static long videoMins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_type);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
        userId = preferences.getString(LoginActivity.USER_ID, "0");
        utils = new AppUtils(this);

        progressBar = (ProgressBar) findViewById(R.id.checkWalletProgress);
        videoButton = (Button) findViewById(R.id.video);
        audioButton = (Button) findViewById(R.id.audio);
        textButton = (Button) findViewById(R.id.text);
        wait = (TextView) findViewById(R.id.waitText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (utils.isConnectedToInternet()){
            new GetBalanceAsync("video").execute();
            new GetBalanceAsync("audio").execute();
            new GetBalanceAsync("text").execute();
        }

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatsActivity.CHAT_TYPE = "video";

                long vMinFromMilSec = TimeUnit.MILLISECONDS.toMinutes(videoMins);
                //Log.i(TAG, "VIDEO MIN: "+vMinFromMilSec);
                if (vMinFromMilSec >= 5) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatTypeActivity.this);
                    dialog.setMessage("You currently have " + formatTime(videoMins) + " minutes of chat time, do you want to continue?")

                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    createRoom();
                                }
                            })
                            .setNegativeButton(getString(R.string.buy_more), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showPayCategories();
                                }
                            })
                            .create().show();
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatTypeActivity.this);
                    dialog.setMessage("Your available balance is too low")

                            .setNeutralButton(getString(R.string.buy_more), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showPayCategories();
                                }
                            }).create().show();
                }
            }
        });

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ChatsActivity.CHAT_TYPE = "audio";

                long aMinFromMilSec = TimeUnit.MILLISECONDS.toMinutes(audioMins);
                //Log.e(TAG, "AUDIO MIN: "+aMinFromMilSec);
                if (aMinFromMilSec >= 5) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatTypeActivity.this);
                    dialog.setMessage("You currently have "+formatTime(audioMins)+" minutes of chat time, do you want to continue?")

                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    createRoom();
                                }
                            })
                            .setNegativeButton(getString(R.string.buy_more), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showPayCategories();
                                }
                            })
                            .create().show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Your available balance is too low", Snackbar.LENGTH_LONG)
                            .setAction("Buy More Time", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showPayCategories();
                                }
                            }).show();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatTypeActivity.this);
                    dialog.setMessage("Your available balance is too low")

                            .setNeutralButton(getString(R.string.buy_more), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showPayCategories();
                                }
                            }).create().show();
                }
            }
        });

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatsActivity.CHAT_TYPE = "text";
                //Log.e("Choice", ChatActivity.USER_CHAT_TYPE_CHOICE);

                long tMinFromMilSec = TimeUnit.MILLISECONDS.toMinutes(textMins);
                //Log.e(TAG, "TEXT MIN: "+tMinFromMilSec);
                if (tMinFromMilSec > 5) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatTypeActivity.this);
                    dialog.setMessage("You currently have "+formatTime(textMins)+" minutes of chat time, do you want to continue?")

                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    createRoom();
                                }
                            })
                            .setNegativeButton(getString(R.string.buy_more), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showPayCategories();
                                }
                            })
                            .create().show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Your available balance is too low", Snackbar.LENGTH_LONG)
                            .setAction("Buy More Time", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showPayCategories();
                                }
                            }).show();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatTypeActivity.this);
                    dialog.setMessage("Your available balance is too low")

                            .setNeutralButton(getString(R.string.buy_more), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showPayCategories();
                                }
                            }).create().show();
                }
                //startActivity(new Intent(ChatTypeActivity.this, TextChatActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (utils.isConnectedToInternet()){
            new GetBalanceAsync("video").execute();
            new GetBalanceAsync("audio").execute();
            new GetBalanceAsync("text").execute();
        }
    }

    private void createRoom(){
        if (utils.isConnectedToInternet()){
            new CreateRoomAsync(ChatsActivity.CHAT_TYPE, ChatsActivity.CHAT_CATEGORY_ID).execute();
        }
    }

    private void showPayCategories(){
        if (utils.isConnectedToInternet()){
            new GetPackagesAsync().execute();
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatTime(long millisUntilFinished){
        //String secStr = String.valueOf(intSecs);
        //long secLong = (long)intSecs;//Long.parseLong(secStr.replace(".", ""));
        //long millisUntilFinished = TimeUnit.SECONDS.toMillis(secLong);
        return String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 110 && resultCode == RESULT_OK){
            Toast.makeText(this, "You have successfully credited your account", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetBalanceAsync extends AsyncTask<Void, Integer, String>{
        String chatType;

        public GetBalanceAsync(String chatType) {
            this.chatType = chatType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return AppUtils.getRequest(AppUtils.API_ROOT+"/api/getwallet/" + userId +"/"+chatType);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            wait.setVisibility(View.GONE);
            if (s != null){
                try {
                    JSONObject response = new JSONObject(s);
                    String min = response.getString("available_mins");
                    switch (chatType){
                        case "video":
                            videoMins = Long.parseLong(min);
                            videoButton.setEnabled(true);
                            break;
                        case "audio":
                            audioMins = Long.parseLong(min);
                            audioButton.setEnabled(true);
                            break;
                        case "text":
                            textMins = Long.parseLong(min);
                            textButton.setEnabled(true);
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
            Toast.makeText(ChatTypeActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
        }
            progressBar.setVisibility(View.GONE);
        }
    }

    private class CreateRoomAsync extends AsyncTask<Void, Integer, String>{
        String chatType;
        String chatCat;

        public CreateRoomAsync(String chatType, String chatCat) {
            this.chatType = chatType;
            this.chatCat = chatCat;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return AppUtils.getRequest(AppUtils.API_ROOT+"/api/setroom/"+userId+"/"+chatType+"/"+chatCat);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            if (s != null){
                Log.i(TAG, "RESPONSE: "+s);
                try {
                    JSONObject response = new JSONObject(s);
                    String status = response.getString("status");
                    if (status.equals("success")){
                        ChatsActivity.CHAT_ID = response.getString("chat_id");
                        ChatsActivity.SESSION_ID = response.getString("conferenceId");

                        switch (ChatsActivity.CHAT_TYPE){
                            case "video":
                                ChatsActivity.TIME_LEFT = videoMins;
                                break;
                            case "audio":
                                ChatsActivity.TIME_LEFT = audioMins;
                                break;
                            case "text":
                                ChatsActivity.TIME_LEFT = textMins;
                                break;
                        }

                        if (!ChatsActivity.CHAT_TYPE.equals("text")) {
                            startActivity(new Intent(ChatTypeActivity.this, ChatsActivity.class));
                        }else {
                            startActivity(new Intent(ChatTypeActivity.this, TextChatActivity.class));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
            Toast.makeText(ChatTypeActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
        }
        }
    }

    class PayCategoryAdapter extends ArrayAdapter {

        ArrayList<PaymentCategories> categories;

        public PayCategoryAdapter(Context context, int resource, ArrayList<PaymentCategories> paymentCategories) {
            super(context, resource, paymentCategories);
            categories = paymentCategories;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_airtime_purchase, parent, false);

            ((TextView)v.findViewById(R.id.type)).setText(categories.get(position).getName());

            ((TextView)v.findViewById(R.id.rate)).setText(categories.get(position).getDescription());
            ((TextView)v.findViewById(R.id.bundle)).setText("N" + categories.get(position).getBundleAmount());

            return v;
        }

    }

    private class GetPackagesAsync extends AsyncTask<Void, Integer, String>{
        @Override
        protected String doInBackground(Void... params) {
            try {
                return AppUtils.getRequest(AppUtils.API_ROOT+"/api/paymentpackages");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            if (s != null){
                final GsonBuilder builder = new GsonBuilder();
                builder.setVersion(1.0);

                final Gson gson = builder.create();

                final ArrayList<PaymentCategories> cats = gson.fromJson(s, new TypeToken<List<PaymentCategories>>() {
                }.getType());
                if (cats != null && (cats.size() > 0)){
                    //Log.i("TEST", cats.get(0).getDescription());
                    final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ChatTypeActivity.this);
                    ListAdapter adapter = new PayCategoryAdapter(ChatTypeActivity.this, R.layout.model_airtime_purchase, cats);

                    mBuilder.setTitle("Pay With Cash Envoy")
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            PaymentActivity.BUY_AMOUNT = cats.get(which).getBundleAmount();
                                            startActivityForResult(new Intent(ChatTypeActivity.this, PaymentActivity.class), 110);
                                            //Log.e("Pay", cats.get(which).getName());
                                            break;
                                        case 1:
                                            PaymentActivity.BUY_AMOUNT = cats.get(which).getBundleAmount();
                                            startActivityForResult(new Intent(ChatTypeActivity.this, PaymentActivity.class), 110);
                                            //Log.e("Pay", cats.get(which).getName());
                                            break;
                                        case 2:
                                            PaymentActivity.BUY_AMOUNT = cats.get(which).getBundleAmount();
                                            startActivityForResult(new Intent(ChatTypeActivity.this, PaymentActivity.class), 110);
                                            //Log.e("Pay", cats.get(which).getName());
                                            break;
                                    }
                                }
                            })
                            .create()
                            .show();
                }
            }else {
                utils.showNetworkError(ChatTypeActivity.this);
            }
        }
    }

}
