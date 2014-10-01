/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoDrug;
import org.mskcc.cbio.portal.dao.DaoDrugInteraction;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.Drug;
import org.mskcc.cbio.portal.model.DrugInteraction;

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
        Map<String,List<String>> drugInteractions = Collections.emptyMap();
        
        try {
            DaoDrug daoDrug = DaoDrug.getInstance();
            DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            
            drugs = daoDrug.getDrugs(Arrays.asList(drugIds));
            drugInteractions = new HashMap<String,List<String>>();
            for (DrugInteraction di : daoDrugInteraction.getTargets(drugs)) {
                List<String> dis = drugInteractions.get(di.getDrug());
                if (dis==null) {
                    dis = new ArrayList<String>();
                    drugInteractions.put(di.getDrug(), dis);
                }

                String symbolAllCaps = daoGene.getGene(di.getTargetGene()).getHugoGeneSymbolAllCaps();
                if(!dis.contains(symbolAllCaps))
                    dis.add(symbolAllCaps);
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        for (Drug drug : drugs) {
            exportDrug(table, drug, drugInteractions.get(drug.getId()));
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private void exportDrug(JSONArray table, Drug drug, List<String> targets) 
            throws ServletException {
        JSONArray row = new JSONArray();
        row.add(drug.getId());
        row.add(StringUtils.join(targets,", "));
        row.add(drug.getName());
        row.add(drug.getSynonyms().replaceAll(";","; "));
        row.add(drug.isApprovedFDA());
        row.add(drug.getDescription());
        row.add(drug.getResource());
        row.add(getXrefs(drug.getExternalReference()));
        row.add(drug.getNumberOfClinicalTrials());
        table.add(row);
    }
    
    private Map<String,String> getXrefs(String xref) {
        Map<String,String> map = new HashMap<String,String>();
        if (xref==null) {
            return map;
        }
        
        String[] parts = xref.split(";");
        for (String part : parts) {
            String[] strs = part.split(":",2);
            if (strs.length==2) {
                map.put(strs[0], strs[1]);
            }
        }
        
        return map;
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
