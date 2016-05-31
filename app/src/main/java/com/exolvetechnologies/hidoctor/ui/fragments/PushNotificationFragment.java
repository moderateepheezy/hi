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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.app.ApplicationSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oovoo on 9/9/15.
 */
public class PushNotificationFragment extends BaseFragment {

    private static final String KEY_ADAPTER_STATE = "com.oovoo.sdk.pushfragmentstate.KEY_ADAPTER_STATE";
    private PushReceiverAdapter pushReceiverAdapter = null;
    private ListView pushReceiversList = null;
    private AlertDialog pushDialogBuilder = null;
    private AlertDialog pushReceiverDialog = null;
    private ArrayList<PushReceiverAdapter.PushReceiver> adapterSavedState = null;
    private MenuItem settingsMenuItem = null;

    public static final PushNotificationFragment newInstance(MenuItem settingsMenuItem) {
        PushNotificationFragment fragment = new PushNotificationFragment();
        fragment.settingsMenuItem = settingsMenuItem;

        return fragment;
    }

    public PushNotificationFragment() {
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
        View view = inflater.inflate(R.layout.push_notification_fragment, container, false);

        pushReceiverAdapter = new PushReceiverAdapter();

        if (adapterSavedState != null) {
            pushReceiverAdapter.onRestoreInstanceState(adapterSavedState);
        }

        pushReceiversList = (ListView) view.findViewById( R.id.push_receivers_list);
        pushReceiversList.setDivider(null);
        pushReceiversList.setDividerHeight(0);
        pushReceiversList.setAdapter(pushReceiverAdapter);

        Button unsubscribeButton = (Button) view.findViewById(R.id.unsubscribe_push_button);
        unsubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApplicationSettings settings = app().getSettings();
                String username = settings.get(ApplicationSettings.Username);
                String token = settings.get(username);
                if (token != null) {
                    app().unsubscribe(token);
                    settings.remove(username);
                    settings.save();
                } else {
                    Toast.makeText(getActivity(), R.string.no_token_to_unsubscribe, Toast.LENGTH_LONG).show();
                }
            }
        });

        Button addButton = (Button) view.findViewById(R.id.add_to_push_button);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.enter_push_receiver));

                final EditText input = new EditText(getActivity());

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pushReceiverAdapter.addItem(input.getText().toString());
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                pushReceiverDialog = builder.create();
                pushReceiverDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                pushReceiverDialog.show();
            }
        });

        Button sendButton = (Button) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.enter_text_push));

                final EditText input = new EditText(getActivity());

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ArrayList<String> users = new ArrayList<String>();
                        for (int i = 0; i < pushReceiverAdapter.getCount(); i++) {
                            PushReceiverAdapter.PushReceiver receiver = (PushReceiverAdapter.PushReceiver) pushReceiverAdapter.getItem(i);

                            if (receiver.isPushEnabled) {
                                users.add(receiver.getReceiverId());
                            }
                        }

                        app().send(users, input.getText().toString(), "ooVooSample");
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                pushDialogBuilder = builder.create();
                pushDialogBuilder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                pushDialogBuilder.show();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (pushReceiverAdapter != null) {
            adapterSavedState = pushReceiverAdapter.onSaveInstanceState();
        }

        outState.putParcelableArrayList(KEY_ADAPTER_STATE, adapterSavedState);
    }

    private static class PushReceiverAdapter extends BaseAdapter
    {
        private final List<PushReceiver> receivers = new ArrayList<PushReceiver>();

        private class PushReceiverViewHolder
        {
            TextView textView;
            Switch pushSwitch;
        }

        public static class PushReceiver implements Parcelable {
            private String receiverId = null;
            private Boolean isPushEnabled = true;

            public PushReceiver(String receiverId) {
                this.receiverId = receiverId;
            }

            public String getReceiverId() {
                return receiverId;
            }

            public void setCallEnabled(boolean isCallEnabled) {
                this.isPushEnabled = isCallEnabled;
            }

            protected PushReceiver(Parcel in) {
                receiverId = in.readString();
                isPushEnabled = in.readByte() != 0;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(receiverId);
                dest.writeByte((byte) (isPushEnabled ? 1 : 0));
            }

            @SuppressWarnings("unused")
            public static final Parcelable.Creator<PushReceiver> CREATOR = new Parcelable.Creator<PushReceiver>() {
                @Override
                public PushReceiver createFromParcel(Parcel in) {
                    return new PushReceiver(in);
                }

                @Override
                public PushReceiver[] newArray(int size) {
                    return new PushReceiver[size];
                }
            };
        }

        ArrayList<PushReceiver> onSaveInstanceState()
        {
            int size = getCount();
            ArrayList<PushReceiver> items = new ArrayList<PushReceiver>(size);
            for(int i = 0; i < size; i++) {
                items.add(getItem(i));
            }
            return items;
        }

        void onRestoreInstanceState(ArrayList<PushReceiver> items)
        {
            receivers.clear();
            receivers.addAll(items);
        }

        @Override
        public int getCount()
        {
            return receivers.size();
        }

        @Override
        public PushReceiver getItem(int i)
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
            PushReceiverViewHolder viewHolder = null;
            final PushReceiver receiver = receivers.get(index);
            if (view == null)
            {
                viewHolder = new PushReceiverViewHolder();

                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                view = inflater.inflate(R.layout.call_receiver_info, parent, false);
                viewHolder.textView = (TextView) view.findViewById(R.id.receiver_id_text_view);
                viewHolder.pushSwitch = (Switch) view.findViewById(R.id.receiver_switch);

                view.setTag(viewHolder);
            }
            else
            {
                viewHolder = (PushReceiverViewHolder) view.getTag();
            }

            viewHolder.textView.setText(receiver.getReceiverId());

            viewHolder.pushSwitch.setOnCheckedChangeListener(null);
            viewHolder.pushSwitch.setChecked(receiver.isPushEnabled);
            viewHolder.pushSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

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

            receivers.add(new PushReceiver(receiverId));
        }
    }

    public BaseFragment getBackFragment() {
        return OptionFragment.newInstance(settingsMenuItem);
    }
}
