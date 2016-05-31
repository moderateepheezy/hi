package com.exolvetechnologies.hidoctor.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.activities.LoginActivity;
import com.exolvetechnologies.hidoctor.ui.ChatsActivity;


public class LoginFragment extends BaseFragment {
	
	private static final int MIN_USERNAME_LENGTH = 6;
	private static final int USERNAME_LIMIT = 200;
	private static final int DISPLAY_NAME_LIMIT = 100;
	private String errorDescription 		= null;
	private EditText usernameEditText 		= null;
	private EditText displayNameEditText	= null;
	private TextView errorTextView			= null;
	private MenuItem settingsMenuItem 		= null;

	public LoginFragment() {
	}

	public static LoginFragment newInstance(MenuItem settingsMenuItem) {
		LoginFragment fragment = new LoginFragment();
		fragment.settingsMenuItem = settingsMenuItem;

		return fragment;
	}
	
	public static final LoginFragment newInstance(String errorDescription)
	{
		LoginFragment instance = new LoginFragment();
		instance.setErrorDescription(errorDescription);
	    return instance;
	}
	
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	
	@Override
    public void onResume() {
	    super.onResume();

	    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    getActivity().getWindow().setBackgroundDrawableResource(R.drawable.slqsm);
		if (settingsMenuItem != null) {
			settingsMenuItem.setVisible(true);
		}
    }
	
	@Override
    public void onPause() {
        super.onPause();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		if (settingsMenuItem != null) {
			settingsMenuItem.setVisible(false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_fragment_layout, container, false);

		Button loginButton = (Button) view.findViewById(R.id.login_button);

		usernameEditText = (EditText) view.findViewById(R.id.username_field);
		String username = settings().get("username");
		if (username != null) {
			usernameEditText.setText(username);
		}

		String lastDisplayName = settings().get("avs_session_display_name");

		displayNameEditText = (EditText) view.findViewById(R.id.displayname_field);

		if (lastDisplayName != null) {
			displayNameEditText.setText(lastDisplayName);
		}

		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoginClick();
			}
		});

		errorTextView = (TextView)view.findViewById(R.id.error_label);
		errorTextView.setVisibility(View.INVISIBLE);
		errorTextView.setText("");
		if (!app().isOnline()) {
			errorTextView.setVisibility(View.VISIBLE);
			errorTextView.setText(getActivity().getResources().getString(R.string.no_internet));
		} else if (errorDescription != null) {
			errorTextView.setVisibility(View.VISIBLE);
			errorTextView.setText(errorDescription);
		}

		if (!ChatsActivity.IS_LOGGED_IN) {
			onLoginClick();
		}

		return view;
	}

	public void onLoginClick() {
		errorTextView.setText("");
		SharedPreferences accPrefs = getActivity().getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
		String firstName = accPrefs.getString(LoginActivity.FIRST_NAME, "");
		String id = accPrefs.getString(LoginActivity.USER_ID, "");
		String username = "hidoctor_"+id; //usernameEditText.getText().toString();
        Log.i("HiDoctor", "User Id: "+username.replace(" ", ""));
        ChatsActivity.USER_ID = username.replace(" ", "");
		if (username.isEmpty()) {
			showErrorMessageBox(getString(R.string.login_title), getString(R.string.enter_username_toast));

			return;
		}

		if (username.length() < MIN_USERNAME_LENGTH) {
			showErrorMessageBox(getString(R.string.characters_missing), getString(R.string.min_username_length));

			return;
		}
		
		if (!username.matches("^([a-zA-Z0-9-_%. ])+$") || username.length() > USERNAME_LIMIT) {
			showErrorMessageBox(getString(R.string.login_title), getString(R.string.wrong_username_id));
			
			return;
		}

		String displayName = firstName; //displayNameEditText.getText().toString();

		if (!checkDisplayName(displayName)) {
			return;
		}
		
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);
		
		app().login(username.replace(" ", ""), displayName);
		ChatsActivity.IS_LOGGED_IN = true;
	}

	private boolean checkDisplayName(String displayName)
	{
		if (displayName.isEmpty()) {
			showErrorMessageBox(getString(R.string.login_title), getString(R.string.enter_conference_display_name));

			return false;
		}

		if (displayName.length() > DISPLAY_NAME_LIMIT) {
			showErrorMessageBox(getString(R.string.login_title), getString(R.string.display_name_too_long));

			return false;
		}

		return true;
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
	
	/***
	 * In the fragment when user click on back button we just call finish on host activity
	 */
	public boolean onBackPressed()
	{
		this.getActivity().finish();
		return false ;
	}

}
