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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;

import org.mskcc.cbio.portal.util.WebserviceParserUtils;
import org.mskcc.cbio.portal.web_api.GetProteinArrayData;

import org.owasp.validator.html.PolicyException;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author jj
 */
public class ScatterPlotServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(ScatterPlotServlet.class);

    public static final String PLOT_WIDTH = "width";
    public static final String PLOT_HEIGHT = "height";
    public static final String LABEL = "label";
    public static final String DATA = "data";
    public static final String DATA_TYPE = "datatype";
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
            String strLabel = servletXssUtil.getCleanInput(request, LABEL);
            String strData = servletXssUtil.getCleanInput(request, DATA);
            String width = servletXssUtil.getCleanInput(request, PLOT_WIDTH);
            String height = servletXssUtil.getCleanInput(request, PLOT_HEIGHT);

            String[] labels = strLabel.split(",");
            
            // we probably want a servlet for RPPA plot
            String dataType = servletXssUtil.getCleanInput(request, DATA_TYPE);
            if(dataType.equalsIgnoreCase("RPPA")) {
                strData = getRPPAData(labels, request);
            }
            
            boolean pdf = format!=null && format.equals("pdf");
            if (width==null) {
                width = pdf ? "6" : "600";
            } 
            
            if (height==null) {
                height = pdf ? "6" : "600";
            } 
            
            if (!pdf) {
                format = "png"; // default is png
            }

            StringBuilder plot = new StringBuilder();

            String tmpfile = "tmp" + request.getRequestedSessionId()
                    + String.valueOf(System.currentTimeMillis() + "." + format);

            plot.append("data=data.frame(matrix(c(");
            plot.append(strData);
            plot.append("),ncol=");
            plot.append(labels.length);
            //plot.append(",nrow=");
            //plot.append()
            plot.append("));\n");
            plot.append("names(data)=c('");
            plot.append(strLabel.replaceAll(",", "','"));
            plot.append("');\n");
            
            plot.append("library(Cairo);\n");
            if (pdf) {
                plot.append("pdf(width=");
                plot.append(width);
                plot.append(", height=");
                plot.append(height);
                plot.append(", file='");
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

            plot.append("plot(data);\n");
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
            throw new ServletException(e);
            //response.sendRedirect("images/plots_na.png");
        }
    }
    
    public String getRPPAData(String[] arrayIds, HttpServletRequest request) throws Exception {
        ArrayList<String> cases = WebserviceParserUtils.getCaseList(request);
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(
                WebserviceParserUtils.getCancerStudyIDs(request).iterator().next())
                .getInternalId();
        Map<String,Map<String,Double>> map = GetProteinArrayData.getProteinArrayData(
                cancerStudyId, Arrays.asList(arrayIds), cases);
        HashSet<String> overlapCases = new HashSet<String>(cases);
        for (Map<String,Double> vcases : map.values()) {
            overlapCases.retainAll(vcases.keySet());
        }
        
        StringBuilder sb = new StringBuilder();
        for (String arrayId : arrayIds) {
            Map<String,Double> vcases = map.get(arrayId);
            for (String vcase : overlapCases) {
                sb.append(vcases.get(vcase)).append(',');
            }
        }
        sb.deleteCharAt(sb.length()-1);
        
        return sb.toString();
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
