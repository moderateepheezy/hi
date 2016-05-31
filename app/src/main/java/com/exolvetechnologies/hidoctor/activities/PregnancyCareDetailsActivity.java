package com.exolvetechnologies.hidoctor.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import java.io.File;
import java.util.HashMap;

public class PregnancyCareDetailsActivity extends AppCompatActivity {

    public static final String ID = "id";

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnancy_care_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final String id = intent.getStringExtra(ID);

        AppDataManager dataManager = new AppDataManager(this);
        final AppUtils utils = new AppUtils(this);
        HashMap<String, String> care = null;
        try {
            care = dataManager.getPregnancyCare(id);
        }catch (Exception e){
            e.printStackTrace();
        }

        TextView title = (TextView) findViewById(R.id.blogTitle);
        //TextView author = (TextView) findViewById(R.id.blogAuthor);
        //TextView date = (TextView) findViewById(R.id.blogDate);
        TextView content = (TextView) findViewById(R.id.blogContent);
        ImageView image = (ImageView) findViewById(R.id.blogImage);
        ProgressBar imageProgress = (ProgressBar) findViewById(R.id.imageDownloadProgress);


        if (care != null) {
            String mId = care.get("id");
            String titleStr = care.get("week");
            String contentStr = care.get("content");
            String imageUrl = care.get("image_url");

            String fileName = "care_" + mId;

            title.setText(Html.fromHtml("Week "+titleStr));
            //author.setText(Html.fromHtml(authorStr));
            //date.setText(Html.fromHtml(dateStr));
            content.setText(Html.fromHtml(contentStr));

            String filePath = getExternalCacheDir() + File.separator + "Images/" + fileName;
            Bitmap bitmap = AppUtils.decodeFile(filePath, 400, 280);

            if (bitmap != null) {
                image.setImageBitmap(bitmap);
            } else {
                image.setTag(fileName);
                utils.setImageFromServer(imageUrl, fileName, 400, 280, image, imageProgress, true);
            }
        }
    }

}
