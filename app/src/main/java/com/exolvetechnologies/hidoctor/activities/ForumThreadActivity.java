package com.exolvetechnologies.hidoctor.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.adapters.ThreadAdapter;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ForumThreadActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "HiDoctor";
    public static final String FORUM_ID = "forumId";

    private AppDataManager dataManager;
    private AppUtils utils;
    private AbsListView listView;
    private ProgressBar progressBar;
    private ThreadAdapter adapter;
    private List<HashMap<String, String>> threads;
    private String forumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_thread);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        forumId = intent.getStringExtra(FORUM_ID);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (AbsListView) findViewById(R.id.forumThreads);
        progressBar = (ProgressBar) findViewById(R.id.getThreadsProgress);

        dataManager = new AppDataManager(this);
        utils = new AppUtils(this);
        try{
            threads = dataManager.getThreads(forumId);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (threads != null && (threads.size() > 0)){
            adapter = new ThreadAdapter(threads, this);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

            if (utils.isConnectedToInternet()) {
                new GetCategoriesAsync(false).execute();
            }

        }else {
            if (utils.isConnectedToInternet()) {
                new GetCategoriesAsync(true).execute();
            }else {
                utils.showNetworkError(this);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(ForumThreadActivity.this, ForumDetailsActivity.class)
                .putExtra(ForumDetailsActivity.ID, threads.get(position).get("id")));
    }

    private class GetCategoriesAsync extends AsyncTask<Void, Integer, String> {

        boolean showProgress;

        public GetCategoriesAsync(boolean showProgress) {
            this.showProgress = showProgress;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showProgress) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = AppUtils.API_ROOT+"/api/thread/"+forumId;
            try {
                return AppUtils.getRequest(url);
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
                try {
                    JSONObject object = new JSONObject("{data:"+s+"}");
                    JSONArray array = object.getJSONArray("data");
                    for (int i = 0; i < array.length(); i ++){
                        JSONObject cat = array.getJSONObject(i);
                        String id = cat.getString("thread_id");
                        String title = cat.getString("title");
                        String desc = cat.getString("description");
                        /*String url = cat.getString("image");*/

                        dataManager.insertThread(id, title, /*url,*/ desc, forumId);

                    }

                    threads = dataManager.getThreads(forumId);
                    if (threads != null && (threads.size() > 0)){
                        adapter = new ThreadAdapter(threads, ForumThreadActivity.this);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(ForumThreadActivity.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
