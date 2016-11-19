package com.codemine.talk2me;

import android.os.Message;

/**
 * Created by billy on 2016/11/18.
 */

public class MyMessage {
    public static Message createMessage(int messageWhat) {
        Message message = new Message();
        message.what = messageWhat;
        return message;
    }
}
