package com.jcav.bilibilidanmakubackup;

import com.jcav.bilibilidanmakubackup.cidtokenizer.CidTokenizer;
import com.jcav.bilibilidanmakubackup.cidtokenizer.TokenizerGuilder;
import com.jcav.bilibilidanmakubackup.utilities.DanmakuXMLUtilities;
import com.jcav.bilibilidanmakubackup.utilities.IOUtilities;
import com.jcav.bilibilidanmakubackup.utilities.TimeUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static com.jcav.bilibilidanmakubackup.Global.timeout;
import static com.jcav.bilibilidanmakubackup.Global.cookie;

public class BackupSingleDay {
    private final String url, date, outDir;

    public BackupSingleDay(String url, String date, String outDir) {
        this.url = url;
        this.date = date;
        this.outDir = outDir;

        if(!date.matches("\\d{4}-\\d{2}-\\d{2}")){
            throw new IllegalArgumentException("Syntax error.");
        }

        if(IOUtilities.hasIllegalChar(outDir)){
            System.out.println("Argument illegal: '-out="+outDir+ "'");
            System.out.println("Chars below are illegal in system file system. Don't use them:");
            System.out.println("    " + IOUtilities.ILLEGAL_PATH_CHARS);
            System.exit(0);
        }
    }

    public void start(){
        CidTokenizer cidToken = TokenizerGuilder.ins.getCidTokenizerByURL(url, timeout);
        if(cidToken == null){
            System.out.println("No proper CidTokenizer. Sorry.");
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

        String xmlName = "av" + cidToken.getAid() + "_" + date + ".xml";
        File outDirPos = new File(this.outDir);
        if(!outDirPos.exists()) outDirPos.mkdirs();
        File outFile = new File(this.outDir, xmlName);
        if(outFile.exists()){
            System.out.println("Backup existed. At: " + outFile.toPath());
            System.exit(0);
        }

        Date date = TimeUtilities.parse(this.date, "yyyy-MM-dd");
        try {
            String xml = DanmakuXMLUtilities.getHistoryDanmakuXML(cid, date, cookie, timeout);
            if(xml.startsWith("{")){
                System.out.println("Error: " + xml);
                System.exit(0);
            }
            IOUtilities.saveToFile(xml, outFile);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("Success.");
        System.out.println("Save xml file at: " + outFile.getPath());
    }
}





