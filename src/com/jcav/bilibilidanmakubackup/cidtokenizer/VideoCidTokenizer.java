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

package com.jcav.bilibilidanmakubackup.cidtokenizer;

import com.jcav.bilibilidanmakubackup.utilities.IOUtilities;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Get a single cid in a page which url
 * matches <code>www.bilibili.com/video/av\d+</>.
 * If this page contains various cids, get the
 * first one or the one specified by url tag <code>p</>.
 * For instance, if url contains "p=3", then
 * this will get the cid of the third episode.
 */
public class VideoCidTokenizer extends AbCidTokenizer {
    public static final int CODE_PAGE_OUT_OF_RANGE_ERROR = 100;
    public static final String URL_PATTERN =
            ".*www.bilibili.com/video/av(\\d+).*";
    public static final String DEFAULT_PATTERN =
            "\"cid=(\\d+?)&aid=";
    public static final String EACH_PAGE_PATTERN =
            "\"cid\":(\\d+?),\\.*?\"page\":(\\d+),";
    public static final String URL_PAGE_TAG_PATTERN =
            "p=(\\d+)";


    private int aid = -1;
    private Map<Integer, Integer> cidMap;
    private int errorCode = CidTokenizer.CODE_BEFORE_RUN_ERROR;
    private IOException lastError = null;
    private String parseErrorHtml = null;
    private int cid = -1;

    /**
     * @param url - the url of the video page
     * @param timeout - 0 implies infinite timeout
     */
    public VideoCidTokenizer(String url, int timeout){
        super(url, timeout);
        if(!url.matches(URL_PATTERN))
            throw new IllegalArgumentException("URL illegal.");

        this.url = url;
        cidMap = new HashMap<>();
        this.timeout = timeout;
    }

    public VideoCidTokenizer(String url){
        this(url, 0);
    }

    @Override
    public int getAid(){
        if(aid != -1) return aid;

        Pattern pat = Pattern.compile(URL_PATTERN);
        Matcher mat = pat.matcher(url);
        mat.find();
        aid = Integer.valueOf(mat.group(1));
        return aid;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    private void init(){
        cidMap.clear();
        errorCode = CidTokenizer.CODE_BEFORE_RUN_ERROR;
        lastError = null;
        parseErrorHtml = null;
    }

    public void reset(){
        cid = -1;
        init();
    }

    /**
     * If epIndex equals 0, this return the same as <code>getCid()</code>.
     * Otherwise return the cid of the given episode.
     * @param epIndex
     * @return cid, or -1 if epIndex isn't existed.
     */
    public int getCid(int epIndex){
        if(errorCode == CODE_BEFORE_RUN_ERROR)
            getCid();
        return cidMap.getOrDefault(epIndex, -1);
    }

    /**
     * Download the target url and parse it to get cid.
     * If a previous invocation existed, return the
     * last result.
     * If a new result is needed, invoke <code>reset()</code>,
     * then invoke this.
     * @return
     */
    @Override
    public int getCid() {
        if(errorCode == CODE_SUCCESS) return cid;

        init();
        HttpURLConnection cn = null;
        try {
            cn = IOUtilities.createHttpsConnection(url, null);
        } catch (IOException e) {
            errorCode = CidTokenizer.CODE_CONNECTION_FAILED;
            lastError = e;
            return -1;
        }

        if(cn == null) return -1;

        String html = null;
        try {
            html = IOUtilities.downloadAsString(cn, timeout);
        } catch (SocketTimeoutException e) {
            errorCode = CidTokenizer.CODE_TIMEOUT_ERROR;
            lastError = null;
            return -1;
        } catch (IOException e){
            errorCode = CidTokenizer.CODE_UNKNOWN_IO_ERROR;
            lastError = e;
            return -1;
        }

        if(html == null) return -1;

        Pattern defaultPat = Pattern.compile(DEFAULT_PATTERN);
        Matcher mat = defaultPat.matcher(html);

        if(!mat.find()){
            errorCode = CidTokenizer.CODE_PHRASE_ERROR;
            parseErrorHtml = html;
            return -1;
        }

        cid = Integer.valueOf(mat.group(1));
        errorCode = CidTokenizer.CODE_SUCCESS;
        lastError = null;

        Pattern epPat = Pattern.compile(EACH_PAGE_PATTERN);
        mat = epPat.matcher(html);

        while(mat.find()){
            cidMap.put(
                    Integer.valueOf(mat.group(2)),
                    Integer.valueOf(mat.group(1))
            );
        }

        Pattern pagePat = Pattern.compile(URL_PAGE_TAG_PATTERN);
        mat = pagePat.matcher(url);
        if(mat.find()){
            int p = Integer.valueOf(mat.group(1));
            cid = getCid(p);
            if(cid == -1)
                errorCode = CODE_PAGE_OUT_OF_RANGE_ERROR;
        }

        cidMap.put(0, cid);
        return cid;
    }

    /**
     * Use <code>getCid(epIndex)</code> instead.
     * @return [getCid(), ep1Cid, ep2Cid, ep3Cid, ...]
     */
    @Override
    public List<Integer> getCidList() {
        if(errorCode == CODE_BEFORE_RUN_ERROR)
            getCid();
        return cidMap
                .entrySet()
                .stream()
                .sorted((a, b) -> a.getKey() - b.getKey())
                .map(a -> a.getValue())
                .collect(Collectors.toList());
    }

    @Override
    public IOException getLastIOException() {
        return lastError;
    }
}
