package com.exolvetechnologies.hidoctor.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.fragments.TabbedBlogListFragment;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;
import com.exolvetechnologies.hidoctor.utilities.BlogLoadListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class BlogListActivity extends AppCompatActivity {

    public static final String TAG = "HiDoctor";

    private AppDataManager dataManager;
    private AppUtils utils;
    /*private AbsListView listView;
    private ProgressBar progressBar;
    private BlogListAdapter adapter;
    private List<HashMap<String, String>> blogs;*/
    public static BlogLoadListener loadListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dataManager = new AppDataManager(this);
        utils = new AppUtils(this);
        /*try{
            blogs = dataManager.getBlogs();
        }catch (Exception e){
            e.printStackTrace();
        }*/

        /*if (blogs != null && (blogs.size() > 0)){
            //adapter = new BlogListAdapter(blogs, this);
            //listView.setAdapter(adapter);
            //listView.setOnItemClickListener(this);
            //if (utils.isConnectedToInternet()) {
                //List<HashMap<String, String>> cats = dataManager.getUndownloadedCats();
                //new DownloadFileAsync(cats).execute();
            //}
            if (utils.isConnectedToInternet()) {
                new GetBlogsAsync(false).execute();
            }
        }else {*/
            if (utils.isConnectedToInternet()) {
                new GetBlogsAsync(false).execute();
            }else {
                utils.showNetworkError(this);
            }
        //}

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            TabbedBlogListFragment fragment = new TabbedBlogListFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    /*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(BlogListActivity.this, BlogDetailsActivity.class)
                .putExtra(BlogDetailsActivity.ID, blogs.get(position).get("id")));
    }*/

    private class GetBlogsAsync extends AsyncTask<Void, Integer, String> {
        boolean showProgress;

        public GetBlogsAsync(boolean showProgress) {
            this.showProgress = showProgress;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showProgress) {
                //progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return AppUtils.getRequest(AppUtils.API_ROOT + "/api/blog");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //progressBar.setVisibility(View.GONE);
            if (s != null){
                try {
                    JSONObject object = new JSONObject("{data:"+s+"}");
                    JSONArray array = object.getJSONArray("data");
                    for (int i = 0; i < array.length(); i ++){
                        JSONObject cat = array.getJSONObject(i);
                        String id = cat.getString("blog_id");
                        String title = cat.getString("title");
                        String excerpt = cat.getString("excerpt");
                        String imageUrl = cat.getString("image");
                        String content = cat.getString("content");
                        String author = cat.getString("author");
                        String date = cat.getString("date");
                        String url = cat.getString("url");
                        String catId = cat.getString("category_id");

                        dataManager.insertBlog(id, title, imageUrl, excerpt, content, author, date, url, catId);

                        loadListener.onLoadComplete();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
