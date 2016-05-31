package com.exolvetechnologies.hidoctor.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.activities.MainActivity;
import com.exolvetechnologies.hidoctor.models.PagerClickListener;
import com.exolvetechnologies.hidoctor.utilities.QuickstartPreferences;

public class VideoSliderFragment extends Fragment {

    private static final String POSITION = "position";

    private int position;
    public static PagerClickListener pagerListener;


    public VideoSliderFragment() {
        // Required empty public constructor
    }

    public static VideoSliderFragment newInstance(int position) {
        VideoSliderFragment fragment = new VideoSliderFragment();
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_slider, container, false);
        TextView textView = (TextView) view.findViewById(R.id.videoTile);
        ImageView videoThumbnail = (ImageView) view.findViewById(R.id.videoThumbnail);

        Uri videoURI = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.sample_video);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getActivity(), videoURI);
        Bitmap bitmap = retriever
                .getFrameAtTime(100000,MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        videoThumbnail.setBackground(drawable);

        String title = MainActivity.vTitles[position];
        textView.setText(title);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent =  new Intent(QuickstartPreferences.PAGER_CLICK_INTENT);
                //getActivity().sendBroadcast(intent);
                pagerListener.pagerItemClick();
            }
        });

        return view;
    }

}
