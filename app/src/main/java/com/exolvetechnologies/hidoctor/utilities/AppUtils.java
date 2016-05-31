package com.exolvetechnologies.hidoctor.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.exolvetechnologies.hidoctor.activities.LoginActivity;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Created by Exolve on 10/26/2014.
 */
public class AppUtils {

    public static final String TAG = "HiDoctor";
    public static final String API_ROOT = "https://hidoctor.com.ng";//http://fixcover.com/hid";

    static int vWidth;
    static int vHeight;
    Context context;
    public AppUtils(Context _context){
        context = _context;
    }

    public void showAlert(Context context, String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.create().show();
    }

    public void showNetworkError(Context context){
        showAlert(context, "Network Error", "Please check your network settings and try again");
    }

    public String getUserId(){
        SharedPreferences idPrefs = context.getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
        return idPrefs.getString(LoginActivity.USER_ID, "");
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if ((info != null)&&(info.isConnected()))
            {
                return true;
            }
        }
        return false;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int viewWidth(final View v){
        ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
        if(viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    int sWidth = 0;
                    v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    //int height = mainLayout.getHeight();
                    sWidth = v.getWidth();
                    vWidth = sWidth;

                }
            });
        }
        return vWidth;
    }

    public static int viewHeight(final View v){
        ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
        if(viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    int sHeight = 0;
                    v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    sHeight = v.getHeight();
                    vHeight = sHeight;

                }
            });
        }
        return vHeight;
    }

    public void setAutoCompleteAdapter(AutoCompleteTextView autoCompleteTextView, List<String> items){
        assert items != null;
        if(items.size() > 0){
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_dropdown_item_1line, items);
            autoCompleteTextView.setAdapter(adapter);
        }
    }

    public void setImageFromServer(String imageUrl, String fileName, int width,  int height,
                                  ImageView imageView, ProgressBar progressBar, boolean saveImage){
        new GetBitmap(imageUrl, fileName, width, height, imageView, progressBar, saveImage).execute();
    }

    public static void setTextViewTextLimit(TextView textView, CharSequence text, int limit){
        if (text.length() > limit){
            int maxLength = (limit);
            InputFilter[] fArray = new InputFilter[1];
            fArray[0] = new InputFilter.LengthFilter(maxLength);
            textView.setFilters(fArray);
            String mText = text.subSequence(0, (limit-3))+"...";
            textView.setText(mText);
        }else {
            textView.setText(text);
        }
    }

    public static String sendPostRequest(String mUrl, HashMap<String, String> params) throws IOException {
        InputStream in = null;
        HttpURLConnection conn = null;
        String charset = "UTF-8";
        StringBuilder sbParams;
        String parameters;
        sbParams = new StringBuilder();
        try{
            int i = 0;
            for (String key : params.keySet()) {
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), charset));

                i++;
            }
            parameters = sbParams.toString();
            //Log.i("OGIRS", parameters);
            byte[] postData = parameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;
            URL url = new URL(mUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput( true );
            conn.setFixedLengthStreamingMode(postDataLength);//conn.setChunkedStreamingMode(0); for unknown length
            conn.setReadTimeout(30000 /* milliseconds */);
            conn.setConnectTimeout(35000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//application/json
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));

            DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
            writer.write(postData);

            in = new BufferedInputStream(conn.getInputStream());
            return readStream(in);

        }finally {
            if (in != null) {
                in.close();
            }
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String getRequest(String myUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000 /* milliseconds */);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response code is: " + response);
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

    public static String readStream(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader bReader = new BufferedReader(reader);
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = bReader.readLine()) != null) {
            out.append(line);
        }
        Log.i(TAG, "HTTP RESPONSE"+ out.toString());
        return out.toString();
    }

     Bitmap downloadBitmap(String imageUrl) {
        InputStream is;
        Bitmap bitmap = null;

        try {
            if (isConnectedToInternet()) {
                URL url = new URL(imageUrl);
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
            }else {
                Toast.makeText(context, "Internet required", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }

    public String saveBitmap(String imageUrl, String fileName) throws IOException {
        String path = null;
        Bitmap bm;
        File chkfile = new File(context.getExternalCacheDir()+File.separator+"Images", fileName);
        if(!chkfile.exists()){
            bm = downloadBitmap(imageUrl);
            if (bm != null) {
                OutputStream outStream;
                File RootFile = new File(context.getExternalCacheDir() + File.separator + "Images");
                RootFile.mkdirs();
                File file = new File(context.getExternalCacheDir() + File.separator + "Images", fileName);
                try {
                    outStream = new FileOutputStream(file);
                    bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
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
        File chFile = new File(context.getExternalCacheDir()+File.separator+"Images",fileName);

        if(chFile.exists()){
            path = chFile.getPath();
        }
        return path;
    }

    private class GetBitmap extends AsyncTask<Void, Integer, Bitmap>{
        private String imageUrl;
        private String fileName;
        private int height;
        private int width;
        private ImageView imageView;
        private ProgressBar progressBar;
        private boolean saveImage;

        public GetBitmap(String imageUrl, String fileName, int width, int height,
                         ImageView imageView, ProgressBar progressBar, boolean saveImage) {
            this.imageUrl = imageUrl;
            this.fileName = fileName;
            this.height = height;
            this.width = width;
            this.imageView = imageView;
            this.progressBar = progressBar;
            this.saveImage = saveImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressBar != null){
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            String imagePath;
            Bitmap bitmap = null;
            if (saveImage){
                try {
                    imagePath = saveBitmap(imageUrl, fileName);
                    if (imagePath != null){
                        bitmap = decodeFile(imagePath, width, height);
                    }else {
                        bitmap = downloadBitmap(imageUrl);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (progressBar != null){
                progressBar.setVisibility(View.GONE);
            }
            if (bitmap != null){
                if (imageView != null){
                    if (imageView.getTag().equals(fileName)) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }else {
                Toast.makeText(context, "Error occurred with image download", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class EndChatAsync extends AsyncTask<Void, Void, Void> {
        long timeLeftMiliSecs;

        public EndChatAsync(long timeLeftMiliSecs) {
            this.timeLeftMiliSecs = timeLeftMiliSecs;
        }

        @Override
        protected Void doInBackground(Void... params) {
            //long secondsFromMiliSecs = TimeUnit.MILLISECONDS.toSeconds(timeLeftMiliSecs);
            String url = AppUtils.API_ROOT+"/api/endchat/"+ ChatsActivity.CHAT_ID
                    +"/"+String.valueOf(timeLeftMiliSecs);
            Log.i(TAG, "URL: "+url);
            try {
                getRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Toast.makeText(context, "Chat Ended", Toast.LENGTH_SHORT).show();
            //getActivity().finish();
        }
    }

    public void setMeasuredView(View view, double widthPercentage, double heightPercentage){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int rowPixelWidth = metrics.widthPixels;
        double imageWidth = ((double)rowPixelWidth / 100) * 90;
        double imageHeight = (imageWidth / 100) * 65;

        view.getLayoutParams().height = (int)imageHeight;
        view.getLayoutParams().width = (int)imageWidth;
    }
}
