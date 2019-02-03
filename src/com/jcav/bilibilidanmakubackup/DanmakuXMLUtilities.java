package com.jcav.bilibilidanmakubackup;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Give me a cid, give you the danmaku xml file.
 * (〃'▽'〃)
 */
public final class DanmakuXMLUtilities {
    public static String DEFAULT_URL_PATTERN =
            "https://api.bilibili.com/x/v1/dm/list.so?oid=%d";
    public static String HISTORY_URL_PATTERN =
            "https://api.bilibili.com/x/v2/dm/history?type=1&oid=%d&date=%s";

    public static String getHistoryDanmakuXML(int cid, Date date, String cookieStr) throws IOException {
        return getHistoryDanmakuXML(cid, date, cookieStr, 0);
    }

    public static String getHistoryDanmakuXML(int cid, Date date, String cookieStr, int timeout) throws IOException {
        String time = TimeUtilities.format(date, "yyyy-MM-dd");
        String url = String.format(HISTORY_URL_PATTERN, cid, time);
        HttpURLConnection cn = IOUtilities.createHttpsConnection(url, null);
        cn.setRequestProperty("Cookie", cookieStr);
        return IOUtilities.downloadAsString(cn, timeout);
    }

    public static String getDanmakuXML(int cid) throws IOException {
        return getDanmakuXML(cid, 0);
    }

    public static String getDanmakuXML(int cid, int timeout) throws IOException {
        String url = String.format(DEFAULT_URL_PATTERN, cid);
        String xml = null;
        HttpURLConnection cn = IOUtilities.createHttpsConnection(url, null);
        xml = IOUtilities.downloadAsString(cn, timeout);
        return xml;
    }

    public static List<String> getValidDateByMonth(
            int cid,
            String month,
            String cookieStr
    ) throws IOException{
        return getValidDateByMonth(cid, month, cookieStr, 0);
    }

    public static List<String> getValidDateByMonth(
            int cid,
            String month,
            String cookieStr,
            int timeout
    ) throws IOException{
        final String PATTERN = "https://api.bilibili.com/x/v2/dm/history/index?" +
                "type=1&oid=%d&month=%s";

        String url = String.format(PATTERN, cid, month);

        HttpURLConnection cn = IOUtilities.createHttpsConnection(url, null);
        cn.setRequestProperty("Cookie", cookieStr);

        String json = IOUtilities.downloadAsString(cn, timeout);
        if(json == null || json.equals(""))
            throw new IOException("Wrong json format.");

        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();

        if(obj.get("data").isJsonNull()) return new ArrayList<>(0);

        List<String> list = new ArrayList<>();
        for(JsonElement e : obj.get("data").getAsJsonArray()){
            list.add(e.getAsString());
        }

        return list;
    }
}
