package com.codemine.talk2me;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by billy on 2016/12/5.
 */

public class Tools {
    /**
     * 获取当前时间
     * @return
     */
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }
}
