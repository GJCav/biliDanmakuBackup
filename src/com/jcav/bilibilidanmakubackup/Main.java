package com.jcav.bilibilidanmakubackup;

import com.jcav.bilibilidanmakubackup.cidtokenizer.CidTokenizer;
import com.jcav.bilibilidanmakubackup.cidtokenizer.VideoCidTokenizer;
import com.jcav.bilibilidanmakubackup.utilities.CookieUtilities;
import com.jcav.bilibilidanmakubackup.utilities.DanmakuXMLUtilities;
import com.jcav.bilibilidanmakubackup.utilities.IOUtilities;
import com.jcav.bilibilidanmakubackup.utilities.TimeUtilities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static void main(String[] args){
        Properties stg = new Properties();
        for(String s : args){
            int idx = s.indexOf("=");
            if(idx != -1){
                stg.setProperty(s.substring(0, idx), s.substring(idx + 1));
            }else{
                stg.setProperty(s, "");
            }
        }

        String timeoutStr = stg.getProperty("-timeout", "0");
        Global.timeout = Integer.valueOf(timeoutStr);

        String delayStr = stg.getProperty("-delay", "0");
        Global.delay = Integer.valueOf(delayStr);

        String datfStr = stg.getProperty("-datf", "1200000");
        Global.delayAfterTooFrequently = Integer.valueOf(datfStr);

        if(stg.containsKey("-b")){
            boolean valid = stg.containsKey("-st");
            valid &= stg.containsKey("-ed");
            valid &= stg.containsKey("-url");

            if(!valid) {
                System.out.println("Syntax error.");
                System.exit(0);
            }

            String url = stg.getProperty("-url");
            String start = stg.getProperty("-st");
            String end = stg.getProperty("-ed");

            String cookiePath = stg.getProperty("-cookie");
            String cookie = CookieUtilities.readCookieFromFile(cookiePath);
            Global.cookie = cookie;

            if(stg.containsKey("-history")){
                BackupHistory backup = new BackupHistory(
                        url,
                        start,
                        end
                );
                backup.start();
            }
        }
    }
}



