package com.exolvetechnologies.hidoctor.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ekeretepeter on 6/22/15.
 */
public class CategoryAdapter extends BaseAdapter {

    private List<HashMap<String, String>> categories;
    private Activity context;
    private AppUtils utils;

    public CategoryAdapter(List<HashMap<String, String>> contacts, Activity activity) {
        this.categories = contacts;
        this.context = activity;
        utils = new AppUtils(context);

    }

    @Override
    public int getCount() {
        return categories.size();
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

        String id = categories.get(i).get("id");
        String title = categories.get(i).get("title");
        String desc =  categories.get(i).get("desc");
        String imageUrl =  categories.get(i).get("image_url");
        String fileName = "cat_"+id;

        if(convertView==null){

            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.model_doctor_category, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.image = (ImageView) view.findViewById(R.id.cat_image);
            holder.desc = (TextView) view.findViewById(R.id.description);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.title.setText(title);
        holder.desc.setText(desc);

        String filePath = context.getExternalCacheDir()+ File.separator+"Images/"+fileName;
        Bitmap bitmap = AppUtils.decodeFile(filePath, 200, 200);

        /*if (bitmap != null) {
            holder.image.setImageBitmap(bitmap);
        }*/
        if (bitmap != null) {
            holder.image.setImageBitmap(bitmap);
        }else {
            holder.image.setTag(fileName);
            utils.setImageFromServer(imageUrl, fileName, 200, 200, holder.image, null, true);
        }

        return view;
    }

    public static class ViewHolder{

        public TextView title;
        public TextView desc;
        public ImageView image;

    }
}