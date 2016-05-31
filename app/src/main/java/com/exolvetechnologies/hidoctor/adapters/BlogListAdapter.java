package com.exolvetechnologies.hidoctor.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ekeretepeter on 28/01/16.
 */
public class BlogListAdapter extends BaseAdapter {

    private List<HashMap<String, String>> courses;
    private Context context;

    public BlogListAdapter(List<HashMap<String, String>> courses, Context context) {
        this.courses = courses;
        this.context = context;
    }

    @Override
    public int getCount() {
        return courses.size();
        //return 3;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        String titleStr = courses.get(i).get("title");
        String excerptStr = courses.get(i).get("excerpt");
        String authorStr = courses.get(i).get("author");
        String dateStr = courses.get(i).get("date");
        //String lecturerStr = courses.get(i).get("course_lecturer");
        //String imageId = courses.get(i).get("thumbnail");

        if(convertView==null){

            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.model_blog_preview, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(android.R.id.text1);
            holder.excerpt=(TextView)view.findViewById(android.R.id.text2);
            holder.author=(TextView)view.findViewById(R.id.textViewAuthor);
            holder.date=(TextView)view.findViewById(R.id.textViewDate);
            //holder.image=(ImageView)view.findViewById(R.id.course_image);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.title.setText(Html.fromHtml(titleStr));
        holder.excerpt.setText(Html.fromHtml(excerptStr));
        holder.author.setText(Html.fromHtml(authorStr));
        holder.date.setText(Html.fromHtml(dateStr));

        //holder.title.setText(titleStr);
        /*AppUtils.setTextViewTextLimit(holder.title, titleStr, 20);
        if (lecturerStr != null && (!lecturerStr.equals("null"))) {
            holder.lecturer.setText(lecturerStr);
        }*/
        //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.course_default);
        //holder.image.setImageBitmap(bitmap);

        return view;
    }

    public static class ViewHolder{

        public TextView title;
        public TextView excerpt;
        public TextView author;
        public TextView date;
        //public ImageView image;

    }
}
