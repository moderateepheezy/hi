package com.exolvetechnologies.hidoctor.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.oovoo.core.Utils.LogSdk;
import com.exolvetechnologies.hidoctor.R;

public class ReautorizeFragment extends BaseFragment {
	private static final String	TAG	             = ReautorizeFragment.class.getSimpleName();
	private String	            reason	         = null;
	private MenuItem	        settingsMenuItem	= null;

	public ReautorizeFragment(){}

	public static final ReautorizeFragment newInstance(MenuItem settingsMenuItem, String reason) {
		ReautorizeFragment instance = new ReautorizeFragment();
		instance.setSettingsMenuItem(settingsMenuItem);
		instance.setReason(reason);
	    return instance;
	}

	public void setSettingsMenuItem(MenuItem settingsMenuItem) {
		this.settingsMenuItem = settingsMenuItem;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.reauthorize_layout, container, false);
		final Button reauthorizeButton = (Button) view.findViewById(R.id.reauthorize_button);
		final TextView errorTextView = (TextView) view.findViewById(R.id.error_label);


		if(this.reason.startsWith("App Token probably invalid")){
			reauthorizeButton.setText("Finish");
		}

		reauthorizeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(reason.startsWith("App Token probably invalid")){
					getActivity().finish();
					return ;
				}
				errorTextView.setVisibility(View.INVISIBLE);
				app().reautorize();
			}
		});

		errorTextView.setVisibility(View.VISIBLE);
		if (app().isOnline()) {
			errorTextView.setText(reason);
		} else {
			errorTextView.setText(getActivity().getResources().getString(R.string.no_internet));
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			if (settingsMenuItem != null)
				settingsMenuItem.setVisible(true);
		} catch (Exception err) {
			LogSdk.e(TAG,"onResume error "+err);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			if (settingsMenuItem != null)
				settingsMenuItem.setVisible(false);
		} catch (Exception err) {
			LogSdk.e(TAG,"onPause error "+err);
		}
	}

	/***
	 * In the fragment when user click on back button we just call finish on
	 * host activity
	 */
	public boolean onBackPressed() {
		this.getActivity().finish();
		return false;
	}
}
