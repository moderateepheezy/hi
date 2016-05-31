package com.exolvetechnologies.hidoctor.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;

import com.oovoo.core.LoggerListener;
import com.oovoo.core.Utils.LogSdk;
import com.oovoo.core.media.ooVooCamera;
import com.oovoo.core.sdk_error;
import com.oovoo.sdk.api.GLPerformanceUtils;
import com.oovoo.sdk.api.Message;
import com.oovoo.sdk.api.ooVooClient;
import com.oovoo.sdk.interfaces.AVChatListener;
import com.oovoo.sdk.interfaces.AudioControllerListener;
import com.oovoo.sdk.interfaces.AudioRoute;
import com.oovoo.sdk.interfaces.AudioRouteController;
import com.oovoo.sdk.interfaces.Effect;
import com.oovoo.sdk.interfaces.Messaging;
import com.oovoo.sdk.interfaces.MessagingListener;
import com.oovoo.sdk.interfaces.Participant;
import com.oovoo.sdk.interfaces.VideoController;
import com.oovoo.sdk.interfaces.VideoController.ResolutionLevel;
import com.oovoo.sdk.interfaces.VideoControllerListener;
import com.oovoo.sdk.interfaces.VideoDevice;
import com.oovoo.sdk.interfaces.VideoRender;
import com.oovoo.sdk.interfaces.ooVooSdkResult;
import com.oovoo.sdk.interfaces.ooVooSdkResultListener;
import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.call.CNMessage;
import com.exolvetechnologies.hidoctor.call.PNMessage;
import com.exolvetechnologies.hidoctor.ui.CustomVideoPanel;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

//import com.oovoo.sdk.plugins.yap.PerfVerifier;
//import com.oovoo.sdk.plugin.ooVooPluginFactory ;
//import com.oovoo.sdk.plugins.yap.YapFactory;

//import com.oovoo.sdk.plugins.affdex.AffdexDetectorListener;
//import com.oovoo.sdk.plugins.affdex.AffdexPluginFactory;
//import com.oovoo.sdk.plugins.affdex.AffdexPluginMetric;
//import com.oovoo.sdk.plugins.affdex.AffdexPluginSettings;

