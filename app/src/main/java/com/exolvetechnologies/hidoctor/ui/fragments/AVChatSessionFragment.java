package com.exolvetechnologies.hidoctor.ui.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.exolvetechnologies.hidoctor.activities.LoginActivity;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;
import com.oovoo.core.Utils.LogSdk;
import com.oovoo.core.sdk_error;
import com.oovoo.sdk.api.ui.VideoPanel;
import com.oovoo.sdk.interfaces.AudioController;
import com.oovoo.sdk.interfaces.AudioRoute;
import com.oovoo.sdk.interfaces.AudioRouteController;
import com.oovoo.sdk.interfaces.Device;
import com.oovoo.sdk.interfaces.Effect;
import com.oovoo.sdk.interfaces.VideoController;
import com.oovoo.sdk.interfaces.VideoControllerListener.RemoteVideoState;
import com.oovoo.sdk.interfaces.VideoDevice;
import com.oovoo.sdk.interfaces.VideoRender;
import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.app.ApplicationSettings;
import com.exolvetechnologies.hidoctor.app.ooVooSdkSampleShowApp;
import com.exolvetechnologies.hidoctor.app.ooVooSdkSampleShowApp.CallControllerListener;
import com.exolvetechnologies.hidoctor.app.ooVooSdkSampleShowApp.NetworkListener;
import com.exolvetechnologies.hidoctor.app.ooVooSdkSampleShowApp.ParticipantsListener;
import com.exolvetechnologies.hidoctor.ui.CustomVideoPanel;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity.MenuList;
import com.exolvetechnologies.hidoctor.ui.SignalBar;
import com.exolvetechnologies.hidoctor.ui.fragments.AVChatSessionFragment.VideoAdapter.VideoItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AVChatSessionFragment extends BaseFragment implements ParticipantsListener, CallControllerListener, View.OnClickListener, OnItemClickListener, NetworkListener {

    public static final String DEBUG_TAG ="HiDoctor";

    private CountDownTimer timer, resendTimer;
    private boolean doctorHasJoined;

    public enum CameraState {
        BACK_CAMERA(0), FRONT_CAMERA(1), MUTE_CAMERA(2);

        private final int value;

        private CameraState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    protected static final String TAG = AVChatSessionFragment.class.getSimpleName();

    private View self = null;
    private Button microphoneBttn = null;
    private Button speakerBttn = null;
    private Button cameraBttn = null;
    private Button endOfCall = null;
    private View callbar = null;
    private GridView videoGridView = null;
    private VideoAdapter videoAdapter = null;
    private VideoRender fullScreenRemoteview = null;
    private ImageView fullScreenAvatar = null;
    private TextView fullScreenLabel = null;
    private TextView timeLeft = null;
    private LinearLayout notificationBox;
    private MenuItem signalStrengthMenuItem = null;
    private MenuItem secureNetworkMenuItem = null;
    private MenuItem informationMenuItem = null;
    private CameraState cameraState = CameraState.FRONT_CAMERA;
    private ArrayList<Effect> filters = null;

    private long timeLeftMiliSecs;
    private boolean chatEnded = false;

    public AVChatSessionFragment() {
    }

    public static final AVChatSessionFragment newInstance(MenuItem signalStrengthMenuItem,
                                                          MenuItem secureNetworkMenuItem,
                                                          MenuItem informationMenuItem) {
        AVChatSessionFragment instance = new AVChatSessionFragment();
        instance.setSignalStrengthMenuItem(signalStrengthMenuItem);
        instance.setSecureNetworkMenuItem(secureNetworkMenuItem);
        instance.setInformationMenuItem(informationMenuItem);

        return instance;
    }

    public void setSignalStrengthMenuItem(MenuItem signalStrengthMenuItem) {
        this.signalStrengthMenuItem = signalStrengthMenuItem;
    }

    public void setSecureNetworkMenuItem(MenuItem secureNetworkMenuItem) {
        this.secureNetworkMenuItem = secureNetworkMenuItem;
    }

    public void setInformationMenuItem(MenuItem informationMenuItem) {
        this.informationMenuItem = informationMenuItem;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new NotifyDoctorAsync().execute();
        resendNotification();
        timeLeftMiliSecs = Long.parseLong(String.valueOf(ChatsActivity.TIME_LEFT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        self = inflater.inflate(R.layout.avchat_fragment, container, false);
        filters = app().getVideoFilters();

        app().selectVideoEffect("original");

        initControlBar(self);

        timeLeft = (TextView)self.findViewById(R.id.time_left);
        timeLeft.setText(formatTime(ChatsActivity.TIME_LEFT));
        notificationBox = (LinearLayout) self.findViewById(R.id.notificationBox);


        videoGridView = (GridView) self.findViewById(R.id.video_grid_view);
        videoAdapter = new VideoAdapter(getActivity());
        videoGridView.setAdapter(videoAdapter);
        videoGridView.setOnItemClickListener(this);

        String useCustomRenderValue = settings().get(ApplicationSettings.UseCustomRender);
        if (useCustomRenderValue != null && Boolean.valueOf(useCustomRenderValue)) {
            fullScreenRemoteview = (CustomVideoPanel) self.findViewById(R.id.full_screen_custom_panel_remoteview);
        } else {
            fullScreenRemoteview = (VideoPanel) self.findViewById(R.id.full_screen_video_panel_remoteview);
        }

        fullScreenAvatar = (ImageView) self.findViewById(R.id.full_screen_avatar_image_view);
        fullScreenLabel = (TextView) self.findViewById(R.id.full_screen_label);
        setupFullScreenViewClickListener();

        addParticipantVideoPanel(ApplicationSettings.PREVIEW_ID, "Me");

        app().addParticipantListener(this);
        app().setControllerListener(this);

        try {
            String securityState = settings().get(ApplicationSettings.SecurityState);
            if (securityState != null && Boolean.valueOf(securityState)) {
                secureNetworkMenuItem.setIcon(getResources().getDrawable(R.drawable.menu_ic_lock));
            } else {
                secureNetworkMenuItem.setIcon(getResources().getDrawable(R.drawable.menu_ic_lock_unlock));
            }
        }
        catch(Exception err){
            LogSdk.e(TAG,"onCreateView "+err);
        }

        return self;
    }

    private void initControlBar(View callbar) {
        this.callbar = callbar;

        microphoneBttn = (Button) callbar.findViewById(R.id.microphoneButton);
        microphoneBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                microphoneBttn.setEnabled(false);
                app().onMicrophoneClick();
            }
        });

        speakerBttn = (Button) callbar.findViewById(R.id.speakersButton);
        speakerBttn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                speakerBttn.setEnabled(false);
                app().onSpeakerClick();
            }
        });

        cameraBttn = (Button) callbar.findViewById(R.id.cameraButton);
        prepareButtonMenu(cameraBttn, new MenuList() {
            @Override
            public void fill(View view, ContextMenu menu) {
                try {
                    menu.setHeaderTitle(R.string.change_camera);
                    ArrayList<VideoDevice> cameras = app().getVideoCameras();
                    for (VideoDevice camera : cameras) {
                        MenuItem item = null;

                        if (camera.toString().equals("FRONT")) {
                            item = menu.add(view.getId(), CameraState.FRONT_CAMERA.getValue(), 0, R.string.front_camera);
                        } else if (camera.toString().equals("BACK")) {
                            item = menu.add(view.getId(), CameraState.BACK_CAMERA.getValue(), 0, R.string.back_camera);
                        } else {
                            item = menu.add(view.getId(), -1, 0, R.string.unknown);
                        }

                        item.setOnMenuItemClickListener(new DeviceMenuClickListener(camera) {
                            @Override
                            public boolean onMenuItemClick(Device camera, MenuItem item) {
                                if (item.getItemId() == cameraState.getValue()) {
                                    return true;
                                }
                                app().switchCamera((VideoDevice) camera);
                                app().muteCamera(false);
                                videoAdapter.hideAvatar(null);
                                if (item.getItemId() == CameraState.FRONT_CAMERA.getValue()) {
                                    cameraState = CameraState.FRONT_CAMERA;
                                } else {
                                    cameraState = CameraState.BACK_CAMERA;
                                }
                                cameraBttn.setSelected(false);
                                return true;
                            }

                        });
                    }

                    MenuItem item = menu.add(view.getId(), CameraState.MUTE_CAMERA.getValue(), 0, R.string.mute_camera);
                    item.setOnMenuItemClickListener(new MuteCameraMenuClickListener(app()) {

                        @Override
                        public boolean onMenuItemClick(boolean state, MenuItem item) {
                            if (item.getItemId() == cameraState.getValue()) {
                                return true;
                            }
                            app().muteCamera(state);
                            videoAdapter.showAvatar(null);
                            cameraState = state ? CameraState.MUTE_CAMERA : CameraState.MUTE_CAMERA;
                            cameraBttn.setSelected(true);
                            return true;
                        }
                    });

                    for (int i = 0; i < menu.size(); ++i) {
                        MenuItem mi = menu.getItem(i);
                        if (cameraState.getValue() == mi.getItemId()) {
                            mi.setChecked(true);
                            break;
                        }
                    }

                    menu.setGroupCheckable(view.getId(), true, true);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

        endOfCall = (Button) callbar.findViewById(R.id.endOfCallButton);
        endOfCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                app().onEndOfCall();
                app().sendEndOfCall();

                int count = getFragmentManager().getBackStackEntryCount();
                String name = getFragmentManager().getBackStackEntryAt(count - 2).getName();
                getFragmentManager().popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });


        prepareButtonMenu((Button) callbar.findViewById(R.id.effectButton), new MenuList() {
            @Override
            public void fill(View view, ContextMenu menu) {
                try {
                    menu.setHeaderTitle(R.string.filters);


                    for (Effect effect : filters) {
                        MenuItem item = menu.add(effect.toString());
                        item.setChecked(false);
                        LogSdk.d(TAG, "Effect " + effect);
                        item.setOnMenuItemClickListener(new EffectMenuClickListener(effect) {

                            @Override
                            public boolean onMenuItemClick(Effect effect, MenuItem item) {
                                app().changeVideoEffect(effect);
                                return false;
                            }

                        });
                        item.setCheckable(true);
                        Effect active_effect = app().getActiveEffect();
                        if (active_effect != null) {
                            item.setChecked(active_effect.getID().equalsIgnoreCase(effect.getID()));
                        } else {
                            if (effect.getName().equalsIgnoreCase("original")) {
                                item.setChecked(true);
                            }
                        }
                    }

                    menu.setGroupCheckable(view.getId(), true, true);
                } catch (Exception err) {
                    err.printStackTrace();
                    LogSdk.e(TAG, "Effect err" + err);
                }
            }
        });

        app().selectCamera("FRONT");
        app().changeResolution(VideoController.ResolutionLevel.ResolutionLevelMed);
        app().openPreview();
//        app().startTransmit();
        settings().put(ApplicationSettings.ResolutionLevel, toResolutionString(VideoController.ResolutionLevel.ResolutionLevelMed));

        prepareButtonMenu((Button) callbar.findViewById(R.id.videoResolution), new MenuList() {
            @Override
            public void fill(View view, ContextMenu menu) {
                try {
                    menu.setHeaderTitle(R.string.resolution);
                    menu.setGroupCheckable(view.getId(), true, true);
                    String activeResolution = toResolutionString(app().getActiveResolution());

                    for (VideoController.ResolutionLevel resolution : app().getAvailableResolutions()) {
                        MenuItem item = menu.add(toResolutionString(resolution));
                        item.setOnMenuItemClickListener(new ResolutionMenuClickListener(resolution) {

                            @Override
                            public boolean onMenuItemClick(VideoController.ResolutionLevel resolution, MenuItem item) {
                                app().changeResolution(resolution);
                                settings().put(ApplicationSettings.ResolutionLevel, item.getTitle().toString());
                                item.setChecked(true);
                                return false;
                            }
                        });

                        if (item.getTitle().toString().equals(activeResolution)) {
                            item.setChecked(true);
                        }

                        item.setCheckable(true);
                    }

                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

        app().getAudioRouteController().setListener(new AudioRouteController.AudioRouteControllerListener() {
            @Override
            public void onAudioRouteChanged(AudioRoute audioRoute, AudioRoute audioRoute1) {
                onAudioRouteChangedEvent(audioRoute, audioRoute1);
            }
        });


        prepareButtonMenu((Button) callbar.findViewById(R.id.audioRoutes), new MenuList() {
            @Override
            public void fill(View view, ContextMenu menu) {
                try {
                    menu.setHeaderTitle(R.string.audio_routes);
                    ArrayList<AudioRoute> routes = app().getAudioRouteController().getRoutes();
                    for (AudioRoute route : routes) {
                        MenuItem item = menu.add(route.toString());
                        item.setOnMenuItemClickListener(new AudioRouteMenuClickListener(route) {

                            @Override
                            public boolean onMenuItemClick(AudioRoute route, MenuItem item) {
                                app().changeRoute(route);
                                return false;
                            }

                        });
                        item.setCheckable(true);
                        item.setChecked(route.isActive());

                    }

                    menu.setGroupCheckable(view.getId(), true, true);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

        app().setMicMuted(false);
        app().setSpeakerMuted(false);

        ArrayList<AudioRoute> routes = app().getAudioRouteController().getRoutes();
        for (AudioRoute route : routes) {
            if (route.isActive())
                updateRouteButtonImage(route);
        }

        updateController();
    }

    private String toResolutionString(VideoController.ResolutionLevel level) {
        String friendlyName = "";
        switch (level) {
            case ResolutionLevelLow:
                friendlyName = "Low";
                break;
            case ResolutionLevelMed:
                friendlyName = "Medium";
                break;
            case ResolutionLevelHigh:
                friendlyName = "High";
                break;
            case ResolutionLevelHD:
                friendlyName = "HD";
                break;
            default:
                break;
        }
        return friendlyName;
    }

    @Override
    public void onResume() {

        try {
            app().setNetworkListener(this);
            signalStrengthMenuItem.setVisible(true);
            secureNetworkMenuItem.setVisible(true);
            informationMenuItem.setVisible(true);

        } catch (Exception err) {
            LogSdk.e(TAG, "onResume" + err);
        }

        super.onResume();
        if(timer != null){
            timer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            app().setNetworkListener(null);
            signalStrengthMenuItem.setVisible(false);
            secureNetworkMenuItem.setVisible(false);
            informationMenuItem.setVisible(false);
        } catch (Exception err) {

        }
        if(timer != null){
            timer.cancel();
        }
    }

    @Override
    public void onStop() {

        if(timer != null){
            timer.cancel();
        }
        if(resendTimer != null){
            resendTimer.cancel();
        }
        if (!chatEnded) {
            new AppUtils.EndChatAsync(timeLeftMiliSecs).execute();
            chatEnded = true;
            getActivity().finish();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        try {
            app().removeParticipantListener(this);
            app().setControllerListener(null);
            super.onDestroy();
        } catch (Exception err) {

        }
        if(timer != null){
            timer.cancel();
        }
        if(resendTimer != null){
            resendTimer.cancel();
        }
        if (!chatEnded) {
            new AppUtils.EndChatAsync(timeLeftMiliSecs).execute();
            chatEnded = true;
            getActivity().finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (app().isTablet()) {
            updatePreviewLayout();
        }
    }

    public void updatePreviewLayout() {
        VideoItem item = videoAdapter.getItem(0);
        if (item != null) {
            View gridItem = (View) ((View) item.getVideo()).getTag(R.layout.video_grid_item);
            int[] paddings = getVideoWindowPaddings();
            int width = videoAdapter.isPreviewFullScreen() ? getDisplaySize().x :
                    (getDisplaySize().x - gridItem.getPaddingLeft() * 3) / 2;
            int height = videoAdapter.isPreviewFullScreen() ? (getDisplaySize().y - (paddings[0] + paddings[1])) :
                    (getDisplaySize().y - (paddings[0] + paddings[1])) / 2;

            if (!videoAdapter.isPreviewFullScreen()) {
                for (int i = 0; i < videoAdapter.getCount(); i++) {
                    item = videoAdapter.getItem(i);
                    if (item.getVideo() != null) {
                        gridItem = (View) ((View) item.getVideo()).getTag(R.layout.video_grid_item);
                        gridItem.setLayoutParams(new GridView.LayoutParams(width, height));
                    }
                }
            } else {
                gridItem.setLayoutParams(new GridView.LayoutParams(width, height));
            }
        }
    }

    public Point getDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private  void  buildAlert(long duration){
        //int chatTimeInt = Integer.parseInt(chatTime);
        //String longTime = String.valueOf((long)(chatTimeInt));
        //long duration = TimeUnit.MINUTES.toMillis(Long.parseLong(longTime));

        timer = new CountDownTimer(duration, 500) {
            public void onTick(long millisUntilFinished) {
                timeLeft.setText(""+formatTime(millisUntilFinished));
                timeLeftMiliSecs = millisUntilFinished;
            }
            @SuppressWarnings("deprecation")
            public void onFinish() {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Time up");
                alertDialog.setMessage("Your time is up");
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (!chatEnded) {
                            new AppUtils.EndChatAsync(timeLeftMiliSecs).execute();
                            chatEnded = true;
                            getActivity().finish();
                        }
                    }
                });

                // Showing Alert Message
                alertDialog.show();
            }
        }.start();
        notificationBox.setVisibility(View.GONE);
    }

    @SuppressLint("DefaultLocale")
    public String formatTime(long millisUntilFinished){
        return String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
    }

    public void onTransmitStateChanged(boolean state, sdk_error error) {

    }

    private void resendNotification(){
        resendTimer = new CountDownTimer(180000, 30000) {
            public void onTick(long millisUntilFinished) {
                if (!doctorHasJoined){
                    new NotifyDoctorAsync().execute();
                }
            }
            public void onFinish() {
                doctorHasJoined = true;
            }
        }.start();
    }

    @Override
    public void onRemoteVideoStateChanged(final String userId, final RemoteVideoState state, final sdk_error error) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switch (state) {
					case RVS_Started:
					case RVS_Resumed:
						videoAdapter.hideNoVideoMessage(userId);
						break;
					case RVS_Stopped:
						break;
					case RVS_Paused:
						videoAdapter.showNoVideoMessage(userId);
						break;
				}
				
				if (error == sdk_error.ResolutionNotSupported) {
					videoAdapter.showAvatar(userId);
				}
			}
		});
    }

    @Override
    public void onParticipantJoined(final String userId, final String userData) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addParticipantVideoPanel(userId, userData);
                    buildAlert(ChatsActivity.TIME_LEFT);
                    doctorHasJoined = true;
                }
            });
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    protected void addParticipantVideoPanel(String userId, String userData) {
        try {
            videoAdapter.addItem(videoAdapter.new VideoItem(userId, userData));
            //if(timer != null){
               // timer.cancel();
            //}

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public void onParticipantLeft(final String userId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeParticipantVideoPanel(userId);
            }
        });
        if(timer != null){
            timer.cancel();
        }

        if(resendTimer != null){
            resendTimer.cancel();
        }
        if (!chatEnded) {
            new AppUtils.EndChatAsync(timeLeftMiliSecs).execute();
            chatEnded = true;
            getActivity().finish();
        }
    }

    protected void removeParticipantVideoPanel(String userId) {
        try {
            videoAdapter.removeItem(userId);
            disableFullScreenView();

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public int[] getVideoWindowPaddings() {
        int[] paddings = new int[2];

        final View bottomView = AVChatSessionFragment.this.callbar.findViewById(R.id.call_controll_layout);
        final Window window = AVChatSessionFragment.this.getActivity().getWindow();
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int contentViewBottom = bottomView.getMeasuredHeight();
        contentViewBottom = contentViewBottom == 0 ? contentViewTop : contentViewBottom;

        paddings[0] = contentViewTop;
        paddings[1] = contentViewBottom;

        return paddings;
    }

    private int[] p1 = new int[4];
    private int[] p2 = new int[4];

    private final Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            View gridItem = (View)msg.obj;
            gridItem.setPadding(0, 0, 0, 0);
            videoGridView.setPadding(0, 0, 0, 0);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        VideoItem item = videoAdapter.getItem(position);

        if (!item.isAvatarVisible() && !item.isErrorMessageVisible()) {

            int visibility = View.INVISIBLE;

            if (item.getUserId().isEmpty() && !item.isFullScreen()) {

                int[] paddings = getVideoWindowPaddings();

                int width = getDisplaySize().x;
                int height = (getDisplaySize().y - (paddings[0] + paddings[1]));

                View gridItem = (View) ((View)item.getVideo()).getTag(R.layout.video_grid_item);
                ((GLSurfaceView)item.getVideo()).setZOrderMediaOverlay(true);
                gridItem.setLayoutParams(new GridView.LayoutParams(width, height));

                p1[0] = gridItem.getPaddingLeft();
                p1[1] = gridItem.getPaddingTop();
                p1[2] = gridItem.getPaddingRight();
                p1[3] = gridItem.getPaddingBottom();

                p2[0] = videoGridView.getPaddingLeft();
                p2[1] = videoGridView.getPaddingTop();
                p2[2] = videoGridView.getPaddingRight();
                p2[3] = videoGridView.getPaddingBottom();

                Message msg = new Message();
                msg.obj = gridItem;
                handler.sendMessage(msg);

                item.setFullScreen(true);
            } else if (item.getUserId().isEmpty() && item.isFullScreen()) {

                View gridItem = (View) ((View)item.getVideo()).getTag(R.layout.video_grid_item);
                ((GLSurfaceView)item.getVideo()).setZOrderMediaOverlay(false);
                gridItem.setPadding(p1[0], p1[1], p1[2], p1[3]);
                videoGridView.setPadding(p2[0], p2[1], p2[2], p2[3]);

                int[] paddings = getVideoWindowPaddings();

                int width = (getDisplaySize().x - gridItem.getPaddingLeft() * 3)/ 2;
                int height = (getDisplaySize().y - (paddings[0] + paddings[1])) / 2;

                gridItem.setLayoutParams(new GridView.LayoutParams(width, height));

                item.setFullScreen(false);
                visibility = View.VISIBLE;

            } else {

                ((View)fullScreenRemoteview).setTag(R.id.video_panel_view, item);
                ((View)fullScreenRemoteview).setVisibility(View.VISIBLE);
                ((GLSurfaceView)fullScreenRemoteview).setZOrderMediaOverlay(true);
                fullScreenLabel.setText(item.getUserData());
                fullScreenLabel.setVisibility(View.VISIBLE);

                setVideoRenderStateChangeListener(fullScreenRemoteview, new VideoPanel.VideoRenderStateChangeListener() {
                    @Override
                    public void onVideoRenderStart() {
                        try {
                            fullScreenAvatar.setVisibility(View.INVISIBLE);
                        } catch (Exception err) {
                            LogSdk.e(TAG, "onVideoRenderStart " + err);
                        }
                    }

                    @Override
                    public void onVideoRenderStop() {
                        try {
                            fullScreenAvatar.setVisibility(View.VISIBLE);
                        } catch (Exception err) {
                            LogSdk.e(TAG, "onVideoRenderStop " + err);
                        }
                    }
                });

                app().unbindVideoPanel(item.getUserId(), item.getVideo());
                app().bindVideoPanel(item.getUserId(), fullScreenRemoteview);
                item.disableListener();
            }

            for (int i = 0; i < videoAdapter.getCount(); i++) {
                item = videoAdapter.getItem(i);
                try {
                    if (!item.getUserId().isEmpty()) {
                        ((View)item.getVideo()).setVisibility(visibility);
                        View gridItem = (View)((View)item.getVideo()).getTag(R.layout.video_grid_item);
                        gridItem.setVisibility(visibility);
                    }

                } catch (Exception ex) {
                    LogSdk.e("TAG", ex.toString());
                }
            }
            if (app().isTablet()) {
                updatePreviewLayout();
            }

            videoAdapter.notifyDataSetChanged();
        }
    }

    private void disableFullScreenView() {
        try {
            for (int i = 0; i < videoAdapter.getCount(); i++) {
                VideoItem item = videoAdapter.getItem(i);
                if (item.getVideo() != null) {
                    ((View)item.getVideo()).setVisibility(View.VISIBLE);
                    View gridItem = (View)((View)item.getVideo()).getTag(R.layout.video_grid_item);
                    gridItem.setVisibility(View.VISIBLE);
                }
            }

            VideoItem item = (VideoItem) ((View)fullScreenRemoteview).getTag(R.id.video_panel_view);
            app().unbindVideoPanel(item.getUserId(), fullScreenRemoteview);

            ((View)fullScreenRemoteview).setVisibility(View.GONE);
            ((GLSurfaceView)fullScreenRemoteview).setZOrderMediaOverlay(false);
            setVideoRenderStateChangeListener(fullScreenRemoteview, null);
            fullScreenAvatar.setVisibility(View.GONE);
            fullScreenLabel.setVisibility(View.GONE);

            app().bindVideoPanel(item.getUserId(), item.getVideo());
            item.enableListener();

            videoAdapter.notifyDataSetChanged();

        } catch (Exception err) {

        }
    }

    public void setupFullScreenViewClickListener() {
        ((View)fullScreenRemoteview).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                disableFullScreenView();
            }
        });
    }

    @Override
    public void updateController() {
        try {
            microphoneBttn.setEnabled(true);
            speakerBttn.setEnabled(true);
            microphoneBttn.setSelected(app().isMicMuted());
            speakerBttn.setSelected(app().isSpeakerMuted());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        videoAdapter.removeAllItems();

        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (v instanceof Button && v.getTag() instanceof MenuList) {
            v.showContextMenu();
        }
    }

    private void prepareButtonMenu(final Button button, MenuList list) {
        button.setOnClickListener(this);
        button.setTag(list);
        getActivity().registerForContextMenu(button);
    }

    protected void onAudioRouteChangedEvent(AudioRoute old_route, AudioRoute new_route) {
        updateRouteButtonImage(new_route);
    }

    /**
     * When audio route changes we change button image too.
     *
     * @param new_route
     */
    private void updateRouteButtonImage(AudioRoute new_route) {
        try {
            Button button = (Button) callbar.findViewById(R.id.audioRoutes);
            switch (new_route.getRouteId()) {
                case AudioRoute.Earpiece:
                    button.setBackgroundResource(R.drawable.earpiece_selector);
                    break;
                case AudioRoute.Speaker:
                    button.setBackgroundResource(R.drawable.speakers_selector);
                    break;
                case AudioRoute.Headphone:
                    button.setBackgroundResource(R.drawable.headphone_selector);
                    break;
                case AudioRoute.Bluetooth:
                    button.setBackgroundResource(R.drawable.bluetooth_selector);
                    break;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

    }

    abstract class DeviceMenuClickListener implements MenuItem.OnMenuItemClickListener {
        private Device device = null;

        DeviceMenuClickListener(Device device) {
            this.device = device;
        }

        @Override
        public final boolean onMenuItemClick(MenuItem item) {
            return onMenuItemClick(device, item);
        }

        public abstract boolean onMenuItemClick(Device device, MenuItem item);
    }

    abstract class AudioRouteMenuClickListener implements MenuItem.OnMenuItemClickListener {
        private AudioRoute route = null;

        AudioRouteMenuClickListener(AudioRoute route) {
            this.route = route;
        }

        @Override
        public final boolean onMenuItemClick(MenuItem item) {
            return onMenuItemClick(route, item);
        }

        public abstract boolean onMenuItemClick(AudioRoute route, MenuItem item);
    }

    abstract class EffectMenuClickListener implements MenuItem.OnMenuItemClickListener {
        private Effect effect = null;

        EffectMenuClickListener(Effect effect) {
            this.effect = effect;
        }

        @Override
        public final boolean onMenuItemClick(MenuItem item) {
            return onMenuItemClick(effect, item);
        }

        public abstract boolean onMenuItemClick(Effect device, MenuItem item);
    }

    abstract class ResolutionMenuClickListener implements MenuItem.OnMenuItemClickListener {
        private VideoController.ResolutionLevel resolution = null;

        ResolutionMenuClickListener(VideoController.ResolutionLevel resolution) {
            this.resolution = resolution;
        }

        @Override
        public final boolean onMenuItemClick(MenuItem item) {
            return onMenuItemClick(resolution, item);
        }

        public abstract boolean onMenuItemClick(VideoController.ResolutionLevel resolution, MenuItem item);
    }

    abstract class MuteCameraMenuClickListener implements MenuItem.OnMenuItemClickListener {
        ooVooSdkSampleShowApp app = null;

        MuteCameraMenuClickListener(ooVooSdkSampleShowApp app) {
            this.app = app;
        }

        @Override
        public final boolean onMenuItemClick(MenuItem item) {
            return onMenuItemClick(!app.isCameraMuted(), item);
        }

        public abstract boolean onMenuItemClick(boolean state, MenuItem item);
    }

    public class VideoAdapter extends BaseAdapter {
        private final List<VideoItem> mItems = new ArrayList<VideoItem>();
        private final LayoutInflater mInflater;

        private class ViewHolder {
        	VideoRender videoPanel;
        	TextView displayNameTextView;
        	TextView noVideoMessage;
        	ImageView avatarImageView;
        }
        
        public VideoAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return mItems.size();
        }

        @Override
        public VideoItem getItem(int i) {
            return mItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public View getView(final int position, View convertView, ViewGroup viewGroup) {

            try {
            	ViewHolder viewHolder = null;
            	View v = convertView;

                VideoItem item = getItem(position);

                VideoRender panel = item != null ? item.getVideo() : null;
                VideoRender cached = (VideoPanel) (v != null ? v.getTag(R.id.video_panel_view) : null);
                boolean must_process_new_video_panel = panel == null || cached == null ||  !panel.equals(cached);

                if (item != null) {
                    LogSdk.d(TAG, "VideoAdapter => video (getView)  item = " + (item.getUserId() == null ? "preview " : item.getUserId()) + ", at position = " + position + ", video panel need to be updated ? " + (must_process_new_video_panel ? "Yes" : "No"));
                } else
                    LogSdk.d(TAG, "VideoAdapter => video (getView)  item = not exist for position = " + position + ", video panel need to be updated ? " + (must_process_new_video_panel ? "Yes" : "No"));

                if (must_process_new_video_panel)
                {
                    if (v == null) {
                    	viewHolder = new ViewHolder();
                    	
                    	v = mInflater.inflate(R.layout.video_grid_item, viewGroup, false);

                        String useCustomRenderValue = settings().get(ApplicationSettings.UseCustomRender);
                        if (useCustomRenderValue != null && Boolean.valueOf(useCustomRenderValue)) {
                            viewHolder.videoPanel = (CustomVideoPanel) v.findViewById(R.id.custom_panel_view);
                        } else {
                            viewHolder.videoPanel = (VideoPanel) v.findViewById(R.id.video_panel_view);
                        }
                        LogSdk.d(TAG, "VideoAdapter => video (view  null)  video = " + viewHolder.videoPanel.hashCode());
                        ((View)viewHolder.videoPanel).setTag(R.layout.video_grid_item, v);

                        viewHolder.displayNameTextView = (TextView) v.findViewById(R.id.display_name_text_view);
                        viewHolder.avatarImageView = (ImageView) v.findViewById(R.id.avatar_image_view);
                        viewHolder.noVideoMessage = (TextView) v.findViewById(R.id.no_video_message);

                        v.setTag(viewHolder);
                        
                        if (item.getVideo() == null) {
                            item.setVideo(viewHolder.videoPanel);
                            app().bindVideoPanel(item.getUserId(), viewHolder.videoPanel);
                            ((View)viewHolder.videoPanel).setVisibility(View.VISIBLE);
                        }
                        
                    } else {
                    	viewHolder = (ViewHolder) v.getTag();
                    	
                        if (item.getVideo() == null) {
                            item.setVideo(viewHolder.videoPanel);
                            app().bindVideoPanel(item.getUserId(), viewHolder.videoPanel);
                            if (((View)fullScreenRemoteview).getVisibility() != View.VISIBLE) {
                                ((View) viewHolder.videoPanel).setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                int[] paddings = getVideoWindowPaddings();

                int width = getDisplaySize().x / 2;
                int height = (getDisplaySize().y - (paddings[0] + paddings[1])) / 2 - (v.getPaddingTop() * 3);
                if (item.isFullScreen()) {
                    width = getDisplaySize().x;
                    height = getDisplaySize().y - (paddings[0] + paddings[1]);
                } else {
                    if (isPreviewFullScreen()) {
                    	v.setVisibility(View.INVISIBLE);
                        ((View)item.getVideo()).setVisibility(View.INVISIBLE);
                    } else if (((View)fullScreenRemoteview).getVisibility() == View.INVISIBLE && v.getVisibility() == View.INVISIBLE) {
                    	v.setVisibility(View.VISIBLE);
                        ((View)item.getVideo()).setVisibility(View.VISIBLE);
                    } else if (((View)fullScreenRemoteview).getVisibility() == View.VISIBLE && !item.getUserId().isEmpty()) {
                        ((View)item.getVideo()).setVisibility(View.INVISIBLE);
                        v.setVisibility(View.INVISIBLE);
                    }
                }
                
                boolean isMuted = app().getMuted().get(item.getUserId()) == null ? false :
                        app().getMuted().get(item.getUserId());
                if (isMuted) {
                    item.showAvatar();
                }

                ((View)viewHolder.videoPanel).setTag(new Point(width, height));
                
                viewHolder.displayNameTextView.setText(item.getUserData());

                if (item.isAvatarVisible()) {
                    VideoItem videoItem = (VideoItem) ((View)fullScreenRemoteview).getTag(R.id.video_panel_view);
                    if (videoItem != null && videoItem.getUserId().equals(item.getUserId()) &&
                            ((View)fullScreenRemoteview).getVisibility() == View.VISIBLE) {
                        fullScreenAvatar.setVisibility(View.VISIBLE);
                    } else {
                        ViewGroup.LayoutParams layoutParams = viewHolder.avatarImageView.getLayoutParams();
                        layoutParams.width = width;
                        layoutParams.height = height;
                        viewHolder.avatarImageView.setLayoutParams(layoutParams);
                	viewHolder.avatarImageView.setVisibility(View.VISIBLE);
                    }

                } else {
                	viewHolder.avatarImageView.setVisibility(View.INVISIBLE);
                }

                if (item.isErrorMessageVisible()) {
                	viewHolder.noVideoMessage.setText(getString(R.string.video_cannot_be_viewed));
                	viewHolder.noVideoMessage.setVisibility(View.VISIBLE);
                    viewHolder.avatarImageView.setVisibility(View.VISIBLE);
                } else {
                	viewHolder.noVideoMessage.setVisibility(View.GONE);
                    if (!item.isAvatarVisible()) {
                    	viewHolder.avatarImageView.setVisibility(View.INVISIBLE);
                    }
                }

                if (ChatsActivity.CHAT_TYPE.equals("audio")){
                    viewHolder.avatarImageView.setImageResource(R.mipmap.audio_chat_default);
                    viewHolder.avatarImageView.setVisibility(View.VISIBLE);
                }

                return v;
            }
            catch(Exception err){
                err.printStackTrace();
            }
            
            return convertView;
        }

        public boolean isPreviewFullScreen() {
            for (VideoItem item : mItems) {
                if (item.getUserId().isEmpty() && item.isFullScreen()) {
                    return true;
                }
            }

            return false;
        }

        public int getItemPosition(String userId) {
            for (int i = 0; i < mItems.size(); i++) {
                VideoItem item = mItems.get(i);
                if (item.getUserId().equals(userId)) {
                    return i;
                }
            }

            return -1;
        }

        public void addItem(VideoItem item) {
            LogSdk.d(TAG,"VideoAdapter => video (addItem)  "+item.getUserId());
            mItems.add(item);
            notifyDataSetChanged();
        }

        public void removeItem(String userId) {
            int itemPosition = getItemPosition(userId);
            VideoItem item = getItem(itemPosition);

            VideoRender video = item.getVideo();
            if (video != null) {
                View gridItem = (View)((View)video).getTag(R.layout.video_grid_item);
                ((View)video).setVisibility(View.INVISIBLE);
                gridItem.setVisibility(View.VISIBLE);
                app().unbindVideoPanel(item.getUserId(), video);
                item.setVideo(null);
            }

            mItems.remove(item);

            for (int i = itemPosition; i < mItems.size(); i++) {
                item = getItem(i);
                video = item.getVideo();
                item.setVideo(null);
                app().unbindVideoPanel(item.getUserId(), video);
            }

            notifyDataSetChanged();
        }

        public void removeAllItems() {
            Iterator<VideoItem> iter = mItems.iterator();
            while (iter.hasNext()) {
                VideoItem item = iter.next();
                VideoRender video = item.getVideo();
                if (video != null) {
                    ((View)video).setVisibility(View.INVISIBLE);
                    item.setVideo(null);
                }
                app().unbindVideoPanel(item.getUserId(), video);
            }
        }

        public void showAvatar(String userId) {
            try {
                VideoItem videoItem = (VideoItem) ((View)fullScreenRemoteview).getTag(R.id.video_panel_view);
                if (videoItem != null && videoItem.getUserId().equals(userId) &&
                        ((View)fullScreenRemoteview).getVisibility() == View.VISIBLE) {
                    fullScreenAvatar.setVisibility(View.VISIBLE);
                }

				for (VideoItem item : mItems) {
					if (item.getUserId().equals(userId)) {
						item.showAvatar();
						break;
					}
				}

				notifyDataSetChanged();
            } catch (Exception err) {
                LogSdk.e(TAG, "showAvatar " + err);
            }
        }

        public void showNoVideoMessage(String userId) {
            VideoItem videoItem = (VideoItem) ((View)fullScreenRemoteview).getTag(R.id.video_panel_view);
            if (videoItem != null && videoItem.getUserId().equals(userId) &&
                    ((View)fullScreenRemoteview).getVisibility() == View.VISIBLE) {
                //TODO: video cannot be viewed in full screen mode
            }

            for (VideoItem item : mItems) {
                if (item.getUserId().equals(userId)) {
                    item.showErrorMessage();
                    break;
                }
            }

            notifyDataSetChanged();
        }

        public void hideAvatar(String userId) {
            try {
                VideoItem videoItem = (VideoItem) ((View)fullScreenRemoteview).getTag(R.id.video_panel_view);
                if (videoItem != null && videoItem.getUserId().equals(userId) &&
                        ((View)fullScreenRemoteview).getVisibility() == View.VISIBLE) {
                    fullScreenAvatar.setVisibility(View.INVISIBLE);
                }

				for (VideoItem item : mItems) {
					if (item.getUserId().equals(userId)) {
						item.hideAvatar();
						break;
					}
				}

				notifyDataSetChanged();
            } catch (Exception err) {
                LogSdk.e(TAG, "hideAvatar = " + err);
            }
        }

        public void hideNoVideoMessage(String userId) {
            try {
                VideoItem videoItem = (VideoItem) ((View)fullScreenRemoteview).getTag(R.id.video_panel_view);
                if (videoItem != null && videoItem.getUserId().equals(userId) &&
                        ((View)fullScreenRemoteview).getVisibility() == View.VISIBLE) {
                    //fullScreenAvatar.setVisibility(View.GONE);
                }

                for (VideoItem item : mItems) {
                    if (item.getUserId().equals(userId)) {
                        item.hideErrorMessage();
                        break;
                    }
                }

                notifyDataSetChanged();
            } catch (Exception err) {
                LogSdk.e(TAG, "hideNoVideoMessage = " + err);
            }
        }

        public class VideoItem {
            private VideoRender video = null;
            private boolean isAvatarVisible = true;
            private boolean isErrorMessageVisible = false;
            private boolean isFullScreen = false;
            private final String userId;
            private final String userData;
            private VideoPanel.VideoRenderStateChangeListener listener = null;

            public VideoItem(String userId, String userData) {
                this.userId = userId;
                this.userData = userData;
                showAvatar();
            }

            public void setVideo(VideoRender video) {
                if (video != null) {
                    this.listener = new VideoPanel.VideoRenderStateChangeListener() {
                        public String toString() {
                            return userId.isEmpty() ? "preview" : userId;
                        }

                        @Override
                        public void onVideoRenderStart() {
                            try {
                                hideAvatar();
                                LogSdk.d(TAG, "VideoControllerWrap -> VideoPanel -> Application  onVideoRenderStop hideAvatar " + toString());
                                videoAdapter.notifyDataSetChanged();
                            } catch (Exception err) {
                                LogSdk.e(TAG, "onVideoRenderStart " + err);
                            }
                        }

                        @Override
                        public void onVideoRenderStop() {
                            try {
                                showAvatar();
                                LogSdk.d(TAG, "VideoControllerWrap -> VideoPanel -> Application  onVideoRenderStop showAvatar " + toString());
                                videoAdapter.notifyDataSetChanged();

                            } catch (Exception err) {
                                LogSdk.e(TAG, "onVideoRenderStop " + err);
                            }

                        }
                    };
                    setVideoRenderStateChangeListener(video, this.listener);
                }

                this.video = video;
            }

            public VideoRender getVideo() {
                return this.video;
            }

            public boolean isAvatarVisible() {
                return this.isAvatarVisible;
            }

            public boolean isErrorMessageVisible() {
                return isErrorMessageVisible;
            }

            public void showAvatar() {
                this.isAvatarVisible = true;
            }

            public void hideAvatar() {
                this.isAvatarVisible = false;
                this.isErrorMessageVisible = false;
            }

            public void enableListener() {
                setVideoRenderStateChangeListener(video, listener);
            }

            public void disableListener() {
                setVideoRenderStateChangeListener(video, null);
            }

            public void showErrorMessage() {
                this.isErrorMessageVisible = true;
            }

            public void hideErrorMessage() {
                this.isErrorMessageVisible = false;
            }

            public void setFullScreen(boolean isFullScreen) {
                this.isFullScreen = isFullScreen;
            }

            public boolean isFullScreen() {
                return this.isFullScreen;
            }

            public String getUserId() {
                return userId;
            }

            public String getUserData() {
                return userData;
            }
        }
    }

    public void setVideoRenderStateChangeListener(VideoRender video, VideoPanel.VideoRenderStateChangeListener listener) {
        if (video instanceof CustomVideoPanel) {
            ((CustomVideoPanel)video).setVideoRenderStateChangeListener(listener);
        } else if (video instanceof VideoPanel) {
            ((VideoPanel)video).setVideoRenderStateChangeListener(listener);
        }
    }

    public boolean onBackPressed() {
        app().onEndOfCall();
        app().sendEndOfCall();

        int count = getFragmentManager().getBackStackEntryCount();
        String name = getFragmentManager().getBackStackEntryAt(count - 2).getName();
        getFragmentManager().popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if(timer != null){
            timer.cancel();
        }
        if(resendTimer != null){
            resendTimer.cancel();
        }
        if (!chatEnded) {
            new AppUtils.EndChatAsync(timeLeftMiliSecs).execute();
            chatEnded = true;
            getActivity().finish();
        }

        return false;
    }

    @Override
    public void onNetworkSignalStrength(int level) {
        SignalBar signalBar = (SignalBar) signalStrengthMenuItem.getActionView();
        signalBar.setLevel(level);
    }

    @Override
    public void onNetworkSecurityState(boolean isSecure)
    {
        if (isSecure) {
            secureNetworkMenuItem.setIcon(getResources().getDrawable(R.drawable.menu_ic_lock));
        } else {
            secureNetworkMenuItem.setIcon(getResources().getDrawable(R.drawable.menu_ic_lock_unlock));
        }
    }

    public void muteVideo(String userId) {
    	videoAdapter.showAvatar(userId);
    }

    public void unmuteVideo(String userId) {
        videoAdapter.hideAvatar(userId);
    }

    private void notifyDoctor(String myUrl) throws IOException {
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
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public class NotifyDoctorAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences idPrefs = getActivity().getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
            String userId = idPrefs.getString(LoginActivity.USER_ID, "");
            String url = AppUtils.API_ROOT+"/api/notifydoctor/"+userId+"/"
                    +ChatsActivity.CHAT_CATEGORY_ID+"/"+ ChatsActivity.SESSION_ID+"/"
                    +ChatsActivity.CHAT_TYPE.toLowerCase()+"/"+ChatsActivity.CHAT_ID;
            Log.i(DEBUG_TAG, "URL: "+url);
            try {
                notifyDoctor(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
