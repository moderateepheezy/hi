package com.exolvetechnologies.hidoctor.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import java.io.IOException;

public class TextChatActivity extends AppCompatActivity {

    private AppUtils utils;
    private ProgressBar progress;

    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE=1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        utils = new AppUtils(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        SharedPreferences accPrefs = getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
        String id = accPrefs.getString(LoginActivity.USER_ID, "");

        String chatUrl = "https://hidoctor.com.ng/chat/text/"+ ChatsActivity.CHAT_ID+"?userid="+id+"&mobile=yes";
        String userAgent = "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/"+android.os.Build.ID
                +") AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36";
        progress = (ProgressBar) findViewById(R.id.web_progress);
        progress.setVisibility(View.VISIBLE);

        if (utils.isConnectedToInternet()) {
           WebView mWebView = (WebView) findViewById(R.id.webView1);
            mWebView.getSettings().setUserAgentString(userAgent);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setAllowFileAccess(true);
            mWebView.getSettings().setAllowContentAccess(true);

            mWebView.setWebChromeClient(new WebChromeClient()
            {
                //The undocumented magic method override
                //Eclipse will swear at you if you try to put @Override here
                // For Android 3.0+
                public void openFileChooser(ValueCallback<Uri> uploadMsg) {

                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    TextChatActivity.this.startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);

                }

                // For Android 3.0+
                public void openFileChooser( ValueCallback uploadMsg, String acceptType ) {
                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    TextChatActivity.this.startActivityForResult(
                            Intent.createChooser(i, "File Browser"),
                            FILECHOOSER_RESULTCODE);
                }

                //For Android 4.1
                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    TextChatActivity.this.startActivityForResult( Intent.createChooser( i, "File Chooser" ), TextChatActivity.FILECHOOSER_RESULTCODE );

                }

            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.setWebContentsDebuggingEnabled(true);
            }

            mWebView.setWebViewClient(new HidWebClient());

            mWebView.loadUrl(chatUrl);
            new NotifyDoctorAsync().execute();
        }else {
            utils.showNetworkError(TextChatActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if(requestCode==FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage) return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    public class HidWebClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            progress.setVisibility(View.GONE);
        }
    }

    //flipscreen not loading again
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    public class NotifyDoctorAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences idPrefs = getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
            String userId = idPrefs.getString(LoginActivity.USER_ID, "");
            String url = AppUtils.API_ROOT+"/api/notifydoctor/"+userId+"/"
                    +ChatsActivity.CHAT_CATEGORY_ID+"/"+ ChatsActivity.SESSION_ID+"/"
                    +ChatsActivity.CHAT_TYPE+"/"+ChatsActivity.CHAT_ID;
            //Log.i(DEBUG_TAG, "URL: "+url);
            try {
                AppUtils.getRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
