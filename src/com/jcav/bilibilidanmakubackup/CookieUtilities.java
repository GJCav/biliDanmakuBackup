package com.jcav.bilibilidanmakubackup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CookieUtilities {
    public static String COOKIE_PATTERN =
            "\"name\":\\s*?\"(.*?)\",.*?\"value\":\\s*?\"(.*?)\"";

    public static String readCookieFromFile(String path){
        return readCookieFromFile(new File(path));
    }

    public static String readCookieFromFile(File file){
        Pattern pat = Pattern.compile(COOKIE_PATTERN, Pattern.DOTALL);
        String str = IOUtilities.readAllString(file);
        if(str == null) return "";
        List<Pair<String, String>> list = new ArrayList<>();
        Matcher mat = pat.matcher(str);
        while(mat.find()){
            list.add(new Pair<>(mat.group(1), mat.group(2)));
        }
        return list
                .stream()
                .map(e -> e.first + "=" + e.second)
                .collect(Collectors.joining("; "));
    }
}
