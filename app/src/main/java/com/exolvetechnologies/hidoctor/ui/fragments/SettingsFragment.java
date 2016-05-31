package com.exolvetechnologies.hidoctor.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.oovoo.core.LoggerListener.LogLevel;
import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.app.ApplicationSettings;
import com.exolvetechnologies.hidoctor.ui.CustomVideoPanel;

public class SettingsFragment extends BaseFragment {
	
	private Spinner logSpinner = null;
	private TextView tokenTextView = null;
	private CustomVideoPanel customPanel = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings_fragment_layout, container, false);

		customPanel = (CustomVideoPanel) container.findViewById(R.id.custom_preview_view);

		tokenTextView = (TextView) view.findViewById(R.id.token_edit_text);
		tokenTextView.setText(settings().get(ApplicationSettings.Token));
		
		logSpinner = (Spinner) view.findViewById(R.id.log_level_spinner);

		String[] logLevelValues = {LogLevel.None.toString(), LogLevel.Fatal.toString(), LogLevel.Error.toString(), LogLevel.Warning.toString(),
				LogLevel.Info.toString(), LogLevel.Debug.toString(), LogLevel.Trace.toString()};
		ArrayAdapter<String> logAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, logLevelValues);
		logSpinner.setAdapter(logAdapter);
		int logSpinnerPosition = logAdapter.getPosition(settings().get(ApplicationSettings.LogLevelKey));
		logSpinner.setSelection(logSpinnerPosition);
		
		logSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String logLevel = (String) logSpinner.getSelectedItem();
		        settings().put(ApplicationSettings.LogLevelKey, logLevel);
		        app().setLogLevel(logLevel);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
        });
		
		TextView sdkVersion = (TextView) view.findViewById(R.id.sdk_version_text_view);
		sdkVersion.setText(app().getSdkVersion());
		
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (customPanel != null) {
			customPanel.setVisibility(View.INVISIBLE);
		}
	}

	@Override
    public void onPause() {
        super.onPause();
	
        String logLevel = (String) logSpinner.getSelectedItem();
        settings().put(ApplicationSettings.LogLevelKey, logLevel);
        settings().put(ApplicationSettings.Token, tokenTextView.getText().toString());
        settings().save();

		if (customPanel != null) {
			customPanel.setVisibility(View.VISIBLE);
		}
	}
}
