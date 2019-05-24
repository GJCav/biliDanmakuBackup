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

package com.jcav.bilibilidanmakubackup;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcav.bilibilidanmakubackup.cidtokenizer.CidTokenizer;
import com.jcav.bilibilidanmakubackup.cidtokenizer.TokenizerGuilder;
import com.jcav.bilibilidanmakubackup.cidtokenizer.VideoCidTokenizer;
import com.jcav.bilibilidanmakubackup.utilities.DanmakuXMLUtilities;
import com.jcav.bilibilidanmakubackup.utilities.IOUtilities;
import com.jcav.bilibilidanmakubackup.utilities.TimeUtilities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import static com.jcav.bilibilidanmakubackup.Global.cookie;
import static com.jcav.bilibilidanmakubackup.Global.delay;
import static com.jcav.bilibilidanmakubackup.Global.timeout;
import static com.jcav.bilibilidanmakubackup.Global.delayAfterTooFrequently;

/**
 * backup danmaku from start to end.
 */
public class BackupHistory {
    private static final String TIME_PATTERN = "\\d{4}-\\d{2}";
    private final String url, start, end;

    public BackupHistory(
            String url,
            String start,
            String end
    ) {
        this.url = url;
        this.start = start;
        this.end = end;

        if(!start.matches(TIME_PATTERN) ||  !end.matches(TIME_PATTERN))
            throw new IllegalArgumentException("Syntax error");

        if(cookie.equals("")){
            throw new IllegalArgumentException("Cookie not found.");
        }

        if(TimeUtilities.parse(start, "yyyy-MM")
                .compareTo(TimeUtilities.parse(end, "yyyy-MM")) > 0){
            throw new IllegalArgumentException("Syntax error");
        }
    }

    public void start(){
        CidTokenizer cidToken = TokenizerGuilder.ins.getCidTokenizerByURL(url, Global.timeout);
        if(cidToken == null){
            System.out.println("There is no proper CidTokenizer. Sorry.");
            System.exit(0);
        }

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
                    JsonObject errObj = new JsonParser().parse(xml).getAsJsonObject();
                    System.out.println("Error xml: " + xml);

                    int code = errObj.get("code").getAsInt();
                    if(code == -101){
                        System.out.println("Reset cookies...");
                        System.exit(0);
                    }

                    if(code != -509){
                        System.out.println("unknown error.");
                        fldWriter.println(xml);
                        fldWriter.close();
                        scsWriter.close();
                        System.exit(0);
                    }

                    System.out.println("Request too frequently.");
                    System.out.println(
                            String.format(
                                    "Program will sleep about %.2f mins. (Wake up at %s)",
                                    TimeUtilities.ms2m(delayAfterTooFrequently),
                                    TimeUtilities.format(
                                            TimeUtilities.msAfter(new Date(), delayAfterTooFrequently),
                                            "HH:mm:ss"
                                    )
                            )
                    );
                    delayAfterTooFrequently();
                    // reset to yesterday
                    curMonth = TimeUtilities.yesterday(curMonth);
                    continue;
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

    private void delay(){
        if(delay != 0){
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }

    private void delayAfterTooFrequently(){
        if(delayAfterTooFrequently != 0){
            try {
                Thread.sleep(delayAfterTooFrequently);
            } catch (InterruptedException e) {
            }
        }
    }
}
