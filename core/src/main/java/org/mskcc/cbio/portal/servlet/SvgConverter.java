package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
import org.owasp.validator.html.PolicyException;

public class svgConverter extends HttpServlet {

    private Pattern svgXPosPattern;
    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {

        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
            svgXPosPattern = Pattern.compile("( x=\"(\\d+)\")");
        }
        catch (PolicyException e) {
            throw new ServletException (e);
        }
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

        String xml = "";
        String format = "";
        if (httpServletRequest instanceof FileUploadRequestWrapper) {

            // get instance of our request wrapper
            FileUploadRequestWrapper wrapper = (FileUploadRequestWrapper)httpServletRequest;

            // get format parameter
            format = wrapper.getParameter("filetype");

            // get xml parameter
            xml = wrapper.getParameter("svgelement");
        }
        else {
            format = servletXssUtil.getCleanInput(httpServletRequest, "filetype");
            // TODO - update antisamy.xml to support svg-xml
            xml = httpServletRequest.getParameter("svgelement");
        }

        if (format.equals("pdf")) {
            convertToPDF(httpServletResponse, xml);
        } else if (format.equals("png")) {
            convertToPNG(httpServletResponse, xml);
        } else if (format.equals("svg")) {
            convertToSVG(httpServletResponse, xml);
        }
    }

    /**
     * Return svg xml as it is for downloading
     *
     * @param response
     * @param xml
     * @throws ServletException
     * @throws IOException
     */
    private void convertToSVG(HttpServletResponse response, String xml) throws ServletException, IOException {

        try {
            response.setContentType("application/svg+xml");
            response.setHeader("content-disposition", "inline; filename='plots.svg'");
            PrintWriter writer = response.getWriter();
            try {
                writer.write(xml);
            }
            finally {
                writer.flush();
                writer.close();
            }
        }
        catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Convert svg xml to pdf and writes it to the response
     *
     * @param response
     * @param xml
     * @throws ServletException
     * @throws IOException
     */
    private void convertToPDF(HttpServletResponse response, String xml) throws ServletException, IOException {
        OutputStream out = response.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            TranscoderInput input = new TranscoderInput(is);
            TranscoderOutput output = new TranscoderOutput(out);
            Transcoder transcoder = new PDFTranscoder();
            transcoder.addTranscodingHint(PDFTranscoder.KEY_XML_PARSER_CLASSNAME, "org.apache.xerces.parsers.SAXParser");
            response.setContentType("application/pdf");
            response.setHeader("content-disposition", "inline; filename='plots.pdf'");
            transcoder.transcode(input, output);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Convert svg xml to PNG and writes it to the response
     *
     * @param response
     * @param xml
     * @throws ServletException
     * @throws IOException
     */
    private void convertToPNG(HttpServletResponse response, String xml) throws ServletException, IOException {
        OutputStream out = response.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            TranscoderInput input = new TranscoderInput(is);
            TranscoderOutput output = new TranscoderOutput(out);
            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_XML_PARSER_CLASSNAME, "org.apache.xerces.parsers.SAXParser");
            transcoder.addTranscodingHint( PNGTranscoder.KEY_WIDTH, new Float(1500));
            transcoder.addTranscodingHint( PNGTranscoder.KEY_HEIGHT, new Float(1500));
            response.setContentType("application/png");
            response.setHeader("content-disposition", "inline; filename='plots.png'");
            transcoder.transcode(input, output);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Method called when exception occurs.
     *
     * @param servletContext ServletContext
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param xdebug XDebug
     */
    private static void forwardToErrorPage(ServletContext servletContext,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           XDebug xdebug) throws ServletException, IOException {

        request.setAttribute("xdebug_object", xdebug);
        RequestDispatcher dispatcher = servletContext.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
        dispatcher.forward(request, response);
    }
}
