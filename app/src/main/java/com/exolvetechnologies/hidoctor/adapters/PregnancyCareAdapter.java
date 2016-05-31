package com.exolvetechnologies.hidoctor.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ekeretepeter on 12/04/16.
 */
public class PregnancyCareAdapter extends BaseAdapter {

    private List<HashMap<String, String>> courses;
    private Context context;

    public PregnancyCareAdapter(List<HashMap<String, String>> courses, Context context) {
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

        String weekStr = courses.get(i).get("week");
        String excerptStr = courses.get(i).get("excerpt");

        if(convertView==null){

            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.model_pregnancy_care, parent, false);

            holder = new ViewHolder();
            holder.week = (TextView) view.findViewById(android.R.id.text1);
            holder.excerpt=(TextView)view.findViewById(android.R.id.text2);
            //holder.image=(ImageView)view.findViewById(R.id.course_image);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.week.setText(Html.fromHtml("Week "+weekStr));
        holder.excerpt.setText(Html.fromHtml(excerptStr));

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

        public TextView week;
        public TextView excerpt;
        //public TextView author;
        //public TextView date;
        //public ImageView image;

    }
}
