
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.commons.math.MathException;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import org.mskcc.portal.model.ProteinArrayInfo;
import org.mskcc.portal.remote.GetProteinArrayData;
import org.mskcc.portal.util.XDebug;

/**
 *
 * @author jj
 */
public class ProteinArraySignificanceTestJSON extends HttpServlet {

    public static final String HEAT_MAP = "heat_map";
    public static final String GENE = "gene";
    public static final String ALTERATION_TYPE = "alteration_type";
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        XDebug xdebug = new XDebug(request);
        
        // get heat map
        String heatMap = request.getParameter(HEAT_MAP);
        String strIndexGene = request.getParameter(GENE);
        int indexGene = strIndexGene==null ? 0:Integer.parseInt(strIndexGene); // start from 1, 0 means any
        String alterationType = request.getParameter(ALTERATION_TYPE);
        Map<String,Boolean> alterationMap = getAlterationList(heatMap, indexGene, alterationType);
        
        Map<String,ProteinArrayInfo> proteinArrays = GetProteinArrayData.getProteinArrayInfo(null, null, xdebug);
        Map<String,Map<String,Double>> proteinArrayData = GetProteinArrayData.getProteinArrayData(proteinArrays.keySet(), alterationMap.keySet(), xdebug);
        
        JSONArray table = new JSONArray();
        for (ProteinArrayInfo pai : proteinArrays.values()) {
            JSONArray row = new JSONArray();
            table.add(row);
            
            row.add(pai.getArrayType());
            row.add(pai.getGene());
            row.add(pai.getResidue());
            row.add(pai.getSource());
            row.add(pai.isValidated());
            
            Map<String,Double> data = proteinArrayData.get(pai.getArrayId());
            if (data==null) {
                row.add("NaN");
                row.add("NaN");
                row.add("NaN");
                row.add(";");
            }
            
            List<double[]> sepAbun = separateAbundance(alterationMap, data);
            double[] values = ttest(sepAbun.get(0),sepAbun.get(1));
            for (double d : values) {
                if (Double.isNaN(d))
                    row.add("NaN");
                else
                    row.add(Double.toString(d));
            }
            
            row.add(StringUtils.join(ArrayUtils.toObject(sepAbun.get(0)),",")+";"
                    +StringUtils.join(ArrayUtils.toObject(sepAbun.get(1)),","));
        }
                
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private Map<String,Boolean> getAlterationList(String heatMap, int indexGene, String alterationType)
            throws ServletException{
        Map<String,Boolean> ret = new HashMap<String,Boolean>();
        
        String[] lines = heatMap.split("\n");
        String[] genes = lines[0].split("\t");
        if (indexGene<0 || indexGene>=genes.length) {
            throw new ServletException("Gene index out of boundary");
        }
        
        for (int i=1; i<lines.length; i++) {
            String[] strs = lines[i].split("\t");
            if (indexGene==0) { // at least one gene
                boolean altered = false;
                for (int j=1; j<strs.length; j++) {
                    if (isAltered(strs[j],alterationType)) {
                        altered = true;
                        break;
                    }
                }
                ret.put(strs[0], altered);
            } else {
                ret.put(strs[0], isAltered(strs[indexGene],alterationType));
            }
        }
        
        return ret;
    }
    
    private List<double[]> separateAbundance(Map<String,Boolean> alterationMap, Map<String,Double> data) {        
        List<Double> alteredList = new ArrayList<Double>();
        List<Double> unalteredList = new ArrayList<Double>();
        
        for (Map.Entry<String,Double> entry : data.entrySet()) {
            Boolean altered = alterationMap.get(entry.getKey());
            if (altered!=null) {
                if (altered) {
                    alteredList.add(entry.getValue());
                } else {
                    unalteredList.add(entry.getValue());
                }
            }
        }
                
        double[] alteredArray = ArrayUtils.toPrimitive(alteredList.toArray(new Double[0]));
        double[] unalteredArray = ArrayUtils.toPrimitive(unalteredList.toArray(new Double[0]));
        
        return Arrays.asList(unalteredArray, alteredArray);
    }
    
    /**
     * 
     * @param alterationMap
     * @param data
     * @return [unaltered mean, altered mean, p-value]
     */
    private double[] ttest(double[] unalteredArray, double[] alteredArray) {        
        double alteredMean = StatUtils.mean(alteredArray);
        double unalteredMean = StatUtils.mean(unalteredArray);
        
        if (alteredArray.length<2 || unalteredArray.length<2)
            return new double[]{unalteredMean, alteredMean, Double.NaN};

        try {
            double pvalue = TestUtils.tTest(alteredArray, unalteredArray);
            return new double[]{unalteredMean, alteredMean, pvalue};
        } catch (MathException e) {
            return new double[]{unalteredMean, alteredMean, Double.NaN};
        }
    }
    
    private boolean isAltered(String alteration, String type) {
        if (alteration.isEmpty())
            return false;
        
        if (type==null) // any type
            return true;
        
        int index = alteration.indexOf(type+";");
        if (index==-1)
            return false;
        
        if (index==0)
            return true;
        
        if (alteration.charAt(index-1)==';')
            return true;
        
        return false;
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
