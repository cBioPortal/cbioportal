/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/


package org.mskcc.cbio.portal.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import org.owasp.validator.html.PolicyException;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author jj
 */
public class BoxPlotServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(BoxPlotServlet.class);

    public static final String PLOT_WIDTH = "width";
    public static final String PLOT_HEIGHT = "height";
    public static final String DATA = "data";
    public static final String XLABEL = "xlabel";
    public static final String YLABEL = "ylabel";
    public static final String FORMAT = "format";

    private static ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException (e);
        }
    }
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> 
     * methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // get data
            String format = servletXssUtil.getCleanInput(request, FORMAT);
            String strData = servletXssUtil.getCleanInput(request, DATA);
            String xlabel = servletXssUtil.getCleanInput(request, XLABEL);
            String ylabel = servletXssUtil.getCleanInput(request, YLABEL);
            String width = servletXssUtil.getCleanInput(request, PLOT_WIDTH);
            String height = servletXssUtil.getCleanInput(request, PLOT_HEIGHT);
            
            xlabel = WordUtils.wrap(StringEscapeUtils.unescapeHtml(xlabel), 60, "\\n", false);
            ylabel = StringEscapeUtils.unescapeHtml(ylabel);

            if (format == null || !format.equals("pdf")) {
                format = "png"; // default is png
            }

            List<Double> values = new ArrayList<Double>();
            List<String> tags = new ArrayList<String>();

            for (String groupData : strData.split(";")) {
                if (groupData.isEmpty()) {
                    continue;
                }

                int ix = groupData.indexOf(':');
                if (ix == -1 || ix == groupData.length() - 1) {
                    continue;
                }

                String group = groupData.substring(0, ix);
                for (String value : groupData.substring(ix + 1).split(",")) {
                    values.add(Double.parseDouble(value));
                    tags.add(group);
                }
            }

            StringBuilder plot = new StringBuilder();

            String tmpfile = "tmp" + request.getRequestedSessionId()
                    + String.valueOf(System.currentTimeMillis() + "." + format);

            plot.append("library(Cairo);\n");
            if (format.equals("pdf")) {
                plot.append("pdf(width=6, height=6, file='");
                plot.append(tmpfile);
                plot.append("');\n");
            } else {
                plot.append("Cairo(width=");
                plot.append(width);
                plot.append(", height=");
                plot.append(height);
                plot.append(", file='");
                plot.append(tmpfile);
                plot.append("', type='");
                plot.append(format);
                plot.append("', units=\"px\")\n");
            }

            plot.append("data=c(");
            plot.append(StringUtils.join(values,","));
            plot.append(");\n");

            plot.append("group=c('");
            plot.append(StringUtils.join(tags,"','"));
            plot.append("');\n");

            plot.append("boxplot(data~group,outline=F,ylim=(c(min(data),max(data))),xlab='");
            plot.append(xlabel);
            plot.append("',ylab='");
            plot.append(ylabel);
            plot.append("');\n");

            plot.append("stripchart(data~group, add=T, pch=16, vertical=T, method='jitter', jitter=0.1);\n");
            plot.append ("dev.off();\n");

            logger.debug("Call to R Follows:");
            logger.debug(plot.toString());
            
            //response.getWriter().print(plot);

            RConnection c = new RConnection();

            // open device
            try {
                c.parseAndEval(plot.toString());
            } catch (org.rosuda.REngine.REngineException e) {
                if (!new File(tmpfile).exists()) {
                    throw e;
                }
            }
            
            // There is no I/O API in REngine because it's actually more efficient to use R for this
            // we limit the file size to 1MB which should be sufficient and we delete the file as well
            REXP xp = c.parseAndEval("r=readBin('" + tmpfile
                    + "','raw',1024*1024); unlink('" + tmpfile + "'); r;");

            // now this is pretty boring AWT stuff - create an image from the data and display it ...
            byte[] imageBytes = xp.asBytes();
            response.setContentLength(imageBytes.length);
            response.getOutputStream().write(imageBytes);
            c.close();

            if (format.equals("pdf")) {
                response.setContentType("application/pdf");
            } else {
                response.setContentType("image/png");
            }
        } catch (Exception e) {
            //  In the event of an exception, redirect to the Plot NA Image.
            logger.error(e);
            response.sendRedirect("images/plots_na.png");
        }
    }
    
    

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
