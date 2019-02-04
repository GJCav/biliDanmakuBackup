package com.jcav.bilibilidanmakubackup.cidtokenizer;

import java.io.IOException;

public abstract class AbCidTokenizer implements CidTokenizer {
    protected String url;
    protected int timeout;
    public AbCidTokenizer(String url, int timeout){
        this.url = url;
        this.timeout = timeout;
    }
}
