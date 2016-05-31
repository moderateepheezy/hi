package com.exolvetechnologies.hidoctor.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.exolvetechnologies.hidoctor.ui.ChatsActivity;
import com.oovoo.core.Utils.LogSdk;
import com.oovoo.sdk.api.ui.VideoPanel;
import com.oovoo.sdk.interfaces.AudioController;
import com.oovoo.sdk.interfaces.VideoController;
import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.app.ApplicationSettings;
import com.exolvetechnologies.hidoctor.ui.CustomVideoPanel;
import com.exolvetechnologies.hidoctor.ui.VideoPanelPreviewRect;

public class AVChatLoginFragment extends BaseFragment {
	private static final int CONFERENCE_ID_LIMIT = 200;
	private static final String TAG = AVChatLoginFragment.class.getSimpleName();
	private EditText	sessionIdEditText	= null;
	private MenuItem 	settingsMenuItem = null;
	private VideoPanelPreviewRect previewRect = null;

	public AVChatLoginFragment(){}

	public static final AVChatLoginFragment newInstance(MenuItem settingsMenuItem) {
		AVChatLoginFragment instance = new AVChatLoginFragment();
	    instance.setSettingsMenuItem(settingsMenuItem);
	    return instance;
	}

	public void setSettingsMenuItem(MenuItem settingsMenuItem) {
		this.settingsMenuItem = settingsMenuItem;
	}

	@Override
    public void onResume() {
	    super.onResume();
		getActivity().getWindow().setBackgroundDrawableResource(R.drawable.slqsm);
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    if (app().isTablet()) {
	    	updatePreviewLayout(newConfig);
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.avchat_login_fragment, container, false);
		VideoPanel panel = (VideoPanel) view.findViewById(R.id.preview_view);
		CustomVideoPanel customPanel = (CustomVideoPanel) view.findViewById(R.id.custom_preview_view);

		TextView displayNameTextView = (TextView) view.findViewById(R.id.display_name_text_view);

		previewRect = (VideoPanelPreviewRect) view.findViewById(R.id.preview_rect);

		if (app().isTablet()) {
			Configuration config = getResources().getConfiguration();
			updatePreviewLayout(config);
		}
		app().checkGL();
		app().selectCamera("FRONT");
		app().changeResolution(VideoController.ResolutionLevel.ResolutionLevelMed);
	    app().openPreview();

		String lastSessionId = settings().get("avs_session_id");

		sessionIdEditText = (EditText) view.findViewById(R.id.session_field);

		if (lastSessionId != null) {
			sessionIdEditText.setText(lastSessionId);
		}

		/****
		 * Let's bind the view for camera preview output
		 */
		String useCustomRenderValue = settings().get(ApplicationSettings.UseCustomRender);

		if (useCustomRenderValue != null && Boolean.valueOf(useCustomRenderValue)) {
			panel.setVisibility(View.INVISIBLE);
			customPanel.setVisibility(View.VISIBLE);
			app().bindVideoPanel(ApplicationSettings.PREVIEW_ID, customPanel);
			if (customPanel.isCircleShape()) {
				displayNameTextView.setVisibility(View.INVISIBLE);
			}
		} else {
			customPanel.setVisibility(View.INVISIBLE);
			panel.setVisibility(View.VISIBLE);
			app().bindVideoPanel(ApplicationSettings.PREVIEW_ID, panel);
		}

		Button join = (Button) view.findViewById(R.id.join_button);
		join.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				join();
			}
		});

		if (!ChatsActivity.HAS_JOINED){
			join();
		}

		return view;
	}

	private void updatePreviewLayout(Configuration config) {
		if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			int width = app().getDisplaySize().x;
	    	int padding = (width - ((int)(app().getDisplaySize().y * 0.75f * (4.0/3.0))))/2;

			String useCustomRenderValue = settings().get(ApplicationSettings.UseCustomRender);
			if (useCustomRenderValue != null && Boolean.valueOf(useCustomRenderValue)) {
				padding = (width - (int)(app().getDisplaySize().y * 0.7f))/2;
			}
			previewRect.setPadding(padding, 0, padding, 0);
		} else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
	    	previewRect.setPadding(0, 0, 0, 0);
	    }
	}

	private boolean checkSessionId(String sessionId)
	{
		if (sessionId.isEmpty()) {
			Toast.makeText(getActivity(), R.string.enter_conference_id, Toast.LENGTH_LONG).show();

			return false;
		}

		if (sessionId.length() > CONFERENCE_ID_LIMIT) {
			showErrorMessageBox(getString(R.string.join_session), getString(R.string.wrong_conference_id));

			return false;
		}

		return true;
	}

	private void join()
	{
		String sessionId = ChatsActivity.SESSION_ID; //sessionIdEditText.getText().toString();

		if (!checkSessionId(sessionId)) {
			return;
		}

		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(sessionIdEditText.getWindowToken(), 0);

		if (!app().isOnline()) {
			showErrorMessageBox("Network Error", getString(R.string.no_internet));
			return;
		}

		app().join(sessionId, false);
		ChatsActivity.HAS_JOINED = true;
	}

	protected void finalize() throws Throwable {
		LogSdk.d(TAG, "ooVooCamera -> VideoPanel -> finalize AVChatLoginFragment ->");
		super.finalize();
	}

	public void showErrorMessageBox(String title,String msg)
	{
		try {
				AlertDialog.Builder popupBuilder = new AlertDialog.Builder(getActivity());
				TextView myMsg = new TextView(getActivity());
				myMsg.setText(msg);
				myMsg.setGravity(Gravity.CENTER);
				popupBuilder.setTitle(title);
				popupBuilder.setPositiveButton("OK", null);
				popupBuilder.setView(myMsg);

				popupBuilder.show();
		} catch( Exception e) {
		}
	}

	public BaseFragment getBackFragment() {
		return OptionFragment.newInstance(settingsMenuItem);
	}

	public boolean onBackPressed() {
		app().releaseAVChat();

		return true;
    }
}
