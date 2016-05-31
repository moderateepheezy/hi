package com.exolvetechnologies.hidoctor.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    public static final String LOGIN = "login";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String EMAIL = "email";
    public static final String USER_ID = "userId";
    public static final String IMAGE_URL = "schoolId";
    public static final String ROLE = "roleId";
    public static final String STATUS = "status";

    private UserLoginTask mAuthTask = null;
    private AppUtils utils;
    //private AppDataManager dataManager;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private SharedPreferences signIn;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        utils = new AppUtils(this);
        //dataManager = new AppDataManager(this);
        signIn = getSharedPreferences(LOGIN, 0);
        editor = signIn.edit();

        boolean isSignedIn = signIn.getBoolean(STATUS, false);
        if (isSignedIn){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();

                    if (getCurrentFocus() != null) {

                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
                //TODO: Uncomment to allow login without credentials
                //startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } /*else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put("email", mEmail);
            map.put("password", mPassword);
            //String parameters = "email="+mEmail+"&password="+mPassword;
            try {
                //Log.d("HTTP", "The request is: " + parameters);
                //Exolvehost http://exolvetechnologies.com/hid/api/signin
                return AppUtils.sendPostRequest(AppUtils.API_ROOT+"/api/signin", map);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            mAuthTask = null;
            showProgress(false);

            if (response != null) {
                try {
                    JSONObject loginJson = new JSONObject(response);

                    String successful = loginJson.getString("message");

                    if (successful.equals("success")) {

                        String firstName = loginJson.getString("user_firstname");
                        String lastName = loginJson.getString("user_lastname");
                        String role = loginJson.getString("user_role");
                        String id = loginJson.getString("user_id");
                        String email = loginJson.getString("user_email");
                        String imageUrl = loginJson.getString("user_image");
                        editor.putString(FIRST_NAME, firstName);
                        editor.putString(LAST_NAME, lastName);
                        editor.putString(IMAGE_URL, imageUrl);
                        editor.putString(EMAIL, email);
                        editor.putBoolean(STATUS, true);
                        editor.putString(ROLE, role);
                        editor.putString(USER_ID, id);
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    }else{
                        mPasswordView.requestFocus();
                        mPasswordView.setError("Invalid login attempt");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                utils.showAlert(LoginActivity.this, "Network Error", "An error occurred while attempting to log in");
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }

}
