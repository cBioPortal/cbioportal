
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.cgds.model.ExtendedMutation;

/**
 *
 * @author jj
 */
public class MutationsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(MutationsJSON.class);

    public static final String PATIENT = "patient";
    //public static final String GENETIC_PROFILE_ID = "genetic_profile_id";
    
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

        // get heat map
        String patient = request.getParameter(PATIENT);
        List<ExtendedMutation> mutations;
        try {
            mutations = DaoMutation.getInstance().getMutations(patient);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        for (ExtendedMutation mutation : mutations) {
            export(table, mutation);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
            //out.print(JSONValue.toJSONString(table));
        } finally {            
            out.close();
        }
    }
    
    private void export(JSONArray table, ExtendedMutation mutation) {
        JSONArray row = new JSONArray();
        row.add(mutation.getGeneSymbol());
        row.add(mutation.getAminoAcidChange());
        row.add(mutation.getMutationType());
        row.add(mutation.getMutationStatus());
        // TODO: clinical trial
        row.add("pending");
        // TODO: context
        row.add("pending");
        // TODO: annotation
        row.add("pending");
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
