package com.exolvetechnologies.hidoctor.call;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.oovoo.core.Utils.LogSdk;
import com.oovoo.sdk.api.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by shanling on 7/7/15.
 */
public class CNMessage extends Message {

    public enum CNMessageType {
        Calling,
        AnswerAccept,
        AnswerDecline,
        Cancel,
        Busy,
        EndCall,
        Unknown
    }

    private static final String MESSAGE_TYPE    = "type";
    private static final String CONFERENCE_ID   = "conference id";
    private static final String DISPLAY_NAME    = "display name";
    private static final String UNIQUE_ID       = "unique id";
    private static final String EXTRA_MESSAGE   = "extra message";

    private static final String TYPE_CALLING    = "calling";
    private static final String TYPE_ANS_ACCEPT = "accept";
    private static final String TYPE_ANS_DECLINE= "decline";
    private static final String TYPE_CANCEL		= "cancel";
    private static final String TYPE_BUSY		= "busy";
    private static final String TYPE_END_CALL	= "end_call";

    private CNMessageType messageType;
    private String conferenceId;
    private String displayName;
    private String extraMessage;
    private String uniqueId;

    private static String CNMessageTypeToString(CNMessageType type){
        switch(type){
            case Calling:
                return TYPE_CALLING;
            case AnswerAccept:
                return TYPE_ANS_ACCEPT;
            case AnswerDecline:
                return TYPE_ANS_DECLINE;
            case Cancel:
            	return TYPE_CANCEL;
            case Busy:
                return TYPE_BUSY;
            case EndCall:
                return TYPE_END_CALL;
            default:
                return null;
        }
    }

    private static CNMessageType CNMessageStringToType(String typeStr){
        if(typeStr.equals(TYPE_CALLING))
            return CNMessageType.Calling;
        else if(typeStr.equals(TYPE_ANS_ACCEPT))
            return CNMessageType.AnswerAccept;
        else if(typeStr.equals(TYPE_ANS_DECLINE))
            return CNMessageType.AnswerDecline;
        else if(typeStr.equals(TYPE_CANCEL))
            return CNMessageType.Cancel;
        else if(typeStr.equals(TYPE_BUSY))
            return CNMessageType.Busy;
        else if(typeStr.equals(TYPE_END_CALL))
            return CNMessageType.EndCall;
        else
            return CNMessageType.Unknown;
    }

    private static String buildMessageBody(CNMessageType type, String confId, String name, String uid, String extra){
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.beginObject();
            writer.name(MESSAGE_TYPE).value(CNMessageTypeToString(type));
            writer.name(CONFERENCE_ID).value(confId);
            writer.name(EXTRA_MESSAGE).value(extra);
            writer.name(DISPLAY_NAME).value(name);
            writer.name(UNIQUE_ID).value(uid);
            writer.endObject();
            writer.close();
            byte[] out_array = out.toByteArray();

            return new String(out_array);//android.util.Base64.encodeToString(out_array, 0, out_array.length, Base64.DEFAULT);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public CNMessage(String to, CNMessageType type, String confId, String name, String uid) throws InstantiationException {
        this(to, type, confId, name, uid, "");
    }

    public CNMessage(String to, CNMessageType type, String confId, String name, String uid, String extra) throws InstantiationException {
        super(to, buildMessageBody(type, confId, name, uid, extra));

        messageType = type;
        conferenceId = confId;
        displayName = name;
        uniqueId = uid;
        extraMessage = extra;
    }

    public CNMessage(ArrayList<String> toList, CNMessageType type, String confId, String name, String uid) throws InstantiationException {
        this(toList, type, confId, name, uid, "");
    }

    public CNMessage(ArrayList<String> toList, CNMessageType type, String confId, String name, String uid, String extra) throws InstantiationException {
        super(toList, buildMessageBody(type, confId, name, uid, extra));

        messageType = type;
        conferenceId = confId;
        displayName = name;
        uniqueId = uid;
        extraMessage = extra;
    }

    public CNMessage(Message message) throws InstantiationException {
        super(message);
        parseMessageBody(message);
    }

    public void parseMessageBody(Message message) throws InstantiationException {
        String body = message.getBody();
        LogSdk.d("CNMessage ","CNMessage :: "+body);
        if(body == null)
            return;

        try {
            //ByteArrayInputStream in = new ByteArrayInputStream(android.util.Base64.decode(body.getBytes(), Base64.DEFAULT));
            ByteArrayInputStream in = new ByteArrayInputStream(body.getBytes());
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.setLenient(true);
            reader.beginObject();
            while(reader.hasNext()){
                String name = reader.nextName();
                if(name.equals(MESSAGE_TYPE)){
                    messageType = CNMessageStringToType(reader.nextString());
                } else if(name.equals(CONFERENCE_ID)){
                    conferenceId = reader.nextString();
                } else if(name.equals(EXTRA_MESSAGE)){
                    extraMessage = reader.nextString();
                } else if(name.equals(DISPLAY_NAME)){
                    displayName = reader.nextString();
                } else if(name.equals(UNIQUE_ID)){
                    uniqueId = reader.nextString();
                }
            }
            reader.endObject();
            reader.close();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public CNMessageType getMessageType() {
        return messageType;
    }

    public String getConferenceId() {
        return conferenceId;
    }

    public String getExtraMessage() {
        return extraMessage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
