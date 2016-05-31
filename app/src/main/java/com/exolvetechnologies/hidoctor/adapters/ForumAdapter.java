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
 * Created by ekeretepeter on 09/04/16.
 */
public class ForumAdapter extends BaseAdapter {

    private List<HashMap<String, String>> courses;
    private Context context;

    public ForumAdapter(List<HashMap<String, String>> courses, Context context) {
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
        String description = courses.get(i).get("description");
        String countStr = courses.get(i).get("thread_count");

        if(convertView==null){

            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.model_forum, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(android.R.id.text1);
            holder.desc=(TextView)view.findViewById(android.R.id.text2);
            holder.count=(TextView)view.findViewById(R.id.threadCount);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.title.setText(Html.fromHtml(titleStr));
        holder.desc.setText(Html.fromHtml(description));
        holder.count.setText(Html.fromHtml(countStr));

        return view;
    }

    public static class ViewHolder{

        public TextView title;
        public TextView desc;
        public TextView count;
        //public ImageView image;

    }
}
