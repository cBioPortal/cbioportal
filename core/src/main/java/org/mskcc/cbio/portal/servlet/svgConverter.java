package org.mskcc.cbio.portal.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class svgConverter extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OutputStream out = response.getOutputStream();
        try {
            String svgContent = request.getParameter("svgelement");
            String docRoot = "<?xml version='1.0' encoding='utf-8'?>";
            String docRoot2 = "<svg version='1.1' width='700px' height='600px' xml:space='preserve'>";
            svgContent = docRoot + docRoot2 + svgContent + "</svg>";
            InputStream is = new ByteArrayInputStream(svgContent.getBytes());
            TranscoderInput input = new TranscoderInput(is);
            TranscoderOutput output = new TranscoderOutput(out);
            Transcoder transcoder = new PDFTranscoder();
            response.setContentType("application/pdf");
            response.setHeader("content-disposition", "inline; filename='result.pdf'");
            transcoder.transcode(input, output);
        } catch (Exception e) {
            System.err.println(e.toString());
        } finally {
            out.close();
        }
    }
    public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}