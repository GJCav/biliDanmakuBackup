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

public class BackupCurrent {
    private final String url, outDir;

    public BackupCurrent(String url, String outDir) {
        this.url = url;
        this.outDir = outDir;

        if(IOUtilities.hasIllegalChar(outDir)){
            System.out.println("Argument Error: '-out="+outDir+ "'");
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

        String date = TimeUtilities.format(new Date(), "yyyy-MM-dd");
        String xmlName = "av" + cidToken.getAid() + "_cur" + date + ".xml";
        File outPos = new File(this.outDir);
        if(!outPos.exists()){
            outPos.mkdirs();
        }
        File outFile = new File(this.outDir, xmlName);
        if(outFile.exists()){
            System.out.println("Backup existed. At: " + outFile.toPath());
            System.exit(0);
        }

        try{
            String xml = DanmakuXMLUtilities.getDanmakuXML(cid, timeout);
            if(xml.startsWith("{")){
                System.out.println("Error: " + xml);
                System.exit(0);
            }
            IOUtilities.saveToFile(xml, outFile);
            System.out.println("Success.");
            System.out.println("Save xml file at: " + outFile.getPath());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(0);
        }
    }
}
