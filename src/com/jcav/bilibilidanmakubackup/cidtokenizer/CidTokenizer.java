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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface CidTokenizer {
    /**
     * Everything is OK.
     */
    public static final int CODE_SUCCESS = 0;
    /**
     * When downloading the page, if an IOException is caught,
     * and the exception is not an InterruptedException instance,
     * this code is returned.
     */
    public static final int CODE_UNKNOWN_IO_ERROR = 1;
    /**
     * This code is used mostly because of the cid pattern is changed,
     * which means, bilibili has changed the html structure.
     * To solve this code message, contact the author of certain
     * CidTokenizer subclass and update it. <-_<-
     */
    public static final int CODE_PHRASE_ERROR = 2;
    /**
     * Used only if the user set a timeout, and it's exceeded.
     */
    public static final int CODE_TIMEOUT_ERROR = 3;

    /**
     * Invoke <code>getCide()</code> or <code>getCidList()</code> first!!!
     */
    public static final int CODE_BEFORE_RUN_ERROR = -1;

    /**
     * Used if the connection to the url is failed.
     */
    public static final int CODE_CONNECTION_FAILED = 4;

    /**
     *
     * @return error code.
     */
    public int getErrorCode();

    /*
    public default String getErrorMessage(int ec){
        switch (ec){
            case CODE_SUCCESS:
                return "success";
            case CODE_BEFORE_RUN_ERROR:
                return "invoke getCid() first";
            case CODE_CONNECTION_FAILED:
                return "connecting failed, check the network";
            case CODE_PHRASE_ERROR:
                return "parse error, check if the url is valid";
            case CODE_TIMEOUT_ERROR:
                return "timeout";
            case CODE_UNKNOWN_IO_ERROR:
                return "see IOException";
        }
        return "";
    }
    */

    /**
     * If everything is successful,
     * return the cid of this page,
     * otherwise return -1.
     * For more information, invoke
     * <code>getErrorCode</code>.
     * @return the cid in this page.
     */
    public int getCid();

    /**
     * @return the aid(or bangumi id) of the url.
     */
    public int getAid();

    public default List<Integer> getCidList() {
        return Arrays.asList(getCid());
    }

    /**
     * If the error code is CODE_UNKNOWN_IO_ERROR,
     * return the last IOException object,
     * otherwise return null.
     * @return IOException
     */
    public IOException getLastIOException();
}
