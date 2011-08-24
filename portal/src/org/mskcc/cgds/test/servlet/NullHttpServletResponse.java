package org.mskcc.cgds.test.servlet;

/*
 * from: http://www.java2s.com/Open-Source/Java-Document/Content-Management-System/TransferCM/com/methodhead/shim/NullHttpServletResponse.java.htm
 */

/* 
 * Copyright (C) 2006 Methodhead Software LLC.  All rights reserved.
 * 
 * This file is part of TransferCM.
 * 
 * TransferCM is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * TransferCM is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * TransferCM; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA  02110-1301  USA
 */

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletResponse;
import javax.servlet.ServletOutputStream;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.Cookie;

/**
 * A servlet response that discards any output.  All methods of this class are
 * empty implementations.
 */
public class NullHttpServletResponse implements  HttpServletResponse {

    // constructors /////////////////////////////////////////////////////////////

    public NullHttpServletResponse() {
        out_ = new NullServletOutputStream();
    }

    // constants ////////////////////////////////////////////////////////////////

    // classes //////////////////////////////////////////////////////////////////

    private static class NullServletOutputStream extends
            ServletOutputStream {

        public void write(int b) {
            // do nothing
        }
    }

    // methods //////////////////////////////////////////////////////////////////

    public void flushBuffer() {
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        return "";
    }

    public Locale getLocale() {
        return Locale.getDefault();
    }

    public ServletOutputStream getOutputStream() {
        return out_;
    }

    public PrintWriter getWriter() {
       return new MyPrintWriter( myStringWriter );
//       return new PrintWriter( System.err );
// was        return new PrintWriter(out_);
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
    }

    public void resetBuffer() {
    }

    public void setBufferSize(int size) {
    }

    public void setContentLength(int len) {
    }

    public void setContentType(String type) {
    }

    public void setLocale(Locale loc) {
    }

    public void setResponse(ServletResponse response) {
    }

    public void addCookie(Cookie cookie) {
    }

    public void addDateHeader(String name, long date) {
    }

    public void addHeader(String name, String value) {
    }

    public void addIntHeader(String name, int value) {
    }

    public boolean containsHeader(String name) {
        return false;
    }

    public String encodeRedirectUrl(String url) {
        return url;
    }

    public String encodeRedirectURL(String url) {
        return url;
    }

    public String encodeUrl(String url) {
        return url;
    }

    public String encodeURL(String url) {
        return url;
    }

    public void sendError(int sc) {
    }

    public void sendError(int sc, String msg) {
    }

    public void sendRedirect(String location) {
    }

    public void setDateHeader(String name, long date) {
    }

    public void setHeader(String name, String value) {
    }

    public void setIntHeader(String name, int value) {
    }

    public void setStatus(int sc) {
    }

    public void setStatus(int sc, String sm) {
    }

    // properties ///////////////////////////////////////////////////////////////

    ServletOutputStream out_ =  null; // new NullServletOutputStream();;
    StringWriter myStringWriter = new StringWriter();
    
    public String getOutput(){
       return myStringWriter.toString();
    }

    // attributes ///////////////////////////////////////////////////////////////
}