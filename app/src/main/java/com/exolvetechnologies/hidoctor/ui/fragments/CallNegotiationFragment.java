package com.exolvetechnologies.hidoctor.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.app.ooVooSdkSampleShowApp.CallNegotiationListener;
import com.exolvetechnologies.hidoctor.app.ooVooSdkSampleShowApp.MessageCompletionHandler;
import com.exolvetechnologies.hidoctor.call.CNMessage;
import com.exolvetechnologies.hidoctor.call.CNMessage.CNMessageType;

import java.util.ArrayList;
import java.util.List;

public class CallNegotiationFragment extends BaseFragment implements View.OnClickListener, CallNegotiationListener {
	
	private static final String KEY_ADAPTER_STATE = "com.oovoo.sdk.fragmentstate.KEY_ADAPTER_STATE";
	private static final int MAX_CALL_RECEIVERS = 4;
	private ListView callReceiversList = null;
	private CallReceiverAdapter callReceiverAdapter = null;
	private AlertDialog callDialogBuilder = null;
	private AlertDialog callReceiverDialog = null;
	private ArrayList<CallReceiverAdapter.CallReceiver> adapterSavedState = null;
	private int count = 0;
	private int enabledReceivesCount = 0;
	private MenuItem settingsMenuItem = null;
	
	public static final CallNegotiationFragment newInstance(MenuItem settingsMenuItem) {
		CallNegotiationFragment fragment = new CallNegotiationFragment();
		fragment.settingsMenuItem = settingsMenuItem;

	    return fragment;
	}
	
