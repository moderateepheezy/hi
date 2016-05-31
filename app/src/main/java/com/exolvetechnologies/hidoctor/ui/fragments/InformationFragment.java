package com.exolvetechnologies.hidoctor.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.oovoo.core.Utils.LogSdk;
import com.oovoo.core.sdk_error;
import com.oovoo.sdk.interfaces.VideoControllerListener.RemoteVideoState;
import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.app.ApplicationSettings;
import com.exolvetechnologies.hidoctor.app.ooVooSdkSampleShowApp.ParticipantsListener;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class InformationFragment extends BaseFragment implements ParticipantsListener {

	private final static String TAG = InformationFragment.class.getSimpleName();
	private TextView sessionIdTextView = null;
	private ParticipantAdapter participantAdapter = null;
	private ListView participantsList = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.information_fragment_layout, container, false);
		
		sessionIdTextView = (TextView) view.findViewById(R.id.session_id_text_view);
		sessionIdTextView.setText(settings().get(ApplicationSettings.AvsSessionId));

	    participantAdapter = new ParticipantAdapter();
	    
		Enumeration<String> en = app().getParticipants().keys();
		while (en.hasMoreElements()) {
			String userId = en.nextElement();
			String displayName = app().getParticipants().get(userId);
			participantAdapter.addItem(participantAdapter.new Participant(userId, displayName));
		}
		
		participantsList = (ListView) view.findViewById( R.id.participants_list);
	    participantsList.setDivider(null);
	    participantsList.setDividerHeight(0);
	    participantsList.setAdapter(participantAdapter);
		app().addParticipantListener(this);
		view.setKeepScreenOn(true);
		return view;
	}
	
	@Override
	public void onDestroy() {
		app().removeParticipantListener(this);
		super.onDestroy();
	}

	private class ParticipantAdapter extends BaseAdapter
	{
		private final List<Participant>	participants = new ArrayList<Participant>();
		
		private class ParticipantViewHolder
		{
			TextView	textView;
			Switch		muteSwitch;
		}
		
		public class Participant {
			private String userId;
			private String displayName;
			
			public Participant(String userId, String displayName) {
				this.userId = userId;
				this.displayName = displayName;
			}

			public String getDisplayName() {
				return displayName;
			}
			
			public String getUserId() {
				return userId;
			}
		}
		
		public void addItem(Participant participant) {
			Iterator<Participant> iter = participants.iterator();
			while(iter.hasNext()){
				Participant item = iter.next();
				if (item.getUserId() != null && item.getUserId().equals(participant.getUserId())) {
					return;
			    }
			}
			participants.add(participant);
			notifyDataSetChanged();
		}
		
		public void removeItem(String userId) {
			Iterator<Participant> iter = participants.iterator();
			while(iter.hasNext()){
				Participant item = iter.next();
				if (item.getUserId() != null && item.getUserId().equals(userId)) {
					iter.remove();
			    }
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount()
		{
			return participants.size();
		}

		@Override
		public Object getItem(int i)
		{
			return participants.get(i);
		}

		@Override
		public long getItemId(int i)
		{
			return i; // index number
		}

		@Override
		public View getView(int index, View view, final ViewGroup parent)
		{
			ParticipantViewHolder viewHolder = null;
			if( view == null)
			{
				viewHolder = new ParticipantViewHolder();

				try
				{
					LayoutInflater inflater = LayoutInflater.from(parent.getContext());
					view = inflater.inflate( R.layout.participant_info, parent, false);
					viewHolder.textView = (TextView) view.findViewById(R.id.display_name);
					viewHolder.muteSwitch = (Switch) view.findViewById(R.id.mute_switch);
					
					view.setTag(viewHolder);
				}
				catch(Exception e)
				{
					LogSdk.e(TAG, "getView Inflate exception.", e);
				}
			}
			else
			{
				viewHolder = (ParticipantViewHolder) view.getTag();
			}

			if (viewHolder != null && view != null)
			{
				final Participant participant = participants.get(index);
				boolean isMuted = app().getMuted().get(participant.getUserId()) == null ? false :
						app().getMuted().get(participant.getUserId());
				viewHolder.textView.setText(participant.getDisplayName());
				viewHolder.muteSwitch.setOnCheckedChangeListener(null);
				viewHolder.muteSwitch.setChecked(!isMuted);
				viewHolder.muteSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (!isChecked) {
							app().unregisterRemote(participant.getUserId());
							((AVChatSessionFragment) getBackFragment()).muteVideo(participant.getUserId());
							app().getMuted().put(participant.getUserId(), true);
						} else {
							app().registerRemote(participant.getUserId());
							((AVChatSessionFragment) getBackFragment()).unmuteVideo(participant.getUserId());
						}
					}
				});
			}
			
			return view;
		}
	}

	@Override
	public void onParticipantJoined(String userId, String userData) {
		participantAdapter.addItem(participantAdapter.new Participant(userId, userData));
	}

	@Override
	public void onParticipantLeft(String userId) {
		participantAdapter.removeItem(userId);
	}

	@Override
	public void onRemoteVideoStateChanged(String userId,
			RemoteVideoState state, sdk_error error) {
		participantAdapter.notifyDataSetChanged();
	}

	@Override
	public void onTransmitStateChanged(boolean state, sdk_error err) {
		// TODO Auto-generated method stub
		
	}
}
