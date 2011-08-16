
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        JSONArray table = new JSONArray();
        
        // get heat map
        String heatMap = request.getParameter(HEAT_MAP);
        //String strIndexGene = request.getParameter(GENE);
        //int indexGene = strIndexGene==null ? 0:Integer.parseInt(strIndexGene); // start from 1, 0 means any
        //String alterationType = request.getParameter(ALTERATION_TYPE);
        String[] heatMapLines = heatMap.split("\n");
        String[] genes = heatMapLines[0].split("\t");
        genes[0] = "Any";
        Set<String> allCases = getAllCases(heatMapLines);
        Map<String,Set<String>>[] alteredCases = getAlteredCases(heatMapLines, genes.length);
        
        Map<String,ProteinArrayInfo> proteinArrays = GetProteinArrayData.getProteinArrayInfo(null, null, xdebug);
        Map<String,Map<String,Double>> proteinArrayData = GetProteinArrayData.getProteinArrayData(proteinArrays.keySet(), allCases, xdebug);
        
        for (int i=0; i<genes.length; i++) {
            Map<String,Set<String>> mapAlterationAltereCases = alteredCases[i];
            for (Map.Entry<String,Set<String>> entry : mapAlterationAltereCases.entrySet()) {
                String alteration = entry.getKey();
                Set<String> altered = entry.getValue();
//                if (altered.size()<2 || allCases.size()-altered.size()<2)
//                    continue;
                
                for (ProteinArrayInfo pai : proteinArrays.values()) {
                    JSONArray row = new JSONArray();
                    
                    row.add(genes[i]);
                    row.add(alteration);
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
                    } else {
                        List<double[]> sepAbun = separateAbundance(altered, data);
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
                    
                    table.add(row);
                }
            }
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
    
    /**
     * 
     * @param heatMap
     * @param indexGene
     * @param alterationType
     * @return array of map from alteration type to set of altered cases
     * @throws ServletException 
     */
    private Map<String,Set<String>>[] getAlteredCases(String[] lines, int nGenes)
            throws ServletException{  
        Map<String,Set<String>>[] ret = new HashMap[nGenes];
        for (int i=0; i<nGenes; i++) {
            ret[i] = new HashMap<String,Set<String>>();
            ret[i].put("Any", new HashSet<String>());
        }
        
        for (int i=1; i<lines.length; i++) {
            String[] strs = lines[i].split("\t");
            for (int j=1; j<strs.length; j++) {
                if (!strs[j].isEmpty()) {
                    ret[j].get("Any").add(strs[0]);
                    ret[0].get("Any").add(strs[0]);
                }
                
                for (String type : strs[j].split(";")) {
                    // add to specific type
                    Set<String> cases = ret[j].get(type);
                    if (cases==null) {
                        cases = new HashSet<String>();
                        ret[j].put(type, cases);
                    }
                    cases.add(strs[0]);
                    
                    // add to any type
                    cases = ret[0].get(type);
                    if (cases==null) {
                        cases = new HashSet<String>();
                        ret[0].put(type, cases);
                    }
                    cases.add(strs[0]);
                }
            }
        }
        
        return ret;
    }
    
    private Set<String> getAllCases(String[] lines) {
        Set<String> cases = new HashSet<String>();
        for (int i=1; i<lines.length; i++) {
            cases.add(lines[i].substring(0, lines[i].indexOf('\t')));
        }
        return cases;
    }
    
    private List<double[]> separateAbundance(Set<String> alteredCases, Map<String,Double> data) {        
        List<Double> alteredList = new ArrayList<Double>();
        List<Double> unalteredList = new ArrayList<Double>();
        
        for (Map.Entry<String,Double> entry : data.entrySet()) {
            if (alteredCases.contains(entry.getKey())) {
                alteredList.add(entry.getValue());
            } else {
                unalteredList.add(entry.getValue());
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
        } catch (Exception e) {
            return new double[]{unalteredMean, alteredMean, Double.NaN};
        }
    }
    
//    private boolean isAltered(String alteration, String type) {
//        if (alteration.isEmpty())
//            return false;
//        
//        if (type==null) // any type
//            return true;
//        
//        int index = alteration.indexOf(type+";");
//        if (index==-1)
//            return false;
//        
//        if (index==0)
//            return true;
//        
//        if (alteration.charAt(index-1)==';')
//            return true;
//        
//        return false;
//    }

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
