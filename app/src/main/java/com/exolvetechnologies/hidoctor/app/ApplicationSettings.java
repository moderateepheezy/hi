package com.exolvetechnologies.hidoctor.app;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.oovoo.core.LoggerListener.LogLevel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;

public class ApplicationSettings extends Hashtable<String, String> {

	private static final String TOKEN = "MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABXwFgIXL" +
			"PH0EAwMgDUDB3rVHQUhSAla2yybf4eEK4PL8BNlLxLoQlT9Xj3lVuKcG1R6Gg0L%2FSgzFt7jvhR%2BtrB9" +
			"OkL8ZI8gCz91%2BPStaVeHA%3D%3D"; // Put your application token here
	public static final String	Token				  = "token";
	public static final String	Username	          = "username";
	public static final String	ResolutionLevel	      = "resolution_level";
	public static final String	AvsSessionId	      = "avs_session_id";
	public static final String	RandomAvsSessionId	  = "random_avs_session_id";
	public static final String	AvsSessionDisplayName	= "avs_session_display_name";
	public static final String	LogLevelKey			  = "log_level_key";
	public static final String	UseCustomRender		  = "use_custom_render";
	public static final String  SecurityState		  = "security_state";
	private static final long	serialVersionUID	  = 1L;
	public static final String	TAG	                  = "ApplicationSettings";
	public static final String  SENT_TOKEN_TO_SERVER  = "sentTokenToServer";
	public static final String  REGISTRATION_COMPLETE = "registrationComplete";
	public static final String 	PREVIEW_ID			  = "";
	private Context	           appcontext	          = null;

	public ApplicationSettings(Context appcontext) {
		this.appcontext = appcontext;
		load();

		if (get(ApplicationSettings.Token) == null) {
			put(ApplicationSettings.Token, TOKEN);
		}
		
		if (get(ApplicationSettings.LogLevelKey) == null) {
			put(ApplicationSettings.LogLevelKey, LogLevel.Debug.toString());
		}
	}

	public void load() {
		try {
			FileInputStream stream = appcontext.openFileInput("ApplicationSettings");
			JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(stream)));
			// String val = reader.toString();
			reader.beginObject();

			while (reader.hasNext()) {
				String key = reader.nextName();
				String value = reader.nextString();
				this.put(key, value);
				Log.d(TAG, "Settings [" + key + " = " + value + "]");
			}

			reader.endObject();

			reader.close();
			stream.close();

		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public void save() {
		try {
			try {
				appcontext.deleteFile("ApplicationSettings");
			} catch (Exception err) {
			}

			FileOutputStream stream = appcontext.openFileOutput("ApplicationSettings", Context.MODE_PRIVATE);
			JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(stream)));
			writer.setIndent("  ");
			writer.beginObject();
			Enumeration<String> keys = this.keys();
			while (keys.hasMoreElements()) {
				String value = keys.nextElement();
				writer.name(value).value(get(value));
			}
			writer.endObject();
			stream.flush();
			writer.close();
			stream.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}
