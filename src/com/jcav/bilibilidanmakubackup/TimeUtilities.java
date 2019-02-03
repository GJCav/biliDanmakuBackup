package com.jcav.bilibilidanmakubackup;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class TimeUtilities {
    public static Calendar getCalendar(String str, String pattern){
        Calendar cal = Calendar.getInstance();
        cal.setTime(parse(str, pattern));
        return cal;
    }

    public static Date parse(String str, String pattern){
        return new SimpleDateFormat(pattern).parse(str, new ParsePosition(0));
    }

    public static String format(Date date, String pat){
        return new SimpleDateFormat(pat).format(date);
    }

    public static Date yesterday(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    public static Date tomorrow(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, +1);
        return cal.getTime();
    }

    public static Date nextMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, +1);
        return cal.getTime();
    }

    public static Date lastMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }
}
