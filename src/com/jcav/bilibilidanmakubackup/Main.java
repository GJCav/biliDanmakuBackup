package com.jcav.bilibilidanmakubackup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static int delay = 0;
    private static int timeout = 0;

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
        timeout = Integer.valueOf(timeoutStr);

        String delayStr = stg.getProperty("-delay", "0");
        delay = Integer.valueOf(delayStr);

        if(stg.containsKey("-b")){
            boolean valid = stg.containsKey("-st");
            valid &= stg.containsKey("-ed");
            valid &= stg.containsKey("-cookie");
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
            if(cookie.equals("")){
                System.out.println("Cookie error.");
                System.exit(0);
            }

            if(stg.containsKey("-month")){
                backUpMonth(
                        url,
                        start,
                        end,
                        cookie,
                        timeout
                );
            }
        }
    }

    private static void backUpMonth(
            final String url,
            final String start,
            final String end,
            final String cookie,
            final int timeout
    ){
        VideoCidTokenizer cidToken = new VideoCidTokenizer(url, timeout);
        int cid = cidToken.getCid();

        if(cid == -1){
            System.out.println("Error: " + cidToken.getErrorCode());
            if(cidToken.getErrorCode() == CidTokenizer.CODE_UNKNOWN_IO_ERROR){
                System.out.println("Detail: ");
                cidToken.getLastIOException().printStackTrace(System.out);
            }
            System.exit(0);
        }

        File workDir = new File(
                "output",
                "av" + cidToken.getAid() + "_" + start + "_" + end
        );

        if(workDir.exists()){
            System.out.println("Backup has already existed.");
            System.exit(0);
        }
        workDir.mkdirs();

        File successListFile = new File(workDir, "successlist.txt");
        File failListFile = new File(workDir, "faillist.txt");
        File xmlDir = new File(workDir, "danmaku");
        if(!xmlDir.exists()) xmlDir.mkdirs();

        PrintWriter scsWriter = IOUtilities.createPrintWriter(successListFile);
        PrintWriter fldWriter = IOUtilities.createPrintWriter(failListFile);

        Date curMonth = TimeUtilities.parse(start, "yyyy-MM");
        final Date endMonth = TimeUtilities.parse(end, "yyyy-MM");

        while(curMonth.compareTo(endMonth) <= 0){
            delay();

            String mth = TimeUtilities.format(curMonth, "yyyy-MM");

            System.out.println("Backup month: " + mth);

            List<String> dateList = null;
            try{
                dateList = DanmakuXMLUtilities.getValidDateByMonth(
                        cid,
                        mth,
                        cookie,
                        timeout
                );
            }catch(Exception e){
                System.out.println("Cant download index of month: " + mth);
                System.out.println("Error Msg: " + e.getMessage());
                fldWriter.println("month: " + mth);
                continue;
            }

            for(String day : dateList){
                delay();

                System.out.println("Backup date " + day);

                String xml = null;
                try {
                    xml = DanmakuXMLUtilities.getHistoryDanmakuXML(
                            cid,
                            TimeUtilities.parse(day, "yyyy-MM-dd"),
                            cookie,
                            timeout
                    );
                } catch (IOException e) {
                    fldWriter.println("day: " + day);
                    System.out.println("Cant download danmaku of " + day);
                    System.out.println("Error Msg: " + e.getMessage());
                    continue;
                }

                if(xml.startsWith("{") && xml.endsWith("}")){
                    System.out.println("Request too frequently.");
                    fldWriter.println("too_frequently: " + day);

                    fldWriter.close();
                    scsWriter.close();
                    System.exit(-1);
                }

                File xmlFile = new File(xmlDir, day + ".xml");
                if(IOUtilities.saveToFile(xml, xmlFile)){
                    scsWriter.println("day: " + day);
                }else{
                    fldWriter.println("day:" + day);
                    System.out.println("Cant save danmaku of date " + day);
                }
            }

            curMonth = TimeUtilities.nextMonth(curMonth);
        }

        scsWriter.close();
        fldWriter.close();
    }

    public static void delay(){
        if(delay != 0){
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }
}



