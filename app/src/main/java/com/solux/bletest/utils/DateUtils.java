package com.solux.bletest.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Create by qindl
 * on 2018/10/15
 */
public class DateUtils {
    public static String getCurrentTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }
}
