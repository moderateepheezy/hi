package com.exolvetechnologies.hidoctor.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ekeretepeter on 11/12/15.
 */
public class AppDataManager extends SQLiteOpenHelper {

    //DB Version
    private static final int DATABASE_VERSION = 2;
    //DB Name
    private static final String DATABASE_NAME = "appData";
    //Tables
    private static final String TABLE_NAME_CATEGORIES = "categories";
    private static final String TABLE_NAME_BLOG = "blog";
    private static final String TABLE_NAME_FORUM = "forum";
    private static final String TABLE_NAME_THREAD = "thread";
    private static final String TABLE_NAME_FAQ = "faq";
    private static final String TABLE_NAME_PREGNANCY_CARE = "pregnancyCare";
    //Share Properties
    private static final String ID = "Id";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String IMAGE_URL = "image";
    private static final String EXCERPT = "excerpt";
    private static final String CONTENT = "content";
    //Table chat categories
    private static final String CAT_IMAGE_URL = "imageUrl";
    private static final String DOWNLOAD_STATUS = "downloadStatus";
    //Table Blog
    private static final String CAT_ID = "catId";
    private static final String AUTHOR = "author";
    private static final String URL = "url";
    private static final String DATE = "date";
    //Table Forum
    private static final String FORUM_ID = "forumId";
    private static final String PRIVACY = "privacy";
    //Table Thread
    private static final String THREAD_ID = "threadId";
    //Table FAQ
    private static final String QUESTION = "question";
    private static final String ANSWER = "answer";
    //Table Pregnancy Care
    private static final String WEEK = "week";



    private static final String CATEGORY_TABLE_CREATE =  "CREATE TABLE " + TABLE_NAME_CATEGORIES + " (" + ID +
            " TEXT PRIMARY KEY, "+ DESCRIPTION + " TEXT, "+ TITLE + " TEXT, "+ DOWNLOAD_STATUS +
            " TEXT, "+ CAT_IMAGE_URL +" TEXT );";

    private static final String BLOG_TABLE_CREATE =  "CREATE TABLE " + TABLE_NAME_BLOG + " (" + ID +
            " TEXT PRIMARY KEY, "+ EXCERPT + " TEXT, "+ TITLE + " TEXT, "+ AUTHOR + " TEXT, "+ DATE +
            " TEXT, "+ CONTENT + " TEXT, "+ URL + " TEXT, "+ CAT_ID + " TEXT, "+ IMAGE_URL +" TEXT );";

    private static final String FORUM_TABLE_CREATE =  "CREATE TABLE " + TABLE_NAME_FORUM + " (" + FORUM_ID +
            " TEXT PRIMARY KEY, "+ DESCRIPTION + " TEXT, "+ TITLE + " TEXT, "+ PRIVACY +" TEXT );";

    private static final String THREAD_TABLE_CREATE =  "CREATE TABLE " + TABLE_NAME_THREAD + " (" + THREAD_ID +
            " TEXT PRIMARY KEY, "+ DESCRIPTION + " TEXT, "+ TITLE + " TEXT, "+ FORUM_ID +
            " TEXT, "+ IMAGE_URL +" TEXT );";
    private static final String FAQ_TABLE_CREATE =  "CREATE TABLE " + TABLE_NAME_FAQ + " (" + ID +
            " TEXT PRIMARY KEY, "+ QUESTION + " TEXT, "+ ANSWER +" TEXT );";

    private static final String PREGNANCY_CARE_TABLE_CREATE =  "CREATE TABLE " + TABLE_NAME_PREGNANCY_CARE + " (" + ID +
            " TEXT PRIMARY KEY, "+ EXCERPT + " TEXT, "+ WEEK + " TEXT, "+ CONTENT + " TEXT, "+ IMAGE_URL +" TEXT );";

    Context context;


