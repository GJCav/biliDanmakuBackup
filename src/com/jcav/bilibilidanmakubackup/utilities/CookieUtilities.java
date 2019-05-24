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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
