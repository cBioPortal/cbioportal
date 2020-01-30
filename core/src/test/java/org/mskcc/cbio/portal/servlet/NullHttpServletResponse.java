/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
/*
 * from: http://www.java2s.com/Open-Source/Java-Document/Content-Management-System/
 * TransferCM/com/methodhead/shim/NullHttpServletResponse.java.htm
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

/**
 * A servlet response that discards any output.  All methods of this class are
 * empty implementations.
 */
public class NullHttpServletResponse implements HttpServletResponse {

    // constructors /////////////////////////////////////////////////////////////

    public NullHttpServletResponse() {
        servletOutputStream = new NullServletOutputStream();
    }

    // constants ////////////////////////////////////////////////////////////////

    // classes //////////////////////////////////////////////////////////////////

    /**
     * An OutputStream implementation for JUnit tests.
     */
    private static class NullServletOutputStream extends ServletOutputStream {

        public void write(int b) {
            // do nothing
        }

        public boolean isReady() {
            return false;
        }
    }

    // methods //////////////////////////////////////////////////////////////////

    public void flushBuffer() {}

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
        return servletOutputStream;
    }

    public PrintWriter getWriter() {
        return new MyPrintWriter(myStringWriter);
        //       return new PrintWriter( System.err );
        // was        return new PrintWriter(servletOutputStream);
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {}

    public void resetBuffer() {}

    public void setBufferSize(int size) {}

    public void setContentLength(int len) {}

    public void setContentType(String type) {}

    public void setLocale(Locale loc) {}

    public void setResponse(ServletResponse response) {}

    public void addCookie(Cookie cookie) {}

    public void addDateHeader(String name, long date) {}

    public void addHeader(String name, String value) {}

    public void addIntHeader(String name, int value) {}

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

    public void sendError(int sc) {}

    public void sendError(int sc, String msg) {}

    public void sendRedirect(String location) {}

    public void setDateHeader(String name, long date) {}

    public void setHeader(String name, String value) {}

    public void setIntHeader(String name, int value) {}

    public void setStatus(int sc) {}

    public void setStatus(int sc, String sm) {}

    public Collection<String> getHeaderNames() {
        return null;
    }

    public Collection<String> getHeaders(String name) {
        return null;
    }

    public int getStatus() {
        return 0;
    }

    public void setContentLengthLong(long len) {}

    public void setCharacterEncoding(String s) {}

    public String getContentType() {
        return null;
    }

    // properties ///////////////////////////////////////////////////////////////

    private ServletOutputStream servletOutputStream = null; // new NullServletOutputStream();;
    private StringWriter myStringWriter = new StringWriter();

    public String getOutput() {
        return myStringWriter.toString();
    }

    public String getHeader(String name) {
        return null;
    }
    // attributes ///////////////////////////////////////////////////////////////
}
