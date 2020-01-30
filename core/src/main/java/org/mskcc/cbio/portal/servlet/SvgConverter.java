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

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.fop.svg.PDFTranscoder;
import org.mskcc.cbio.portal.util.FileUploadRequestWrapper;
import org.mskcc.cbio.portal.util.XDebug;
import org.mskcc.cbio.portal.util.XssRequestWrapper;
import org.owasp.validator.html.PolicyException;

public class SvgConverter extends HttpServlet {
    private Pattern svgXPosPattern;
    private static String DEFAULT_FILENAME = "result";

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
        svgXPosPattern = Pattern.compile("( x=\"(\\d+)\")");
    }

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doGet(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
        // setup a debug object
        XDebug xdebug = new XDebug(httpServletRequest);
        xdebug.logMsg(this, "Attempting to parse request parameters.");

        String format = httpServletRequest.getParameter("filetype");
        String xml = httpServletRequest.getParameter("svgelement");

        // TODO - update antisamy.xml to support svg-xml
        if (httpServletRequest instanceof XssRequestWrapper) {
            format =
                ((XssRequestWrapper) httpServletRequest).getRawParameter(
                        "filetype"
                    );
            xml =
                ((XssRequestWrapper) httpServletRequest).getRawParameter(
                        "svgelement"
                    );
        }

        if (format.equals("pdf_data")) {
            convertToPDF(httpServletResponse, xml);
        } else if (format.equals("png_data")) {
            convertToPNG(httpServletResponse, xml);
        }
    }

    /**
     * Converts svg xml to pdf and writes it to the response as
     * a Base 64 encoded string.
     *
     * @param response
     * @param xml
     * @throws ServletException
     * @throws IOException
     */
    private void convertToPDF(HttpServletResponse response, String xml)
        throws ServletException, IOException {
        OutputStream out = response.getOutputStream();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            TranscoderInput input = new TranscoderInput(is);
            TranscoderOutput output = new TranscoderOutput(byteOut);
            Transcoder transcoder = new PDFTranscoder();
            transcoder.addTranscodingHint(
                PDFTranscoder.KEY_XML_PARSER_CLASSNAME,
                "org.apache.xerces.parsers.SAXParser"
            );
            response.setContentType("application/pdf");
            transcoder.transcode(input, output);
            byteOut.close();
            out.write(Base64.encodeBase64(byteOut.toByteArray()));
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    private void convertToPNG(HttpServletResponse response, String xml)
        throws ServletException, IOException {
        OutputStream out = response.getOutputStream();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            TranscoderInput input = new TranscoderInput(is);
            TranscoderOutput output = new TranscoderOutput(byteOut);
            Transcoder transcoder = new PNGTranscoder();
            transcoder.transcode(input, output);
            byteOut.close();
            out.write(Base64.encodeBase64(byteOut.toByteArray()));
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
