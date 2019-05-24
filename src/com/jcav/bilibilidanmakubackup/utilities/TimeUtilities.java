/*
 * Copyright (c) 2019 JCav <825195983@qq.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