public class ooVooSdkSampleShowApp extends Application implements VideoControllerListener, LoggerListener,
		AVChatListener, AudioControllerListener, MessagingListener//, AffdexDetectorListener
{
	public static final String TAG = "ooVooSdkSampleShowApp";


	public enum Operation {
		Authorized, LoggedIn, Processing, AVChatJoined, AVChatRoom, AVChatCall, PushNotification, AVChatDisconnected, Error;
		private String description = "";
		private Operation forOperation = null;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Operation forOperation() {
			return forOperation;
		}

		public void setForOperation(Operation state) {
			forOperation = state;
		}
	}

	public ooVooClient sdk = null;
	private Context mContext = null;
	private Handler operation_handler = null;
	private ArrayList<OperationChangeListener> listeners = new ArrayList<OperationChangeListener>();
	private Operation state = null;
	private boolean m_iscameraopened = false;
	private boolean m_previewopened = false;
	private boolean m_isaudioinited = false;
	private ArrayList<ParticipantsListener> m_participantListeners = new ArrayList<ParticipantsListener>();
	private Hashtable<String, String> participants = new Hashtable<String, String>();
	private ApplicationSettings settings = null;
	private CallControllerListener controllerListener = null;
	private NetworkListener networkListener = null;
	private ArrayList<CallNegotiationListener> callNegotiationListeners = new ArrayList<CallNegotiationListener>();
	private String conferenceId = UUID.randomUUID().toString();
	private String uniqueId = UUID.randomUUID().toString();
	private boolean isCallNegotiation = false;
	private boolean isInConference = false;


	private Map<String, Boolean> muted = new HashMap<String, Boolean>();

	@Override
	public void onCreate() {
		super.onCreate();
		try {

			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				public void uncaughtException(Thread t, Throwable e) {
					LogSdk.e(TAG, "UncaughtExceptionHandler threade = " + t + ", error " + e);
					e.printStackTrace();
				}
			});

			if (!ooVooClient.isDeviceSupported()) {
				return;
			}

			settings = new ApplicationSettings(this);

			ooVooClient.setLogger(this, LogLevel.fromString(getSettings().get(ApplicationSettings.LogLevelKey)));
			ooVooClient.setContext(this);
			sdk = ooVooClient.sharedInstance();
			sdk.getAVChat().setListener(this);
			sdk.getAVChat().getVideoController().setListener(this);
			sdk.getAVChat().getAudioController().setListener(this);
			sdk.getAVChat().setSslVerifyPeer(true);
			sdk.getMessaging().setListener(this);
			//sdk.getAVChat().registerPlugin(new ooVooPluginFactory());
//			PerfVerifier verify = new PerfVerifier();
//			verify.glTestRun();
//			long vga_average_read = verify.getPerfValue("vga");
//			LogSdk.d( TAG, "perf test result vga: " + vga_average_read );
//			if(vga_average_read < verify.getEnableThreshold())
//			{
//				LogSdk.d( TAG, "yap support up to vga");
//			}
//			else 
//			{
//				long cif_average_read = verify.getPerfValue("cif");
//				LogSdk.d( TAG, "perf test result vga: " + cif_average_read );
//				if(cif_average_read < verify.getEnableThreshold())
//				{
//					LogSdk.d( TAG, "yap support up to cif");
//				}
//				else
//				{
//					LogSdk.d( TAG, "do not suppor yap avatar");
//				}
//			}

			//sdk.getAVChat().registerPlugin(new YapFactory("/storage/sdcard0/oosdksampleshow/avatar"));


//			AffdexPluginSettings affdexSettings = new AffdexPluginSettings();
//			affdexSettings.setDetectAllExpressions(true);
//			affdexSettings.setDetectAllEmotions(true);
//			File extStore = Environment.getExternalStorageDirectory();
//			affdexSettings.setClassifierPath(extStore + "/data");
//			sdk.getAVChat().registerPlugin(new AffdexPluginFactory(affdexSettings, this));

			AudioRouteController audioController = sdk.getAVChat().getAudioController().getAudioRouteController();
			LogSdk.d(TAG, "Audio controller " + audioController);

		} catch (Exception e) {
			e.printStackTrace();
			sdk = null;
		}
		operation_handler = new Handler();
	}

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context context) {
		mContext = context;
	}

	public ApplicationSettings getSettings() {
		return settings;
	}

	public void addOperationChangeListener(OperationChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeOperationChangeListener(OperationChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void setNetworkListener(NetworkListener listener) {
		networkListener = listener;
	}

	protected synchronized void fireApplicationStateEvent(final Operation state) {
		fireApplicationStateEvent(state, 0);
	}

	protected synchronized void fireApplicationStateEvent(final Operation state, String description) {
		state.setDescription(description);
		fireApplicationStateEvent(state, 0);
	}

	protected synchronized void fireApplicationStateEvent(final Operation state, Operation forOperation,
														  String description) {
		state.setForOperation(forOperation);
		state.setDescription(description);
		fireApplicationStateEvent(state, 0);
	}

	protected synchronized void fireApplicationStateEvent(final Operation state, String description, long delayMillis) {
		state.setDescription(description);
		fireApplicationStateEvent(state, delayMillis);
	}

	protected synchronized void fireApplicationStateEvent(final Operation state, final Runnable excecuteAfter) {
		fireApplicationStateEvent(state, excecuteAfter, 0);
	}

	protected synchronized void fireApplicationStateEvent(final Operation state, final Runnable excecuteAfter,
														  long delayMillis) {
		operation_handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				synchronized (listeners) {
					for (OperationChangeListener listener : listeners) {
						listener.onOperationChange(state);
					}
				}
				if (excecuteAfter != null) {
					operation_handler.post(excecuteAfter);
				}
			}
		}, delayMillis);
	}

	protected synchronized void fireApplicationStateEvent(final Operation new_state, long delayMillis) {
		this.state = new_state;
		operation_handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				synchronized (listeners) {
					for (OperationChangeListener listener : listeners) {
						listener.onOperationChange(new_state);
					}
				}
			}
		}, delayMillis);

	}

	public Operation getState() {
		return state;
	}

	/***
	 * Called when main activity created
	 */
	public void onMainActivityCreated() {

		reautorize();
	}

	public void reautorize() {
		fireApplicationStateEvent(Operation.Processing, "Authorizing");
		autorize();
	}

	private void autorize() {
		try {
			String APP_TOKEN = settings.get(ApplicationSettings.Token);
			if (APP_TOKEN == null || APP_TOKEN.trim().isEmpty()) {
				fireApplicationStateEvent(Operation.Error, Operation.Authorized, "App Token probably invalid or might be empty.\n\nGet your App Token at\nhttp://developer.oovoo.com.\nSet TOKEN constant in code.");
				return;
			}

			sdk.authorizeClient(APP_TOKEN, new ooVooSdkResultListener() {
				@Override
				public void onResult(ooVooSdkResult autorize_result) {
					if (autorize_result.getResult() == sdk_error.OK) {
						fireApplicationStateEvent(Operation.Authorized);
						return;
					}
					fireApplicationStateEvent(Operation.Error, Operation.Authorized, autorize_result.getDescription());
				}
			});
		} catch (Exception e) {
			fireApplicationStateEvent(Operation.Error, Operation.Authorized, e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public static interface OperationChangeListener {
		public void onOperationChange(Operation state);
	}

	public synchronized void login(final String username, String displayName) {
		m_iscameraopened = false;
		m_previewopened = false;
		fireApplicationStateEvent(Operation.Processing, "Log in");

		settings.put(ApplicationSettings.AvsSessionDisplayName, displayName);

		sdk.getAccount().login(username, new ooVooSdkResultListener() {
			@Override
			public void onResult(ooVooSdkResult result) {

				if (result.getResult() == sdk_error.OK) {

					settings.put(ApplicationSettings.Username, username);
					settings.save();
					fireApplicationStateEvent(Operation.LoggedIn);
					LogSdk.d(TAG,"Application -> messaging service is "+(sdk.getMessaging().isConnected() ? "connected" : "not connected, will try to connect"));
					if(!sdk.getMessaging().isConnected()){
						sdk.getMessaging().connect();
						LogSdk.d(TAG, "Application -> messaging service start connecting");
					}
					return;
				}
				fireApplicationStateEvent(Operation.Error, Operation.LoggedIn, result.getDescription());
			}
		});
	}


	public synchronized void openPreview() {
		if (!m_iscameraopened) {
			sdk.getAVChat().getVideoController().openCamera();
			if (!m_previewopened)
				sdk.getAVChat().getVideoController().openPreview();
			return;
		} else {
			sdk.getAVChat().getVideoController().openPreview();
		}
	}

	@Override
	public void onCameraChanged(String arg0, sdk_error arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraStateChanged(ooVooCamera.ooVooCameraState state, String deviceId, int width, int height,
									 int fps, sdk_error error) {
		LogSdk.d(TAG, "ooVooCamera -> onCameraStateChanged [state = " + state + ". error = " + error + ", size = " +
				width + "x" + height + "]");
		if (state == ooVooCamera.ooVooCameraState.CameraOpened) {
			sdk.getAVChat().getVideoController().openPreview();
			sdk.getAVChat().getVideoController().startTransmit();
			m_iscameraopened = true;
		} else if (state == ooVooCamera.ooVooCameraState.CameraClosed) {
			sdk.getAVChat().getVideoController().closePreview();
			m_iscameraopened = false;
		}


	}

	@Override
	public void onRemoteVideoStateChanged(String uid, RemoteVideoState state, int width, int height, sdk_error error) {

		LogSdk.d(TAG, "ooVooCamera ->onRemoteVideoStateChanged [uid = " + uid + ". RemoteVideoState = " + state + "]");
		switch (state) {
			case RVS_Started:
			case RVS_Resumed:

				muted.put(uid, false);
				break;
			case RVS_Stopped:

				muted.put(uid, true);
				break;
			case RVS_Paused:

				muted.put(uid, true);
				break;
		}

		if (m_participantListeners.size() > 0) {
			Iterator<ParticipantsListener> iter = m_participantListeners.iterator();
			while (iter.hasNext()) {
				ParticipantsListener listener = iter.next();
				listener.onRemoteVideoStateChanged(uid, state, error);
			}
		}
	}

	@Override
	public void onTransmitStateChanged(boolean arg0, sdk_error arg1) {
		if (m_participantListeners.size() > 0) {
			Iterator<ParticipantsListener> iter = m_participantListeners.iterator();
			while (iter.hasNext()) {
				ParticipantsListener listener = iter.next();
				listener.onTransmitStateChanged(arg0, arg1);
			}
		}
	}

	@Override
	public void onVideoPreviewStateChanged(boolean arg0, sdk_error arg1) {
		LogSdk.d(TAG, "ooVooCamera ->onVideoPreviewStateChanged [is_opened = " + arg0 + ". error = " + arg1 + "]");
		m_previewopened = arg0;

	}

	@Override
	public void OnLog(LogLevel level, String tag, String message) {
		switch (level) {
			case None:
				break;
			case Debug:
				Log.d(tag, "[" + level.toString() + "] " + message);
				break;
			case Fatal:
				Log.wtf(tag, "[" + level.toString() + "] " + message);
				break;
			case Info:
				Log.i(tag, "[" + level.toString() + "] " + message);
				break;
			case Trace:
				Log.v(tag, "[" + level.toString() + "] " + message);
				break;
			case Warning:
				Log.w(tag, "[" + level.toString() + "] " + message);
				break;
			case Error:
			default:
				Log.e(TAG, "[" + level.toString() + "] " + message);
				break;
		}
	}

	private VideoRender render_preview = null;

	public void bindPreviewPanel(VideoRender render) {
		if (render instanceof CustomVideoPanel) {
			((CustomVideoPanel)render).setPreview(true);
		}
		unbindPreviewPanel();
		render_preview = render;
		try {
			sdk.getAVChat().getVideoController().bindRender(null, render);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void unbindPreviewPanel() {
		if (render_preview != null) {
			try {
				sdk.getAVChat().getVideoController().unbindRender(null, render_preview);
				render_preview = null;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void registerRemote(String id) {
		sdk.getAVChat().getVideoController().registerRemote(id);
	}

	public void unregisterRemote(String id) {
		sdk.getAVChat().getVideoController().unregisterRemote(id);
	}

	public void bindVideoPanel(String id, VideoRender render) {
		if (id.isEmpty()) {
			bindPreviewPanel(render);

			return;
		}
		try {
			sdk.getAVChat().getVideoController().bindRender(id, render);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sdk.getAVChat().getVideoController().registerRemote(id);
	}

	public void unbindVideoPanel(String id, VideoRender render) {
		if (id.isEmpty()) {
			unbindPreviewPanel();
			return;
		}
		sdk.getAVChat().getVideoController().unregisterRemote(id);
		try {
			sdk.getAVChat().getVideoController().unbindRender(id, render);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void join(final String session_id, boolean isCallNegotiation) {
		this.isCallNegotiation = isCallNegotiation;

		if (isCallNegotiation) {
			settings.put(ApplicationSettings.RandomAvsSessionId, session_id);
		} else {
			settings.put(ApplicationSettings.AvsSessionId, session_id);
		}

		participants.clear();

		fireApplicationStateEvent(Operation.Processing, Operation.AVChatJoined, "Joining");
	}

	public void room() {
		fireApplicationStateEvent(Operation.AVChatRoom, Operation.AVChatRoom, "Room");
	}

	public void makeCall() {
		fireApplicationStateEvent(Operation.AVChatCall, Operation.AVChatCall, "Make call");
	}

	public void pushNotification() {
		fireApplicationStateEvent(Operation.PushNotification, Operation.PushNotification, "Push notification");
	}

	public void onProcessingStarted() {
		if (Operation.Processing.forOperation() != null) {
			switch (Operation.Processing.forOperation()) {
				case AVChatJoined:

				{
					final String session_id = this.isCallNegotiation ? settings.get(ApplicationSettings.RandomAvsSessionId) :
							settings.get(ApplicationSettings.AvsSessionId);
					final String session_dn = settings.get(ApplicationSettings.AvsSessionDisplayName);

					sdk.getAVChat().getAudioController().initAudio(new ooVooSdkResultListener() {
						@Override
						public void onResult(ooVooSdkResult init_audio_result) {
							LogSdk.d(TAG, "Application - > init audio completion " + init_audio_result);
							if (init_audio_result.getResult() == sdk_error.OK) {
								m_isaudioinited = true;
							}
						}
					});

					LogSdk.d(TAG, "Application - > onProcessingStarted start join conference id = " + session_id + ", display name = " + session_dn);

					sdk.getAVChat().join(session_id, session_dn);
				}
				break;


				default:
					break;
			}
			Operation.Processing.setForOperation(null);
		}
	}

	@Override
	public void onConferenceError(sdk_error error) {
		LogSdk.d(TAG, "Application - > onConferenceError  error " + error);
	}

	@Override
	public void onConferenceStateChanged(ConferenceState avchat_state, sdk_error error) {
		LogSdk.d(TAG, "Application - > onConferenceStateChanged " + avchat_state + ", error " + error);
		if (avchat_state == ConferenceState.Joined && error == sdk_error.OK) {
			isInConference = true;
			settings.save();
			fireApplicationStateEvent(Operation.AVChatJoined);

		} else if (avchat_state == ConferenceState.Joined && error != sdk_error.OK) {
			fireApplicationStateEvent(Operation.Error, Operation.AVChatJoined, sdk_error.getErrorString(error));

		} else if (avchat_state == ConferenceState.Disconnected) {
			isInConference = false;
			fireApplicationStateEvent(Operation.AVChatDisconnected);
		}

	}

	@Override
	public void onParticipantJoined(Participant participant, String displayName) {
		muted.put(participant.getID(), false);
		if (m_participantListeners.size() > 0) {
			Iterator<ParticipantsListener> iter = m_participantListeners.iterator();
			while (iter.hasNext()) {
				ParticipantsListener listener = iter.next();
				listener.onParticipantJoined(participant.getID(), displayName);
				LogSdk.d(TAG, "Application - > onParticipantJoined " + participant.getID() + ", "
						+ displayName + ", m_participantListener = " + listener);
			}
		}
		synchronized (participants) {
			participants.put(participant.getID(), displayName);
		}

	}

	@Override
	public void onParticipantLeft(Participant participant) {
		LogSdk.d(TAG, "Application - > onParticipantLeft " + participant.getID());
		if (m_participantListeners.size() > 0) {
			Iterator<ParticipantsListener> iter = m_participantListeners.iterator();
			while (iter.hasNext()) {
				ParticipantsListener listener = iter.next();
				listener.onParticipantLeft(participant.getID());
			}
		}
		synchronized (participants) {
			participants.remove(participant.getID());
			muted.remove(participant.getID());
		}
	}

	@Override
	public void onReceiveData(String arg0, byte[] arg1) {
		LogSdk.d(TAG, "Application - > onReceiveData " + arg0 + ", " + arg1);

	}

	public void checkGL() {
		(new Thread() {
			public void run() {
				try {
					LogSdk.d(TAG, "Application - > checkGL -> ");
					long value_vga = GLPerformanceUtils.getPerfValue(GLPerformanceUtils.VGA_PERFORMANCE);
					LogSdk.d(TAG, "Application - > checkGL vga = " + value_vga);
					if (value_vga > GLPerformanceUtils.getEnableThreshold()) {
						long value_cif = GLPerformanceUtils.getPerfValue(GLPerformanceUtils.CIF_PERFORMANCE);
						LogSdk.d(TAG, "Application - > checkGL cif = " + value_cif);
						if (value_cif > GLPerformanceUtils.getEnableThreshold()) {
							LogSdk.d(TAG, "enable none");
						} else {
							LogSdk.d(TAG, "enable cif only");
						}
					} else {
						LogSdk.d(TAG, "enable cif and vga");
					}
					LogSdk.d(TAG, "Application <- checkGL <-");
				} catch (Exception err) {
					LogSdk.e(TAG, "Application - > checkGL " + err);
				}
			}
		}).start();

	}

	public void logout() {

		if (sdk != null) {
			sdk.getAccount().logout();
			LogSdk.d(TAG, "Application - > logout ");
		}
		System.gc();
	}

	public void addParticipantListener(ParticipantsListener listener) {
		if (listener == null || m_participantListeners.contains(listener))
			return;

		synchronized (participants) {
			m_participantListeners.add(listener);
			Enumeration<String> en = participants.keys();
			while (en.hasMoreElements()) {
				String key = en.nextElement();
				String displayname = participants.get(key);
				listener.onParticipantJoined(key, displayname);
			}
		}
	}

	public void removeParticipantListener(ParticipantsListener listener) {
		m_participantListeners.remove(listener);
	}

	public Hashtable<String, String> getParticipants() {
		return participants;
	}

	public static interface ParticipantsListener {
		public void onParticipantJoined(String userId, String userData);

		public void onParticipantLeft(String userId);

		public void onRemoteVideoStateChanged(String userId, RemoteVideoState state, sdk_error error);

		public void onTransmitStateChanged(boolean state, sdk_error err);
	}

	@Override
	public void onAudioReceiveStateChanged(boolean arg0, sdk_error arg1) {
		try {
			LogSdk.d(TAG, "onAudioReceiveStateChanged " + (!arg0) + ", error " + arg1);
			if (controllerListener != null) {
				controllerListener.updateController();
			}
		} catch (Exception err) {
			LogSdk.d(TAG, "onAudioReceiveStateChanged err " + err);
		}
	}

	@Override
	public void onAudioTransmitStateChanged(boolean arg0, sdk_error arg1) {
		try {
			LogSdk.d(TAG, "onAudioTransmitStateChanged " + (!arg0) + ", error " + arg1);
			if (controllerListener != null) {
				controllerListener.updateController();
			}
		} catch (Exception err) {
			LogSdk.d(TAG, "onAudioTransmitStateChanged err " + err);
		}
	}

	public void onMicrophoneClick() {
		boolean state = sdk.getAVChat().getAudioController().isRecordMuted();
		LogSdk.d(TAG, "Change record state from " + state + ", to " + (!state));
		sdk.getAVChat().getAudioController().setRecordMuted(!state);
	}

	public void onSpeakerClick() {
		boolean state = sdk.getAVChat().getAudioController().isPlaybackMuted();
		LogSdk.d(TAG, "Change playback state from " + (state) + ", to " + (!state));
		sdk.getAVChat().getAudioController().setPlaybackMuted(!state);
	}

	public void switchCamera(VideoDevice camera) {
		sdk.getAVChat().getVideoController().setConfig(VideoController.VideoConfigKey.kVideoCfgCaptureDeviceId, camera.getID());
	}

	public void selectCamera(String name) {
		ArrayList<VideoDevice> cameras = getVideoCameras();
		for (VideoDevice camera : cameras) {
			if (camera.toString().equals(name) && !getActiveCamera().getID().equalsIgnoreCase(camera.getID())) {
				switchCamera(camera);
				break;
			}
		}
	}

	public VideoDevice getActiveCamera() {
		String cameraId = sdk.getAVChat().getVideoController().getConfig(VideoController.VideoConfigKey.kVideoCfgCaptureDeviceId);

		ArrayList<VideoDevice> cameras = getVideoCameras();
		for (VideoDevice camera : cameras) {
			if (camera.getID().equalsIgnoreCase(cameraId)) {
				return camera;
			}
		}

		return null;
	}

	public ArrayList<Effect> getVideoFilters() {
		return sdk.getAVChat().getVideoController().getEffectList();
	}

	public Effect getActiveEffect() {
		String effectId = sdk.getAVChat().getVideoController().getConfig(VideoController.VideoConfigKey.kVideoCfgEffectId);

		ArrayList<Effect> effects = getVideoFilters();
		for (Effect effect : effects) {
			if (effect.getID().equalsIgnoreCase(effectId)) {
				changeVideoEffect(effect);
				return effect;
			}
		}

		return null;
	}

	public ArrayList<VideoDevice> getVideoCameras() {
		return sdk.getAVChat().getVideoController().getDeviceList();
	}

	public void onEndOfCall() {
		selectVideoEffect("original");
		changeResolution(VideoController.ResolutionLevel.ResolutionLevelMed);
		releaseAVChat();
		sdk.getAVChat().leave();
		if (m_isaudioinited) {
			sdk.getAVChat().getAudioController().uninitAudio(new ooVooSdkResultListener() {
				@Override
				public void onResult(ooVooSdkResult uninit_audio_result) {
					LogSdk.d(TAG, "uninitAudio, result = " + uninit_audio_result.getResult());
					if (uninit_audio_result.getResult() == sdk_error.OK) {
						m_isaudioinited = false;
					}
				}
			});
		}
	}

	public static interface CallControllerListener {
		public void updateController();
	}

	public void setControllerListener(CallControllerListener controllerListener) {
		this.controllerListener = controllerListener;
	}

	public void setMicMuted(boolean muted) {
		sdk.getAVChat().getAudioController().setRecordMuted(muted);
	}

	public boolean isMicMuted() {
		return sdk.getAVChat().getAudioController().isRecordMuted();
	}

	public void setSpeakerMuted(boolean muted) {
		sdk.getAVChat().getAudioController().setPlaybackMuted(muted);
	}

	public boolean isSpeakerMuted() {
		return sdk.getAVChat().getAudioController().isPlaybackMuted();
	}

	@Override
	public void onMicrophoneStateChange(boolean arg0, sdk_error arg1) {
		if (controllerListener != null) {
			controllerListener.updateController();
		}
	}

	@Override
	public void onSpeakerStateChange(boolean arg0, sdk_error arg1) {
		if (controllerListener != null) {
			controllerListener.updateController();
		}
	}

	public AudioRouteController getAudioRouteController() {
		return sdk.getAVChat().getAudioController().getAudioRouteController();
	}

	public void performOperation(Runnable operationOnResume) {
		if (operationOnResume != null) {
			operation_handler.postDelayed(operationOnResume, 100);
		}
	}

	public boolean isCameraMuted() {
		boolean tr = sdk.getAVChat().getVideoController().isTransmited();
		LogSdk.d(TAG, "isTransmited = " + tr + ", will return as " + !tr);
		return !sdk.getAVChat().getVideoController().isTransmited();
	}

	public void muteCamera(boolean state) {
		if (state) {
			sdk.getAVChat().getVideoController().closeCamera();
			sdk.getAVChat().getVideoController().stopTransmit();
		} else {
			sdk.getAVChat().getVideoController().openCamera();
			sdk.getAVChat().getVideoController().startTransmit();
		}
	}

	public void changeResolution(ResolutionLevel resolution) {
		try {
			boolean isResolutionSupported = getActiveCamera().isResolutionSupported(resolution);
			LogSdk.d(TAG, "VideoControler -> changeResolution " + resolution);

			if (isResolutionSupported) {
				sdk.getAVChat().getVideoController().setConfig(VideoController.VideoConfigKey.kVideoCfgResolution, Integer.toString(resolution.ordinal()));
			} else {
				LogSdk.e(TAG, "VideoController -> resolution is not supported " + resolution);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public ResolutionLevel getActiveResolution() {
		return sdk.getAVChat().getVideoController().getActiveResolution();
	}

	public void changeVideoEffect(Effect effect) {
		try {
			sdk.getAVChat().getVideoController().setConfig(VideoController.VideoConfigKey.kVideoCfgEffectId, effect.getID());
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public void selectVideoEffect(String name) {
		ArrayList<Effect> effects = getVideoFilters();
		for (Effect effect : effects) {
			if (effect.getName().equalsIgnoreCase(name)) {
				changeVideoEffect(effect);
				break;
			}
		}
	}

	public void changeRoute(AudioRoute route) {
		try {
			sdk.getAVChat().getAudioController().getAudioRouteController().setRoute(route);
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public ArrayList<ResolutionLevel> getAvailableResolutions() {
		return sdk.getAVChat().getAvailableResolutions();
	}

	@Override
	public void onNetworkReliability(int level) {
		LogSdk.d(TAG, "onNetworkReliability level = " + level + "networkListener = " + (networkListener == null ? "null" : "OK"));
		if (networkListener != null) {
			networkListener.onNetworkSignalStrength(level);
		}
	}

	@Override
	public void onSecurityState(boolean isSecure) {
		LogSdk.d(TAG, "onSecurityState isSecure = " + isSecure);

		settings.put(ApplicationSettings.SecurityState, Boolean.toString(isSecure));

		if (networkListener != null) {
			networkListener.onNetworkSecurityState(isSecure);
		}
	}

	public void releaseAVChat() {
		try {
			unbindPreviewPanel();
			sdk.getAVChat().getVideoController().closeCamera();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public String getSdkVersion() {
		return ooVooClient.getSdkVersion();
	}

	public static interface NetworkListener {
		public void onNetworkSignalStrength(int level);

		public void onNetworkSecurityState(boolean isSecure);
	}

	public boolean isTablet() {
		return ooVooClient.isTablet();
	}


	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				return true;
			}
		} catch (Exception e) {
			LogSdk.e(TAG, e.toString());

			// probably connectivity problem so we will return false
		}
		return false;
	}


	public int getDeviceDefaultOrientation() {

		if (ooVooClient.isTablet()) {
			return Configuration.ORIENTATION_LANDSCAPE;
		} else {
			return Configuration.ORIENTATION_PORTRAIT;
		}
	}

	public Point getDisplaySize() {
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}

	public void setLogLevel(String logLevel) {
		ooVooClient.setLogLevel(LogLevel.fromString(logLevel));
	}

	public static interface MessageCompletionHandler {
		public void onHandle(boolean sent);
	}

	public static interface CallNegotiationListener {
		public void onMessageReceived(final CNMessage cnMessage);
	}

	public void addCallNegotiationListener(CallNegotiationListener listener) {
		callNegotiationListeners.add(listener);
	}

	public void removeCallNegotiationListener(CallNegotiationListener listener) {
		callNegotiationListeners.remove(listener);
	}

	@Override
	public void onMessageReceived(final Message message) {
		try {
			LogSdk.d(TAG, "onMessageReceived from: " + message.getFrom() + " body: " + message.getBody());

			sdk.getMessaging().sendAcknowledgement(Messaging.MessageAcknowledgeState.Delivered, message, new ooVooSdkResultListener() {
				@Override
				public void onResult(ooVooSdkResult ooVooSdkResult) {
					if (ooVooSdkResult.getResult() != sdk_error.OK) {
						LogSdk.e(TAG, "Error on sending delivered acknowledgement: " + ooVooSdkResult.getDescription());
					} else {
						LogSdk.d(TAG, "sendAcknowledgement sent delivered acknowledgement");
						sdk.getMessaging().sendAcknowledgement(Messaging.MessageAcknowledgeState.Read, message, new ooVooSdkResultListener() {
							@Override
							public void onResult(ooVooSdkResult ooVooSdkResult) {
								if (ooVooSdkResult.getResult() != sdk_error.OK) {
									LogSdk.e(TAG, "Error on sending read acknowledgement: " + ooVooSdkResult.getDescription());
								} else {
									LogSdk.d(TAG, "sendAcknowledgement sent read acknowledgement");
								}
							}
						});
					}
				}
			});

			CNMessage cnMessage = new CNMessage(message);
			for (CallNegotiationListener listener : callNegotiationListeners) {
				listener.onMessageReceived(cnMessage);
			}
		} catch (InstantiationException e) {
			LogSdk.e(TAG, "error on creation CNMessage: " + e.getMessage());
		}
	}

	@Override
	public void onConnectivityStateChange(Messaging.ConnectivityState var1, sdk_error var2, String var3){
		LogSdk.d(TAG, "onConnectivityStateChange state: " + state + " sdk_error: " + var2+", description "+var3);
	}

	@Override
	public void onMessageAcknowledgementReceived(Messaging.MessageAcknowledgeState state, String messageID) {

		LogSdk.d(TAG, "OnMessageAcknowledgementReceived state: " + state + " messageID: " + messageID);
	}

	public boolean sendCNMessage(String to, CNMessage.CNMessageType type, final MessageCompletionHandler completionHandler) {
		if (to == null) {
			LogSdk.d(TAG, "destination can not be empty!");
			return false;
		}

		String displayName = settings.get(ApplicationSettings.AvsSessionDisplayName);
		if (displayName == null || displayName.isEmpty()) {
			LogSdk.e(TAG, "Display name is empty!");
			return false;
		}

		try {
			final CNMessage cnMessage = new CNMessage(to, type, conferenceId, displayName, uniqueId);
			sdk.getMessaging().sendMessage(cnMessage, new ooVooSdkResultListener() {
				@Override
				public void onResult(ooVooSdkResult ooVooSdkResult) {
					if (completionHandler != null) {
						completionHandler.onHandle(ooVooSdkResult.getResult() == sdk_error.OK);
					}
					if (ooVooSdkResult.getResult() != sdk_error.OK) {
						LogSdk.e(TAG, "Error on send out CN message: " + ooVooSdkResult.getDescription());
					}
				}
			});
			return true;
		} catch (Exception ex) {
			LogSdk.d(TAG, "error on sending out CN message: " + ex.getMessage());
			return false;
		}
	}

	public boolean sendCNMessage(ArrayList<String> toList, CNMessage.CNMessageType type, final MessageCompletionHandler completionHandler) {
		if (toList == null || toList.isEmpty()) {
			LogSdk.d(TAG, "destination list is empty!");
			return false;
		}

		String displayName = settings.get(ApplicationSettings.AvsSessionDisplayName);
		if (displayName == null || displayName.isEmpty()) {
			LogSdk.e(TAG, "Display name is empty!");
			return false;
		}

		try {
			final CNMessage cnMessage = new CNMessage(toList, type, conferenceId, displayName, uniqueId);
			sdk.getMessaging().sendMessage(cnMessage, new ooVooSdkResultListener() {
				@Override
				public void onResult(ooVooSdkResult ooVooSdkResult) {

					if (completionHandler != null) {
						completionHandler.onHandle(ooVooSdkResult.getResult() == sdk_error.OK);
					}
					if (ooVooSdkResult.getResult() != sdk_error.OK) {
						LogSdk.e(TAG, "Error on send out CN message: " + ooVooSdkResult.getDescription());
					}
				}
			});
			return true;
		} catch (Exception ex) {
			LogSdk.d(TAG, "error on sending out CN message: " + ex.getMessage());
			return false;
		}
	}

	public void generateConferenceId() {
		this.conferenceId = UUID.randomUUID().toString();
	}

	public void setConferenceId(String conferenceId) {
		this.conferenceId = conferenceId;
	}

	public String getConferenceId() {
		return conferenceId;
	}

	public boolean isInConference() {
		return isInConference;
	}

	public boolean isCallNegotiation() {
		return isCallNegotiation;
	}

	public void sendEndOfCall() {
		if (isCallNegotiation && isInConference) {
			synchronized (participants) {
				ArrayList<String> toList = new ArrayList<String>();
				Enumeration<String> en = participants.keys();
				while (en.hasMoreElements()) {
					String uid = en.nextElement();
					toList.add(uid);
				}

				sendCNMessage(toList, CNMessage.CNMessageType.EndCall, null);
			}
		}
	}

	public boolean leave() {
		if (isCallNegotiation && isInConference) {
			if (participants.isEmpty()) {
				onEndOfCall();
				return true;
			}
		}
		return false;
	}

	public void showErrorMessageBox(Activity activity, String title, String msg) {
		try {
			AlertDialog.Builder popupBuilder = new AlertDialog.Builder(activity);
			TextView myMsg = new TextView(activity);
			myMsg.setText(msg);
			myMsg.setGravity(Gravity.CENTER);
			popupBuilder.setTitle(title);
			popupBuilder.setPositiveButton("OK", null);
			popupBuilder.setView(myMsg);

			popupBuilder.show();
		} catch (Exception e) {
		}
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public Map<String, Boolean> getMuted() {
		return muted;
	}

	public String getDeviceID() {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager.getDeviceId() != null) {
			return telephonyManager.getDeviceId();
		} else {
			return Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
		}
	}

	public void subscribe(String token) {
		sdk.getPush().subscribe(token, getDeviceID(), new ooVooSdkResultListener() {
			@Override
			public void onResult(ooVooSdkResult ooVooSdkResult) {
				if (ooVooSdkResult.getResult() != sdk_error.OK) {
					LogSdk.e(TAG, "Error to subscribe push notifications : " + ooVooSdkResult.getDescription());
				}
			}
		});
	}

	public void unsubscribe(String token) {
		sdk.getPush().unsubscribe(token, getDeviceID(), new ooVooSdkResultListener() {
			@Override
			public void onResult(ooVooSdkResult ooVooSdkResult) {
				if (ooVooSdkResult.getResult() != sdk_error.OK) {
					LogSdk.e(TAG, "Error to unsubscribe push notifications : " + ooVooSdkResult.getDescription());
					Toast.makeText(getBaseContext(), R.string.user_unsubscribe_error, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getBaseContext(), R.string.user_unsubscribed, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void send(ArrayList<String> users, String payload, String property) {
		sdk.getPush().send(new PNMessage(users, payload, property), new ooVooSdkResultListener() {
			@Override
			public void onResult(ooVooSdkResult ooVooSdkResult) {
				if (ooVooSdkResult.getResult() != sdk_error.OK) {
					LogSdk.e(TAG, "Error to send push notification : " + ooVooSdkResult.getDescription());
					Toast.makeText(getBaseContext(), R.string.push_notification_error, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getBaseContext(), R.string.push_notification_sent, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
