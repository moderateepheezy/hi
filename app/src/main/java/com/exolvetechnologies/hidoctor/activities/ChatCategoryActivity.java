package com.exolvetechnologies.hidoctor.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.adapters.CategoryAdapter;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class ChatCategoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

            public static final String TAG = "HiDoctor";

            private AppDataManager dataManager;
            private AppUtils utils;
            private AbsListView listView;
            private ProgressBar progressBar;
            private CategoryAdapter adapter;
            private List<HashMap<String, String>> categories;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_chat_category);
                //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                //setSupportActionBar(toolbar);

                listView = (AbsListView) findViewById(R.id.categoriesList);
                progressBar = (ProgressBar) findViewById(R.id.getCatProgress);

                dataManager = new AppDataManager(this);
                utils = new AppUtils(this);
                try{
                    categories = dataManager.getCategories();
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (categories != null && (categories.size() > 0)){
                    adapter = new CategoryAdapter(categories, this);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(this);
            if (utils.isConnectedToInternet()) {
                /*List<HashMap<String, String>> cats = dataManager.getUndownloadedCats();
                new DownloadFileAsync(cats).execute();*/
            }
                }else {
                    if (utils.isConnectedToInternet()) {
                        new GetCategoriesAsync().execute();
                    }else {
                        utils.showNetworkError(this);
                    }
                }

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatsActivity.CHAT_CATEGORY_ID = categories.get(position).get("id");
                startActivity(new Intent(ChatCategoryActivity.this, ChatTypeActivity.class));
            }

            private class GetCategoriesAsync extends AsyncTask<Void, Integer, String> {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                protected String doInBackground(Void... params) {
                    try {
                        return getCategories(AppUtils.API_ROOT+"/api/doctorcategory");
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
                                String id = cat.getString("category_id");
                                String title = cat.getString("category_title");
                                String desc = cat.getString("category_description");
                                String url = cat.getString("category_image");

                                dataManager.insertCategory(id, title, url, desc, "0");

                                /*List<HashMap<String, String>> rCats = dataManager.getUndownloadedCats();
                                if (rCats != null) {
                                    new DownloadFileAsync(rCats).execute();
                                }*/
                            }
                            categories = dataManager.getCategories();

                            if (categories != null && (categories.size() > 0)){
                                adapter = new CategoryAdapter(categories, ChatCategoryActivity.this);
                                listView.setAdapter(adapter);
                                listView.setOnItemClickListener(ChatCategoryActivity.this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            public class DownloadFileAsync extends AsyncTask<Void, Integer, Void>{

                private List<HashMap<String, String>> rCats;

                public DownloadFileAsync(List<HashMap<String, String>> rCats) {
                    this.rCats = rCats;
                }

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        downloadCatImage(rCats);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    categories = dataManager.getCategories();

                    if (categories != null && (categories.size() > 0)){
                        adapter = new CategoryAdapter(categories, ChatCategoryActivity.this);
                        if (listView != null) {
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(ChatCategoryActivity.this);
                        }
                    }
                }
            }

        private String getCategories(String myUrl) throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(myUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(20000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                return readStream(is);

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readStream(InputStream stream) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bReader = new BufferedReader(reader);
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = bReader.readLine()) != null) {
                out.append(line);
            }
            Log.i(TAG, "HTTP RESPONSE " + out.toString());
            return out.toString();
        }

        public void downloadCatImage(List<HashMap<String, String>> uCat) throws IOException {
            String fileURL;
            String fileName;
            String adId;
            String path = null;
            for (int i = 0; i < uCat.size(); i ++){
                fileURL = uCat.get(i).get("image_url");
                adId = uCat.get(i).get("id");
                fileName = "cat_"+adId;
                Bitmap bm;
                File chkfile = new File(getExternalCacheDir()+File.separator+"Images", fileName);
                if(!chkfile.exists()){
                    bm = downloadBitmap(fileURL);
                    if (bm != null) {
                        OutputStream outStream;
                        File RootFile = new File(getExternalCacheDir() + File.separator + "Images");
                        RootFile.mkdirs();
                        File file = new File(getExternalCacheDir() + File.separator + "Images", fileName);
                        try {
                            outStream = new FileOutputStream(file);
                            if (bm != null) {
                                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                            }
                            outStream.flush();
                            outStream.close();

                        } catch (FileNotFoundException e) {
                            Log.e("FILE NOT FOUND", e.getLocalizedMessage());
                            e.printStackTrace();
                        } catch (IOException e) {
                            Log.e("IO ERROR", e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                }
                File chFile = new File(getExternalCacheDir()+File.separator+"Images",fileName);

                if(chFile.exists()){
                    path = chFile.getPath();
                    dataManager.updateAdDownloadStatus(adId);
                    Log.i(TAG, "AFTER UPDATE: " + path);
                }
            }
        }

        static Bitmap downloadBitmap(String myUrl) {
            InputStream is;
            Bitmap bitmap = null;

            try {
                URL url = new URL(myUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //conn.setReadTimeout(10000 /* milliseconds */);
                //conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                is = conn.getInputStream();

                bitmap = BitmapFactory.decodeStream(is);
                // Makes sure that the InputStream is closed after the app is
            }catch(Exception e){
                e.printStackTrace();
            }
            return bitmap;
        }
    }

