package com.exolvetechnologies.hidoctor.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.fragments.VideoSliderFragment;
import com.exolvetechnologies.hidoctor.models.PagerClickListener;
import com.exolvetechnologies.hidoctor.services.RegistrationIntentService;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;
import com.exolvetechnologies.hidoctor.utilities.QuickstartPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener, PagerClickListener {

    public static String[] vTitles = {"Introduction to HiDoctor", "How to avoid heart attacks", "Improve your vision"};

    private VideoView mVideoView;
    private int position = 0;
    private ProgressDialog progressDialog;
    private MediaController mediaControls;

    private ViewPager mPager;
    private ImageView adImage;
    private PagerAdapter mPagerAdapter;
    private ListView mGridView;
    private HiBlog[] mBlogs;
    private CountDownTimer timer;

    private AppDataManager dataManager;
    private AppUtils utils;
    private ProgressBar progressBar;
    private List<HashMap<String, String>> blogs;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "HiDoctor";

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.getBlogProgress);
        adImage = (ImageView) findViewById(R.id.adImage);


        adImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://hidoctor.com.ng"));
                startActivity(intent);
            }
        });

        if (mediaControls == null) {
            mediaControls = new MediaController(MainActivity.this);
        }

        //initialize the VideoView
        mVideoView = (VideoView) findViewById(R.id.video_view);

        dataManager = new AppDataManager(this);
        utils = new AppUtils(this);
        VideoSliderFragment.pagerListener = this;

        try {
            //set the media controller in the VideoView
            mVideoView.setMediaController(mediaControls);

            //set the uri of the video to be played
            mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample_video)); //https://fixcover.com/hid/3d.mp4
            //mVideoView.setVideoURI(Uri.parse("https://fixcover.com/hid/3d.mp4"));
            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mVideoView.setVisibility(View.GONE);
                    mPager.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        mVideoView.requestFocus();


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    /*mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));*/
                }
            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);

        TextView userName = (TextView)headerLayout.findViewById(R.id.userName);
        TextView userEmail = (TextView)headerLayout.findViewById(R.id.userEmail);

        SharedPreferences loginPrefs = getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
        String name = loginPrefs.getString(LoginActivity.FIRST_NAME, "")+" "+loginPrefs.getString(LoginActivity.LAST_NAME, "");
        String email = loginPrefs.getString(LoginActivity.EMAIL, "");

        userName.setText(name);
        userEmail.setText(email);

        mPager = (ViewPager) findViewById(R.id.videoSlider);
        mPagerAdapter = new VideoSliderAdapter(getSupportFragmentManager());
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setAdapter(mPagerAdapter);

        try{
            blogs = dataManager.getBlogs();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (blogs != null && (blogs.size() > 0)){
            String id = blogs.get(0).get("id");
            String title = blogs.get(0).get("title");
            String excerpt = blogs.get(0).get("excerpt");
            String author = blogs.get(0).get("author");
            String date = blogs.get(0).get("date");

            mBlogs = new HiBlog[]{
                    new HiBlog(title, excerpt, author, date, BlogDetailsActivity.class, id)
            };

            mGridView = (ListView) findViewById(android.R.id.list);
            mGridView.setAdapter(new SampleAdapter());
            mGridView.setOnItemClickListener(this);

            if (utils.isConnectedToInternet()) {
                new GetBlogsAsync(false).execute();
            }
        }else {
            if (utils.isConnectedToInternet()) {
                new GetBlogsAsync(true).execute();
            }else {
                utils.showNetworkError(this);
            }
        }

        timer = new CountDownTimer(7000, 3000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                int curPos = mPager.getCurrentItem();
                int count = mPager.getChildCount();
                if (curPos < count){
                    mPager.setCurrentItem(curPos + 1, true);
                }else {
                    mPager.setCurrentItem(0, true);
                }
                //Toast.makeText(MainActivity.this, "Slide", Toast.LENGTH_SHORT).show();
                timer.start();
            }
        };
        timer.start();

        //List<HashMap<String, String>> bUrls = dataManager.getBlogImageUrls();

        //if (bUrls != null) {
        //new DownloadFileAsync(bUrls).execute();
        //}
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
        if (timer != null){
            timer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null){
            timer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null){
            timer.start();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if (timer != null){
                timer.cancel();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_chat) {
            startActivity(new Intent(this, ChatCategoryActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> container, View view, int position, long id) {
        startActivity(mBlogs[position].intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bmi) {
            startActivity(new Intent(this, BMIActivity.class));
        } else if (id == R.id.nav_pregnancy) {
            startActivity(new Intent(MainActivity.this, MyBabyAndIActivity.class));
        } else if (id == R.id.nav_find_partner) {

        } else if (id == R.id.nav_invite_friend) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject));
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message)+" "+getString(R.string.gplay_web_url));
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Invite via"));

        } else if (id == R.id.nav_faqs) {
            startActivity(new Intent(MainActivity.this, FAQsActivity.class));
        }  else if (id == R.id.nav_symptom_checker) {
            startActivity(new Intent(MainActivity.this, SymptomCheckerActivity.class));
        } else if (id == R.id.nav_feedback) {

        } else if (id == R.id.nav_ovulation) {

        } else if (id == R.id.nav_chat) {
            startActivity(new Intent(MainActivity.this, ChatCategoryActivity.class));
        } else if (id == R.id.nav_blog) {
            startActivity(new Intent(MainActivity.this, BlogListActivity.class));
        }else if (id == R.id.nav_profile) {

        }else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Please confirm you want to logout")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = getSharedPreferences(LoginActivity.LOGIN, 0).edit();
                            editor.putBoolean(LoginActivity.STATUS, false).apply();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create().show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void pagerItemClick() {
        mVideoView.setVisibility(View.VISIBLE);
        mPager.setVisibility(View.GONE);
        mVideoView.start();
        //Toast.makeText(MainActivity.this, "View at "+mPager.getCurrentItem()+" clicked", Toast.LENGTH_SHORT).show();
    }

    private class VideoSliderAdapter extends FragmentStatePagerAdapter {

        public VideoSliderAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return VideoSliderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return vTitles.length;
        }
    }

    private class SampleAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mBlogs.length;
        }

        @Override
        public Object getItem(int position) {
            return mBlogs[position];
        }

        @Override
        public long getItemId(int position) {
            return mBlogs[position].hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.model_blog_preview,
                        container, false);
            }

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(Html.fromHtml(
                    mBlogs[position].titleResId));
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(Html.fromHtml(
                    mBlogs[position].excerptResId));
            ((TextView) convertView.findViewById(R.id.textViewAuthor)).setText(Html.fromHtml(
                    mBlogs[position].authorResId));
            ((TextView) convertView.findViewById(R.id.textViewDate)).setText(Html.fromHtml(
                    mBlogs[position].dateResId));
            return convertView;
        }
    }

    private class HiBlog {
        String titleResId;
        String excerptResId;
        String authorResId;
        String dateResId;
        Intent intent;
        String id;

        private HiBlog(String titleResId, String excerptResId, String authorResId, String dateResId, Intent intent, String id) {
            this.intent = intent;
            this.titleResId = titleResId;
            this.excerptResId = excerptResId;
            this.authorResId = authorResId;
            this.dateResId = dateResId;
            this.id = id;
        }

        private HiBlog(String titleResId, String excerptResId, String authorResId, String dateResId,
                       Class<? extends Activity> activityClass, String id) {
            this(titleResId, excerptResId, authorResId, dateResId,
                    new Intent(MainActivity.this, activityClass).putExtra(BlogDetailsActivity.ID, id), id);
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private class GetBlogsAsync extends AsyncTask<Void, Integer, String> {
        boolean showProgress;

        public GetBlogsAsync(boolean showProgress) {
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
            progressBar.setVisibility(View.GONE);
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


                        blogs = dataManager.getBlogs();

                        if (blogs != null && (blogs.size() > 0)){
                            /*adapter = new BlogListAdapter(blogs, BlogListActivity.this);
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(BlogListActivity.this);*/

                            String mId = blogs.get(0).get("id");
                            String mTitle = blogs.get(0).get("title");
                            String mExcerpt = blogs.get(0).get("excerpt");
                            String mAuthor = blogs.get(0).get("author");
                            String mDate = blogs.get(0).get("date");

                            mBlogs = new HiBlog[]{
                                    new HiBlog(mTitle, mExcerpt, mAuthor, mDate, BlogDetailsActivity.class, mId)
                            };

                            mGridView = (ListView) findViewById(android.R.id.list);
                            mGridView.setAdapter(new SampleAdapter());
                            mGridView.setOnItemClickListener(MainActivity.this);
                        }

                        //List<HashMap<String, String>> bUrls = dataManager.getBlogImageUrls();
                        //new DownloadFileAsync(bUrls).execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
