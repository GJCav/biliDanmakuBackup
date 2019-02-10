package com.jcav.bilibilidanmakubackup;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcav.bilibilidanmakubackup.cidtokenizer.CidTokenizer;
import com.jcav.bilibilidanmakubackup.cidtokenizer.TokenizerGuilder;
import com.jcav.bilibilidanmakubackup.utilities.DanmakuXMLUtilities;
import com.jcav.bilibilidanmakubackup.utilities.IOUtilities;
import com.jcav.bilibilidanmakubackup.utilities.TimeUtilities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jcav.bilibilidanmakubackup.Global.cookie;
import static com.jcav.bilibilidanmakubackup.Global.delayAfterTooFrequently;
import static com.jcav.bilibilidanmakubackup.Global.timeout;

public class BackupHistorySmart {
    private static final String TIME_PATTERN = "\\d{4}-\\d{2}";
    private static final String DANMAKU_PARAM_PATTERN = "<d p=\"(.*?)\">";
    private final String url, start, end;

    private int cid = -1;
    private final Set<Long> lastXmlIds;
    private String lastXml = "";

    public BackupHistorySmart(
            String url,
            String start,
            String end
    ) {
        this.url = url;
        this.start = start;
        this.end = end;
        lastXmlIds = new HashSet<>();

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

    /**
     * Return the danmaku ids for the first and the last danmaku in xml.
     * @param xml
     * @return
     */
    private long[] getHeadAndTailDid(String xml){
        long[] rtn = new long[]{-1, -1};
        Pattern pat = Pattern.compile(DANMAKU_PARAM_PATTERN);

        String firstParam = null;
        String lastParam = null;

        Matcher mat = pat.matcher(xml);
        if(mat.find()){
            // first danmaku id
            firstParam = mat.group(1);
            lastParam = mat.group(1);
        }

        while(mat.find()){
            // last danmaku id
            lastParam = mat.group(1);
        }

        if(firstParam != null){
            rtn[0] = parseDanmakuId(firstParam);
        }

        if(lastParam != null){
            rtn[1] = parseDanmakuId(lastParam);
        }
        return rtn;
    }

    private void resetDanmakuIdSet(String xml){
        final Pattern pat = Pattern.compile(DANMAKU_PARAM_PATTERN);
        final Matcher mat = pat.matcher(xml);
        lastXmlIds.clear();
        while(mat.find()){
            lastXmlIds.add(parseDanmakuId(mat.group(1)));
        }
    }

    private long parseDanmakuId(String param){
        String[] token = param.split(",");
        return Long.valueOf(token[7]);
    }

    public void start(){
        CidTokenizer cidToken = TokenizerGuilder.ins.getCidTokenizerByURL(url, Global.timeout);
        if(cidToken == null){
            System.out.println("There is no proper CidTokenizer. Sorry.");
            System.exit(0);
        }

        cid = cidToken.getCid();

        if(cid == -1){
            System.out.println("Error: " + cidToken.getErrorCode());
            if(cidToken.getErrorCode() == CidTokenizer.CODE_UNKNOWN_IO_ERROR){
                System.out.println("Detail: ");
                cidToken.getLastIOException().printStackTrace(System.out);
            }
            System.exit(0);
        }

        final File workDir = new File(
                "output",
                "av" + cidToken.getAid() + "_" + start + "_" + end
        );

        if(workDir.exists()){
            System.out.println("Backup has already existed.");
            System.exit(0);
        }
        workDir.mkdirs();

        final File successListFile = new File(workDir, "successlist.txt");
        final File failListFile = new File(workDir, "faillist.txt");
        final File xmlDir = new File(workDir, "danmaku");
        if(!xmlDir.exists()) xmlDir.mkdirs();

        PrintWriter scsWriter = IOUtilities.createPrintWriter(successListFile);
        PrintWriter fldWriter = IOUtilities.createPrintWriter(failListFile);

        Date curMonth = TimeUtilities.parse(start, "yyyy-MM");
        final Date endMonth = TimeUtilities.parse(end, "yyyy-MM");
        final Date endDate = TimeUtilities.lastDayOfMonth(endMonth);
        Date curDate = TimeUtilities.firstDayOfThisMonth(curMonth);

        // move to first danmaku date.
        while(lastXml.equals("") && curDate.compareTo(endDate) <= 0) {
            delay();

            String date = TimeUtilities.format(curDate, "yyyy-MM-dd");
            System.out.println("Test day: " + date);
            try {
                lastXml = DanmakuXMLUtilities.getHistoryDanmakuXML(
                        cid,
                        curDate,
                        cookie,
                        timeout
                );
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Retry...");
                continue;
            }

            int errorCode = getErrorCode(lastXml);
            if(errorCode == 200){
                if(getHeadAndTailDid(lastXml)[0] != -1){
                    break;
                }
            }else if(errorCode == -101){
                System.out.println("Reset cookies...");
                System.exit(0);
            }else if(errorCode == -504){
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
                continue;
            }else{
                System.out.println("Unknown error: " + lastXml);
                System.exit(0);
            }

            curDate = TimeUtilities.tomorrow(curDate);
            lastXml = "";
        }

        // now we get the first danmaku file.
        while(curDate.compareTo(endDate) <= 0){
            delay();

            String date = TimeUtilities.format(curDate, "yyyy-MM-dd");
            IOUtilities.saveToFile(lastXml, new File(xmlDir, date + ".xml"));
            System.out.println("Backup date: " + date);
            scsWriter.println("day: " + date);

            curDate = nextBackupDate(curDate, endDate);
        }

        scsWriter.close();
        fldWriter.close();
        System.out.println("Done.");
    }

    private Date nextBackupDate(Date curDate, final Date endDay){
        resetDanmakuIdSet(lastXml);

        int p = 1;
        int next = 1;
        String curXml = "";
        Date testDate = null;
        long lastDanmakuId = -1;

        while(p > 0 && curDate.compareTo(endDay) < 0){
            delay();

            testDate = TimeUtilities.someDaysAfter(curDate, next + p);
            System.out.println("Test day: "
                    + TimeUtilities.format(testDate, "yyyy-MM-dd"));

            if(testDate.compareTo(endDay) > 0){
                p /= 2;
                next += p;
                break;
            }

            curXml = downloadDanmaku(testDate);
            lastDanmakuId = getHeadAndTailDid(curXml)[1];

            if(lastXmlIds.contains(lastDanmakuId)){
                p *= 2;
            }else{
                p /= 2;
                next += p;
                break;
            }
        }

        p /= 2;
        while(p > 0 && curDate.compareTo(endDay) < 0){
            delay();

            testDate = TimeUtilities.someDaysAfter(curDate, next + p);
            System.out.println("Test day: "
                    + TimeUtilities.format(testDate, "yyyy-MM-dd"));

            if(testDate.compareTo(endDay) > 0){
                p /= 2;
                continue;
            }

            curXml = downloadDanmaku(testDate);
            lastDanmakuId = getHeadAndTailDid(curXml)[1];

            if(lastXmlIds.contains(lastDanmakuId)){
                next += p;
                p /= 2;
            }else{
                p /= 2;
            }
        }

        curDate = TimeUtilities.someDaysAfter(curDate, next);
        lastXml = downloadDanmaku(curDate);
        return curDate;
    }

    private String downloadDanmaku(Date date){
        String xml = "";
        try {
            xml = DanmakuXMLUtilities.getHistoryDanmakuXML(
                 cid,
                 date,
                 Global.cookie,
                 Global.timeout
            );
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(0);
        }

        int errorCode = getErrorCode(xml);
        if(errorCode == -101){
            System.out.println("Reset cookies...");
            System.exit(0);
        }else if(errorCode == -509){
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
            return downloadDanmaku(date);
        }
        return xml;
    }

    private void delayAfterTooFrequently() {
        try {
            Thread.sleep(delayAfterTooFrequently);
        } catch (InterruptedException e) {
        }
    }

    private int getErrorCode(String lastXml) {
        if(!lastXml.startsWith("{")) return 200;
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(lastXml).getAsJsonObject();
        return obj.get("code").getAsInt();
    }

    private void delay(){
        try {
            Thread.sleep(Global.delay);
        } catch (InterruptedException e) {
        }
    }
}




