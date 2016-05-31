package com.exolvetechnologies.hidoctor.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.app.ApplicationSettings;
import com.exolvetechnologies.hidoctor.app.ooVooSdkSampleShowApp;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity;

public class OptionFragment extends BaseFragment {

    private MenuItem settingsMenuItem = null;

    public static OptionFragment newInstance(MenuItem settingsMenuItem) {
        OptionFragment fragment = new OptionFragment();
        fragment.settingsMenuItem = settingsMenuItem;

        return fragment;
    }

    public OptionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_option, container, false);

        Button roomButton = (Button) view.findViewById(R.id.room_button);
        roomButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                app().room();
            }
        });

        Button makeCallButton = (Button) view.findViewById(R.id.make_call_button);
        makeCallButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                app().makeCall();
            }
        });

        Button pushNotificationButton = (Button) view.findViewById(R.id.push_notification_button);
        pushNotificationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                app().pushNotification();
            }
        });

        CheckBox useCustomRender = (CheckBox) view.findViewById(R.id.use_custom_render);

        String useCustomRenderValue = settings().get(ApplicationSettings.UseCustomRender);

        if (useCustomRenderValue != null) {
            useCustomRender.setChecked(Boolean.valueOf(useCustomRenderValue));
        } else {
            settings().put(ApplicationSettings.UseCustomRender, Boolean.toString(false));
        }

        useCustomRender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                settings().put(ApplicationSettings.UseCustomRender, Boolean.toString(isChecked));
            }
        });

        //switch (ChatsActivity.CHAT_TYPE) {
            //case "video" :
            if (!ChatsActivity.HAS_CREATED_ROOM) {
                app().room();
                ChatsActivity.HAS_CREATED_ROOM = true;
            }
            //    break;
            /*case "audio" :
                if (!ChatsActivity.CALL_MADE) {
                    app().makeCall();
                    ChatsActivity.CALL_MADE = true;
                }
                break;*/
       // }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (settingsMenuItem != null) {
            settingsMenuItem.setVisible(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (settingsMenuItem != null) {
            settingsMenuItem.setVisible(false);
        }
    }

    public boolean onBackPressed() {
        ((ooVooSdkSampleShowApp)getActivity().getApplication()).logout();
        return true;
    }

    public BaseFragment getBackFragment()
    {
        return LoginFragment.newInstance(settingsMenuItem);
    }
}