    public AppDataManager(Context context){
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CATEGORY_TABLE_CREATE);
        db.execSQL(BLOG_TABLE_CREATE);
        db.execSQL(FORUM_TABLE_CREATE);
        db.execSQL(THREAD_TABLE_CREATE);
        db.execSQL(FAQ_TABLE_CREATE);
        db.execSQL(PREGNANCY_CARE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BLOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FORUM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_THREAD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FAQ);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PREGNANCY_CARE);
        onCreate(db);
    }

    public void insertCategory(String id, String title, String url, String description, String downloadStatus){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, id);
        values.put(TITLE, title);
        values.put(CAT_IMAGE_URL, url);
        values.put(DESCRIPTION, description);
        values.put(DOWNLOAD_STATUS, downloadStatus);
        db.insert(TABLE_NAME_CATEGORIES, null, values);
        db.close();
    }

    public void insertFaq(String id, String question, String answer){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, id);
        values.put(QUESTION, question);
        values.put(ANSWER, answer);
        db.insert(TABLE_NAME_FAQ, null, values);
        db.close();
    }

    public void insertBlog(String id, String title, String imageUrl, String excerpt, String content,
                           String author, String date, String url, String catId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, id);
        values.put(TITLE, title);
        values.put(IMAGE_URL, imageUrl);
        values.put(EXCERPT, excerpt);
        values.put(CONTENT, content);
        values.put(AUTHOR, author);
        values.put(DATE, date);
        values.put(URL, url);
        values.put(CAT_ID, catId);
        db.insert(TABLE_NAME_BLOG, null, values);
        db.close();
    }

    public void insertPregnancyCare(String id, String week, String imageUrl, String excerpt, String content){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, id);
        values.put(WEEK, week);
        values.put(IMAGE_URL, imageUrl);
        values.put(EXCERPT, excerpt);
        values.put(CONTENT, content);
        db.insert(TABLE_NAME_PREGNANCY_CARE, null, values);
        db.close();
    }

    public void insertForum(String forumId, String title, String privacy, String description){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FORUM_ID, forumId);
        values.put(TITLE, title);
        values.put(PRIVACY, privacy);
        values.put(DESCRIPTION, description);
        db.insert(TABLE_NAME_FORUM, null, values);
        db.close();
    }

    public void insertThread(String threadId, String title, /*String url,*/ String description, String forumId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(THREAD_ID, threadId);
        values.put(TITLE, title);
        /*values.put(IMAGE_URL, url);*/
        values.put(DESCRIPTION, description);
        values.put(FORUM_ID, forumId);
        db.insert(TABLE_NAME_THREAD, null, values);
        db.close();
    }


    public HashMap<String, String> getAd(String adId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_CATEGORIES, new String[]{ID, CAT_IMAGE_URL, DESCRIPTION},
                ID+" = ? AND "+DOWNLOAD_STATUS+" = ?", new String[]{adId, "1"}, null, null, null, null);
        HashMap<String, String> pub = null;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            pub = new HashMap<>();
            pub.put("adId", cr.getString(0));
            pub.put("image_url", cr.getString(1));
            pub.put("desc", cr.getString(2));

            cr.close();
            db.close();
        }
        return pub;
    }

    public HashMap<String, String> getBlog(String blogId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_BLOG, new String[]{ID, TITLE, CONTENT, EXCERPT, IMAGE_URL, AUTHOR, DATE, URL},
                ID+" = ?", new String[]{blogId}, null, null, null, null);
        HashMap<String, String> pub = null;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            pub = new HashMap<>();
            pub.put("id", cr.getString(0));
            pub.put("title", cr.getString(1));
            pub.put("content", cr.getString(2));
            pub.put("excerpt", cr.getString(3));
            pub.put("image_url", cr.getString(4));
            pub.put("author", cr.getString(5));
            pub.put("date", cr.getString(6));
            pub.put("url", cr.getString(7));

            cr.close();
            db.close();
        }
        return pub;
    }

    public HashMap<String, String> getPregnancyCare(String careId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_PREGNANCY_CARE, new String[]{ID, WEEK, CONTENT, EXCERPT, IMAGE_URL},
                ID+" = ?", new String[]{careId}, null, null, null, null);
        HashMap<String, String> pub = null;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            pub = new HashMap<>();
            pub.put("id", cr.getString(0));
            pub.put("week", cr.getString(1));
            pub.put("content", cr.getString(2));
            pub.put("excerpt", cr.getString(3));
            pub.put("image_url", cr.getString(4));

            cr.close();
            db.close();
        }
        return pub;
    }

    public List<HashMap<String, String>> getBlogs(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_BLOG, new String[]{ID, TITLE, CONTENT, EXCERPT, IMAGE_URL, AUTHOR, DATE, URL},
                null, null, null, null, ID + " DESC", null);
        List<HashMap<String, String>> blogs = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("title", cr.getString(1));
                row.put("content", cr.getString(2));
                row.put("excerpt", cr.getString(3));
                row.put("image_url", cr.getString(4));
                row.put("author", cr.getString(5));
                row.put("date", cr.getString(6));
                row.put("url", cr.getString(7));

                blogs.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return blogs;
    }

    public List<HashMap<String, String>> getPregnancyCares(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_PREGNANCY_CARE, new String[]{ID, WEEK, CONTENT, EXCERPT, IMAGE_URL},
                null, null, null, null, ID, null);
        List<HashMap<String, String>> cares = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("week", cr.getString(1));
                row.put("content", cr.getString(2));
                row.put("excerpt", cr.getString(3));
                row.put("image_url", cr.getString(4));

                cares.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return cares;
    }

    public List<HashMap<String, String>> getFAQs(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_FAQ, new String[]{ID, QUESTION, ANSWER},
                null, null, null, null, null, null);
        List<HashMap<String, String>> faqs = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("question", cr.getString(1));
                row.put("answer", cr.getString(2));

                faqs.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return faqs;
    }

    public List<HashMap<String, String>> getFora(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_FORUM, new String[]{FORUM_ID, TITLE, DESCRIPTION, PRIVACY},
                null, null, null, null, FORUM_ID + " DESC", null);
        List<HashMap<String, String>> fora = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("title", cr.getString(1));
                row.put("description", cr.getString(2));
                row.put("privacy", cr.getString(3));
                row.put("thread_count", getThreadCount(cr.getString(0)));

                fora.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return fora;
    }

    public List<HashMap<String, String>> getThreads(String forumId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_THREAD, new String[]{THREAD_ID, TITLE, DESCRIPTION, /*IMAGE_URL,*/ FORUM_ID},
                FORUM_ID+" = ?", new String[]{forumId}, null, null, THREAD_ID + " DESC", null);
        List<HashMap<String, String>> threads = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("title", cr.getString(1));
                row.put("description", cr.getString(2));
                /*row.put("image_url", cr.getString(3));*/
                row.put("forum_id", cr.getString(3));

                threads.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return threads;
    }

    public HashMap<String, String> getThread(String threadId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_THREAD, new String[]{THREAD_ID, TITLE, DESCRIPTION, /*IMAGE_URL,*/ FORUM_ID},
                THREAD_ID+" = ?", new String[]{threadId}, null, null, null, null);
        HashMap<String, String> row = null;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            row = new HashMap<>();
            row.put("id", cr.getString(0));
            row.put("title", cr.getString(1));
            row.put("description", cr.getString(2));
            /*row.put("image_url", cr.getString(3));*/
            row.put("forum_id", cr.getString(3));

            cr.close();
            db.close();
        }
        return row;
    }

    public List<HashMap<String, String>> getCatBlogs(String catId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_BLOG, new String[]{ID, TITLE, CONTENT, EXCERPT, IMAGE_URL, AUTHOR, DATE, URL},
                CAT_ID+" = ?", new String[]{catId}, null, null, ID + " DESC", null);
        List<HashMap<String, String>> blogs = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("title", cr.getString(1));
                row.put("content", cr.getString(2));
                row.put("excerpt", cr.getString(3));
                row.put("image_url", cr.getString(4));
                row.put("author", cr.getString(5));
                row.put("date", cr.getString(6));
                row.put("url", cr.getString(7));

                blogs.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return blogs;
    }

    public List<HashMap<String, String>> getBlogImageUrls(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_BLOG, new String[]{ID, IMAGE_URL},
                null, null, null, null, ID+" DESC", null);
        List<HashMap<String, String>> uAds = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("image_url", cr.getString(1));

                uAds.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return uAds;
    }

    public List<String> getDownloadedCats(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_CATEGORIES, new String[]{ID, CAT_IMAGE_URL, DESCRIPTION},
                DOWNLOAD_STATUS+" = ?", new String[]{"1"}, null, null, null, null);
        List<String> ids = new ArrayList<>();
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                ids.add(cr.getString(0));
            }while (cr.moveToNext());

            cr.close();
            db.close();
        }
        return ids;
    }

    public List<HashMap<String, String>> getCategories(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_CATEGORIES, new String[]{ID, TITLE, CAT_IMAGE_URL, DESCRIPTION},
                null, null, null, null, null, null);
        List<HashMap<String, String>> cats = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("title", cr.getString(1));
                row.put("image_url", cr.getString(2));
                row.put("desc", cr.getString(3));

                cats.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return cats;
    }

    public List<HashMap<String, String>> getUndownloadedCats(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_CATEGORIES, new String[]{ID, CAT_IMAGE_URL, DESCRIPTION, DOWNLOAD_STATUS},
                DOWNLOAD_STATUS + " =?", new String[]{"0"}, null, null, null, null);
        List<HashMap<String, String>> uAds = new ArrayList<>();
        HashMap<String, String> row;
        if(cr != null &&(cr.getCount() > 0)){
            cr.moveToFirst();
            do{
                row = new HashMap<>();
                row.put("id", cr.getString(0));
                row.put("image_url", cr.getString(1));
                row.put("desc", cr.getString(2));

                uAds.add(row);
            }
            while(cr.moveToNext());
            cr.close();
            db.close();
        }
        return uAds;
    }

    public String getThreadCount(String forumId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.query(TABLE_NAME_THREAD, new String[]{THREAD_ID},
                FORUM_ID + " =?", new String[]{forumId}, null, null, null, null);
        int count = 0;
        if(cr != null){
            count = cr.getCount();
            cr.close();
            db.close();
        }
        return String.valueOf(count);
    }

    public void updateAdDownloadStatus(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DOWNLOAD_STATUS, "1");
        db.update(TABLE_NAME_CATEGORIES, values, ID+" = ?", new String[] {id});
        db.close();
    }

}