	public CallNegotiationFragment() {
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ADAPTER_STATE)) {
        	adapterSavedState = savedInstanceState.getParcelableArrayList(KEY_ADAPTER_STATE);
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.call_negotiation_fragment, container, false);
		
		callReceiverAdapter = new CallReceiverAdapter();

		if (adapterSavedState != null) {
			callReceiverAdapter.onRestoreInstanceState(adapterSavedState);
        }
		
		callReceiversList = (ListView) view.findViewById( R.id.call_receivers_list);
		callReceiversList.setDivider(null);
		callReceiversList.setDividerHeight(0);
		callReceiversList.setAdapter(callReceiverAdapter);

		Button addButton = (Button) view.findViewById(R.id.add_to_call_button);
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(getString(R.string.enter_call_receiver));

				final EditText input = new EditText(getActivity());
				
				input.setInputType(InputType.TYPE_CLASS_TEXT);
				builder.setView(input);

				builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	callReceiverAdapter.addItem(input.getText().toString());
				    }
				});
				builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				    }
				});
				
				callReceiverDialog = builder.create();
				callReceiverDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				callReceiverDialog.show();
			}
		});
		
		Button callButton = (Button) view.findViewById(R.id.start_call_button);
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				app().generateConferenceId();

				enabledReceivesCount = callReceiverAdapter.getEnabledReceivesCount();

				boolean showDialog = sendCNMessage(CNMessageType.Calling, new MessageCompletionHandler() {
					@Override
					public void onHandle(boolean state) {



						if (!state) {
							count = 0 ;
							Toast.makeText(getActivity(), R.string.fail_to_send_message, Toast.LENGTH_LONG).show();
							callDialogBuilder.hide();
							return  ;
						}

						count = enabledReceivesCount;
					}
				});

				if (showDialog) {
					callDialogBuilder.show();
				} else {
					Toast.makeText(getActivity(), R.string.no_receivers, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		callDialogBuilder = new AlertDialog.Builder(getActivity()).create();
	    
    	View outgoingCallDialog = inflater.inflate(R.layout.outgoing_call_dialog, null);
    	outgoingCallDialog.setAlpha(0.5f);
    	callDialogBuilder.setView(outgoingCallDialog);
    	
    	Button cancelButton = (Button) outgoingCallDialog.findViewById(R.id.cancel_button);
    	cancelButton.setOnClickListener(this);
    
    	callDialogBuilder.setCancelable(false);
    	
    	app().addCallNegotiationListener(this);
		
		return view;
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (callReceiverAdapter != null) {
            adapterSavedState = callReceiverAdapter.onSaveInstanceState();
        }

        outState.putParcelableArrayList(KEY_ADAPTER_STATE, adapterSavedState);
    }
	
	@Override
    public void onResume() {
	    super.onResume();

	    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    getActivity().getWindow().setBackgroundDrawableResource(R.drawable.slqsm);
    }
	
	@Override
    public void onPause() {
        super.onPause();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}
	
	@Override
	public void onDestroyView() 
	{
		if (callReceiverDialog != null) {
			callReceiverDialog.getWindow().setSoftInputMode(0);
			callReceiverDialog.hide();
		}
		callDialogBuilder.hide();
		app().removeCallNegotiationListener(this);
		
		if (callReceiverAdapter != null) {
            adapterSavedState = callReceiverAdapter.onSaveInstanceState();
        }
        
		super.onDestroyView();
	}
	
	private static class CallReceiverAdapter extends BaseAdapter
	{
		private final List<CallReceiver> receivers = new ArrayList<CallReceiver>();
		
		private class CallReceiverViewHolder
		{
			TextView	textView;
			Switch		callSwitch;
		}
		
		public static class CallReceiver implements Parcelable {
			private String receiverId = null;
			private Boolean isCallEnabled = true;
			
			public CallReceiver(String receiverId) {
				this.receiverId = receiverId;
			}
			
			public String getReceiverId() {
				return receiverId;
			}
			
			public void setCallEnabled(boolean isCallEnabled) {
				this.isCallEnabled = isCallEnabled;
			}
			
			public boolean isCallEnabled() {
				return this.isCallEnabled;
			}
			
			protected CallReceiver(Parcel in) {
				receiverId = in.readString();
				isCallEnabled = in.readByte() != 0;
	        }

			@Override
			public int describeContents() {
				return 0;
			}

			@Override
			public void writeToParcel(Parcel dest, int flags) {
				dest.writeString(receiverId);
				dest.writeByte((byte) (isCallEnabled ? 1 : 0)); 
			}
			
			@SuppressWarnings("unused")
	        public static final Parcelable.Creator<CallReceiver> CREATOR = new Parcelable.Creator<CallReceiver>() {
	            @Override
	            public CallReceiver createFromParcel(Parcel in) {
	                return new CallReceiver(in);
	            }

	            @Override
	            public CallReceiver[] newArray(int size) {
	                return new CallReceiver[size];
	            }
	        };
		}
		
		ArrayList<CallReceiver> onSaveInstanceState()
		{
            int size = getCount();
            ArrayList<CallReceiver> items = new ArrayList<CallReceiver>(size);
            for(int i = 0; i < size; i++) {
                items.add(getItem(i));
            }
            return items;
        }

        void onRestoreInstanceState(ArrayList<CallReceiver> items)
        {
        	receivers.clear();
        	receivers.addAll(items);
        }

		public int getEnabledReceivesCount()
		{
			int count = 0;
			for (int i = 0; i < receivers.size(); i++) {
				CallReceiverAdapter.CallReceiver receiver = getItem(i);
				if (receiver.isCallEnabled() && count < MAX_CALL_RECEIVERS) {
					count++;
				}
			}
			return count;
		}

		@Override
		public int getCount()
		{
			return receivers.size();
		}

		@Override
		public CallReceiver getItem(int i)
		{
			return receivers.get(i);
		}

		@Override
		public long getItemId(int i)
		{
			return i; // index number
		}

		@Override
		public View getView(int index, View view, final ViewGroup parent)
		{
			CallReceiverViewHolder viewHolder = null;
			final CallReceiver receiver = receivers.get(index);
			if (view == null)
			{
				viewHolder = new CallReceiverViewHolder();

				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				view = inflater.inflate(R.layout.call_receiver_info, parent, false);
				viewHolder.textView = (TextView) view.findViewById(R.id.receiver_id_text_view);
				viewHolder.callSwitch = (Switch) view.findViewById(R.id.receiver_switch);
				
				view.setTag(viewHolder);
			}
			else
			{
				viewHolder = (CallReceiverViewHolder) view.getTag();
			}

			viewHolder.textView.setText(receiver.getReceiverId());

			viewHolder.callSwitch.setOnCheckedChangeListener(null);
			viewHolder.callSwitch.setChecked(receiver.isCallEnabled());
			viewHolder.callSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {	
					receiver.setCallEnabled(isChecked);
				}
			});
			
			return view;
		}
		
		public void addItem(String receiverId) 
		{
			if (receiverId == null || receiverId.isEmpty())
				return;
			
			receivers.add(new CallReceiver(receiverId));
		}
	}

	@Override
	public void onClick(View v) {

		callDialogBuilder.hide();
		
		switch (v.getId()) {
			case R.id.cancel_button:
			{
				sendCNMessage(CNMessageType.Cancel, null);
			}
			break;

		default:
			break;
		}
	}

	private boolean sendCNMessage(CNMessageType type, MessageCompletionHandler completionHandler)
	{
		ArrayList<String> toList = new ArrayList<String>();
		for (int i = 0; i < callReceiverAdapter.getCount(); i++) {
			CallReceiverAdapter.CallReceiver receiver = (CallReceiverAdapter.CallReceiver) callReceiverAdapter.getItem(i);

			if (receiver.isCallEnabled() && toList.size() < MAX_CALL_RECEIVERS) {
				toList.add(receiver.getReceiverId());
			}
		}

		if (toList.isEmpty()) {
			return false;
		}

		app().sendCNMessage(toList, type, completionHandler);

		return true;
	}

	@Override
	public void onMessageReceived(CNMessage cnMessage) 
	{
		if (app().getUniqueId().equals(cnMessage.getUniqueId())) {
			return;
		}

		if (cnMessage.getMessageType() == CNMessage.CNMessageType.AnswerAccept) {
			app().join(app().getConferenceId(), true);
		} else if (cnMessage.getMessageType() == CNMessage.CNMessageType.AnswerDecline) {
			count--;
			if (count <= 0) {
				callDialogBuilder.hide();
			}
		}  else if (cnMessage.getMessageType() == CNMessageType.Busy) {
			count--;
			if (count <= 0) {
				callDialogBuilder.hide();
			}
		}
	}

	public BaseFragment getBackFragment() {
		return OptionFragment.newInstance(settingsMenuItem);
	}
}
