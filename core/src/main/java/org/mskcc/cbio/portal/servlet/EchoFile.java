/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.servlet;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.owasp.validator.html.PolicyException;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class EchoFile extends HttpServlet {

    public static final int MAX_NO_GENES = 30;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {

        super.init();
    }

    /**
     *
     * If you specify the `str` parameter in the request, the servlet echoes back the string as is.
     *
     * If you specify files in the request, each file gets echoed back as a json object
     * { name -> string (content of file)}
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        Writer writer = response.getWriter();

        try {

            String str = request.getParameter("str");

            if (str != null) {
                writer.write(request.getParameter("str"));
                return;
            }

            Map fieldName2fileContent = new HashMap<String, String>();

            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);

            for (FileItem item : items) {
                if (item.getSize() == 0) {
                    // skip empty files
                    continue;
                }

                InputStream contentStream = item.getInputStream();
                String fieldName = item.getFieldName();

                // slurp the file as a string
                String encoding = "UTF-8";
                StringWriter stringWriter = new StringWriter();
                IOUtils.copy(contentStream, stringWriter, encoding);
                String contentString = stringWriter.toString();

                fieldName2fileContent.put(fieldName, contentString);
            }

            // write the objects out as json
            response.setContentType("application/json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, fieldName2fileContent);
        }

        // catch all exceptions
        catch (Exception e) {

            // "log" it
            System.out.println(e);

            // hide details from user
            throw new ServletException("there was an error processing your request");
        }
    }

    /**
     * Forwards to doPost
     *
     * doGet == doPost
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
