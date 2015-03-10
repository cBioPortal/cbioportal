package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.fileupload.FileItem;
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
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException,
            IOException {

        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException, IOException {

        // setup a debug object
        XDebug xdebug = new XDebug( httpServletRequest );
        xdebug.logMsg(this, "Attempting to parse request parameters.");

        String format = httpServletRequest.getParameter("filetype");
        String xml = httpServletRequest.getParameter("svgelement");

	    // TODO - update antisamy.xml to support svg-xml
	    if (httpServletRequest instanceof XssRequestWrapper)
	    {
		    xml = ((XssRequestWrapper) httpServletRequest).getRawParameter("svgelement");
	    }

	    if (format.equals("pdf_data"))
	    {
		    convertToPDF(httpServletResponse, xml);
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
					"org.apache.xerces.parsers.SAXParser");
			response.setContentType("application/pdf");
			transcoder.transcode(input, output);
			byteOut.close();
			out.write(Base64.encodeBase64(byteOut.toByteArray()));
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
}