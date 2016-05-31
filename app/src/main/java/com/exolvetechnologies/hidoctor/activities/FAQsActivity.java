package com.exolvetechnologies.hidoctor.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class FAQsActivity extends AppCompatActivity {

    private LinearLayout faqsLayout;
    private ProgressBar faqProgress;
    private AppDataManager dataManager;
    private AppUtils utils;
    private List<HashMap<String, String>> faqs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dataManager = new AppDataManager(this);
        utils = new AppUtils(this);

        faqsLayout = (LinearLayout) findViewById(R.id.faqLayout);
        faqProgress = (ProgressBar) findViewById(R.id.faqProgress);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        faqs = dataManager.getFAQs();
        if (faqs != null && (faqs.size() > 0)){
            faqsLayout.removeAllViews();
            setFAQAdapter(faqs);
            if (utils.isConnectedToInternet()){
                new GetFAQsAsync(false).execute();
            }

        }else {
            if (utils.isConnectedToInternet()){
                new GetFAQsAsync(true).execute();
            }else {
                utils.showNetworkError(this);
            }
        }
    }

    private void setFAQAdapter(List<HashMap<String, String>> faqs){
        LayoutInflater inflater = (LayoutInflater) getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < faqs.size(); i ++) {

            FrameLayout holder = new FrameLayout(this);
            View layout = inflater.inflate(R.layout.model_faq, holder, false);

            TextView question = (TextView) layout.findViewById(android.R.id.text1);
            final TextView answer = (TextView) layout.findViewById(android.R.id.text2);

            final String answerStr = faqs.get(i).get("answer");

            question.setText(Html.fromHtml(faqs.get(i).get("question")));
            question.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answer.setText(Html.fromHtml(answerStr));
                    if (answer.getVisibility() == View.VISIBLE) {
                        answer.setVisibility(View.GONE);
                    } else {
                        answer.setVisibility(View.VISIBLE);
                    }
                }
            });
            faqsLayout.addView(layout);
        }
    }

    private class GetFAQsAsync extends AsyncTask<Void,  Integer, String>{
        boolean showProgress;

        public GetFAQsAsync(boolean showProgress) {
            this.showProgress = showProgress;
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = AppUtils.API_ROOT+"/api/faq";
            try {
                return AppUtils.getRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showProgress){
                faqProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            faqProgress.setVisibility(View.GONE);
            if (s != null){
                try {
                    JSONObject object = new JSONObject("{data:"+s+"}");
                    JSONArray array = object.getJSONArray("data");
                    for (int i = 0; i < array.length(); i ++){
                        JSONObject cat = array.getJSONObject(i);
                        String id = cat.getString("id");
                        String question = cat.getString("question");
                        String answer = cat.getString("answer");

                        dataManager.insertFaq(id, question, answer);

                    }

                    faqs = dataManager.getFAQs();
                    if (faqs != null && (faqs.size() > 0)){
                        faqsLayout.removeAllViews();
                        setFAQAdapter(faqs);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
