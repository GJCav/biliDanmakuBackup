package com.jcav.bilibilidanmakubackup.utilities;

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

    public static Date msAfter(Date date, int millisecond){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, millisecond);
        return cal.getTime();
    }

    /**
     *
     * @param millisecond
     * @return millisecond -> second
     */
    public static double ms2s(long millisecond){
        return millisecond / 1000.0;
    }

    /**
     * @param millisecond
     * @return millisecond -> minutes
     */
    public static double ms2m(long millisecond){
        return ms2s(millisecond) / 60.0;
    }

    public static Date lastDayOfMonth(Date month){
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        int maxDay = cal.getMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, maxDay);
        return cal.getTime();
    }

    public static Date someDaysAfter(Date date, int day){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, day);
        return cal.getTime();
    }

    public static Date firstDayOfThisMonth(Date curMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }
}
