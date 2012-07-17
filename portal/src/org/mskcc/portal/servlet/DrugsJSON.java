
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.*;

/**
 *
 * @author jj
 */
public class DrugsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(DrugsJSON.class);
    
    public static final String DRUG_IDS = "drug_ids";
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONArray table = new JSONArray();

        String strDrugIds = request.getParameter(DRUG_IDS);
        String[] drugIds = strDrugIds.split("[ ,]+");
        
        List<Drug> drugs = Collections.emptyList();
        
        try {
            drugs = DaoDrug.getInstance().getDrugs(Arrays.asList(drugIds));
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        for (Drug drug : drugs) {
            exportDrug(table, drug);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private void exportDrug(JSONArray table, Drug drug) 
            throws ServletException {
        JSONArray row = new JSONArray();
        row.add(drug.getId());
        row.add(drug.getName());
        row.add(drug.getSynonyms());
        row.add(drug.isApprovedFDA());
        row.add(drug.getDescription());
        row.add(drug.getResource());
        table.add(row);
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
