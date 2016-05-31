package com.exolvetechnologies.hidoctor.call;

import com.oovoo.sdk.api.PushNotificationMessage;

import java.util.ArrayList;

/**
 * Created by oovoo on 9/8/15.
 */
public class PNMessage extends PushNotificationMessage {

    public PNMessage(ArrayList<String> users, String payload, String property) {
        super(users, payload, property);
    }
}
