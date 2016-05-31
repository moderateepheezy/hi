package com.exolvetechnologies.hidoctor.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

public class PaymentActivity extends AppCompatActivity {

    String userAgent = "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/"+android.os.Build.ID
            +") AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36";
    public static String BUY_AMOUNT;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        SharedPreferences preferences = getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
        String userId = preferences.getString(LoginActivity.USER_ID, "0");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        WebView webView = (WebView)findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.getSettings().setUserAgentString(userAgent);

        webView.setWebViewClient(new WebViewClient() {
            // load url
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            // when finish loading page
            public void onPageFinished(WebView view, String url) {

            }
        });

        //Log.i("HTTP_URL", "http://www.exolvetechnologies.com/hid/payment/paywithcashenvoy/"+ BUY_AMOUNT + "/" + User.first(User.class).getUserID());
        webView.loadUrl(AppUtils.API_ROOT+"/payment/paywithcashenvoy/"+ BUY_AMOUNT + "/" + userId);
    }

    class WebAppInterface {
        Context mContext;

        /* Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void transactionCompleted() {
            Toast.makeText(mContext, "Transaction Complete", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}
