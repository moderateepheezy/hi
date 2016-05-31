package com.exolvetechnologies.hidoctor.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class BlogDetailsActivity extends AppCompatActivity {

    public static final String ID = "id";
    private ProgressBar progressBarFetchComments;
    private LinearLayout commentsLayout;
    private JSONObject commentsJson;
    private LayoutInflater inflater;
    private EditText editTextComment;

    private String url;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final String id = intent.getStringExtra(ID);

        AppDataManager dataManager = new AppDataManager(this);
        final AppUtils utils = new AppUtils(this);
        HashMap<String, String> blog = null;
        try {
            blog = dataManager.getBlog(id);
        }catch (Exception e){
            e.printStackTrace();
        }

        TextView title = (TextView) findViewById(R.id.blogTitle);
        TextView author = (TextView) findViewById(R.id.blogAuthor);
        TextView date = (TextView) findViewById(R.id.blogDate);
        TextView content = (TextView) findViewById(R.id.blogContent);
        ImageView image = (ImageView) findViewById(R.id.blogImage);
        ProgressBar imageProgress = (ProgressBar) findViewById(R.id.imageDownloadProgress);
        commentsLayout = (LinearLayout) findViewById(R.id.commentsLayout);
        progressBarFetchComments = (ProgressBar) findViewById(R.id.fetchCommentsProgress);
        editTextComment = (EditText) findViewById(R.id.editTextComment);

        if (blog != null){
            String mId = blog.get("id");
            String titleStr = blog.get("title");
            String authorStr = blog.get("author");
            String dateStr = blog.get("date");
            String contentStr = blog.get("content");
            String imageUrl = blog.get("image_url");
            url = blog.get("url");

            String fileName = "blog_"+mId;

            title.setText(Html.fromHtml(titleStr));
            author.setText(Html.fromHtml(authorStr));
            date.setText(Html.fromHtml(dateStr));
            content.setText(Html.fromHtml(contentStr));

            String filePath = getExternalCacheDir()+ File.separator+"Images/"+fileName;
            Bitmap bitmap = AppUtils.decodeFile(filePath, 400, 280);

            if (bitmap != null) {
                image.setImageBitmap(bitmap);
            }else {
                image.setTag(fileName);
                utils.setImageFromServer(imageUrl, fileName, 400, 280, image, imageProgress, true);
            }
            inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            if (commentsJson != null){
                try {
                    adaptComments(commentsJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                if (utils.isConnectedToInternet()){
                    new GetBlogCommentsAsync(id).execute();
                }
            }
        }

        findViewById(R.id.btnSubmitComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editTextComment.getText().toString();
                if (comment.length() < 5){
                    editTextComment.setError("Please enter reasonable comment to continue");
                }else {
                    if (utils.isConnectedToInternet()){
                        new PostCommentAsync(id, utils.getUserId(), comment).execute();
                    }else {
                        utils.showNetworkError(BlogDetailsActivity.this);
                    }
                }
            }
        });
        findViewById(R.id.shareButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check this HiDoctor blog post. "+url);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Check this HiDoctor blog post.");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share via"));
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private class GetBlogCommentsAsync extends AsyncTask<Void, Integer, String>{
        String blogId;

        public GetBlogCommentsAsync(String blogId) {
            this.blogId = blogId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarFetchComments.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return AppUtils.getRequest(AppUtils.API_ROOT+"/api/blogcomments/"+blogId);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBarFetchComments.setVisibility(View.GONE);
            if (s != null){
                try {
                    commentsJson = new JSONObject("{data:"+s+"}");
                    adaptComments(commentsJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void adaptComments(JSONObject jsonObject) throws JSONException{
        editTextComment.setText("");
        JSONArray comments = jsonObject.getJSONArray("data");
        if (comments.length() > 0){
            commentsLayout.removeAllViews();
        }
        for (int i = 0; i < comments.length(); i ++){
            JSONObject comment = comments.getJSONObject(i);
            String readerStr = comment.getString("comment_username");
            String date = comment.getString("comment_created");
            String commentBody = comment.getString("comment_body");

            CardView cardLayout = new CardView(this);
            View cardView = inflater.inflate(R.layout.model_blog_comment, cardLayout);
            TextView reader = (TextView) cardView.findViewById(R.id.blogReader);
            TextView commentDate = (TextView) cardView.findViewById(R.id.commentDate);
            TextView commentTextView = (TextView) cardView.findViewById(R.id.blogComment);
            reader.setText(readerStr);
            commentDate.setText(date);
            commentTextView.setText(commentBody);

            commentsLayout.addView(cardView);
        }
    }

    private class PostCommentAsync extends AsyncTask<Void, Integer, String>{
        String blogId;
        String userId;
        String comment;

        public PostCommentAsync(String blogId, String userId, String comment) {
            this.blogId = blogId;
            this.userId = userId;
            this.comment = comment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarFetchComments.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put("blog_id", blogId);
            map.put("user_id", userId);
            map.put("comment", comment);
            try {
                return AppUtils.sendPostRequest(AppUtils.API_ROOT+"/api/insertblogcomment", map);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBarFetchComments.setVisibility(View.GONE);
            if (s != null){
                try {
                    commentsJson = new JSONObject("{data:"+s+"}");
                    adaptComments(commentsJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
