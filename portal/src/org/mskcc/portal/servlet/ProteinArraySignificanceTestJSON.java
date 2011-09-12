
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.log4j.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.ProteinArrayInfo;
import org.mskcc.portal.remote.GetProteinArrayData;

import org.owasp.validator.html.PolicyException;

/**
 *
 * @author jj
 */
public class ProteinArraySignificanceTestJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(ProteinArraySignificanceTestJSON.class);

    public static final String HEAT_MAP = "heat_map";
    public static final String GENE = "gene";
    public static final String ALTERATION_TYPE = "alteration";
    public static final String ANTIBODY_TYPE = "antibody";
    public static final String EXCLUDE_ANTIBODY_TYPE = "exclude_antibody";
    public static final String DATA_SCALE = "data_scale";

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
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JSONArray table = new JSONArray();

            // get heat map
            String heatMap = servletXssUtil.getCleanInput(request, HEAT_MAP);
            String gene = servletXssUtil.getCleanInput(request, GENE);
            String alterationType = servletXssUtil.getCleanInput(request, ALTERATION_TYPE);
            String antibodyType = servletXssUtil.getCleanInput(request, ANTIBODY_TYPE);
            String excludeAntibodyType = servletXssUtil.getCleanInput(request, EXCLUDE_ANTIBODY_TYPE);
            String strDataScale = servletXssUtil.getCleanInput(request, DATA_SCALE);
            double dataScale = strDataScale==null?0:Double.parseDouble(strDataScale);

            Collection<String> antibodyTypes;
            if (antibodyType==null) {
                if (excludeAntibodyType == null) {
                    antibodyTypes = null; // include all
                } else {
                    try {
                        antibodyTypes = GetProteinArrayData.getProteinArrayTypes();
                        antibodyTypes.removeAll(Arrays.asList(excludeAntibodyType.split(" ")));
                    } catch (DaoException e) {
                        throw new ServletException(e);
                    }
                }
            } else {
                antibodyTypes = Arrays.asList(antibodyType.split(" "));
            }

            String[] heatMapLines = heatMap.split("\r?\n");
            String[] genes = heatMapLines[0].split("\t");
            genes[0] = "Any";
            Set<String> allCases = getAllCases(heatMapLines);
            Map<String,Set<String>>[] alteredCases = getAlteredCases(heatMapLines, genes, gene, alterationType);

            Map<String,ProteinArrayInfo> proteinArrays;
            Map<String,Map<String,Double>> proteinArrayData;
            try {
                proteinArrays = GetProteinArrayData.getProteinArrayInfo(null, antibodyTypes);
                proteinArrayData = GetProteinArrayData.getProteinArrayData(proteinArrays.keySet(), allCases);
            } catch (DaoException e) {
                throw new ServletException(e);
            }

            if (gene==null) {        
                for (int i=0; i<genes.length; i++) {
                    export(table, genes[i], alteredCases[i], proteinArrays, proteinArrayData, dataScale);
                }
            } else {
                export(table, gene, alteredCases[0], proteinArrays, proteinArrayData, dataScale);
            }

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            try {
                JSONValue.writeJSONString(table, out);
                //out.print(JSONValue.toJSONString(table));
            } finally {            
                out.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    
    private void export(JSONArray table,
        String gene,
        Map<String,Set<String>> mapAlterationAltereCases,
        Map<String,ProteinArrayInfo> proteinArrays,
        Map<String,Map<String,Double>> proteinArrayData,
        double dataScale) {
        for (Map.Entry<String,Set<String>> entry : mapAlterationAltereCases.entrySet()) {
            String alteration = entry.getKey();
            Set<String> altered = entry.getValue();
//                if (altered.size()<2 || allCases.size()-altered.size()<2)
//                    continue;

            for (ProteinArrayInfo pai : proteinArrays.values()) {
                JSONArray row = new JSONArray();

                row.add(pai.getId());
                row.add(gene);
                row.add(alteration);
                row.add(pai.getType());
                row.add(pai.getGene());
                row.add(pai.getResidue());
                row.add(pai.getSource());
                row.add(pai.isValidated());

                Map<String,Double> data = proteinArrayData.get(pai.getId());
                if (data==null) {
                    row.add("NaN");
                    row.add("NaN");
                    row.add("NaN");
                    row.add("");
                } else {
                    List<double[]> sepAbun = separateAbundance(altered, data);
                    double[] values = ttest(sepAbun.get(0),sepAbun.get(1));
                    for (double d : values) {
                        if (Double.isNaN(d))
                            row.add("NaN");
                        else
                            row.add(Double.toString(d));
                    }
                    row.add(groupData2String(sepAbun,dataScale));
                }
                row.add(""); // dumb row for plot

                table.add(row);
            }
        }
    }
    
    static Map<String,Set<String>>[] getAlteredCases(String[] heatMapLines, String[] genes, String gene,
            String alterationType)  throws ServletException{
        Map<String,Set<String>>[] alteredCases;
        
        int ixGene = 0;
        if (gene==null) {
            alteredCases = getAlteredCases(heatMapLines, genes.length);
        } else {
            for (; ixGene<genes.length; ixGene++) {
                if (genes[ixGene].equals(gene))
                    break;
            }
            Set<String> set = getAlteredCases(heatMapLines, ixGene, alterationType);
            Map<String,Set<String>> map = Collections.singletonMap(gene, set);
            alteredCases = new Map[1];
            alteredCases[0] = map;
        }
        
        return alteredCases;
    }
    
    static Set<String> getAlteredCases(String[] lines, int ixGene,
            String alterationType) throws ServletException {
        Set<String> ret = new HashSet<String>();
        for (int i=1; i<lines.length; i++) {
            String[] parts = lines[i].split("\t",ixGene+2);
            if (ixGene==0) { // any
                if (alterationType.equals("Any")) {
                    if (parts[1].matches(".*[A-Za-z]+.*")) {
                        ret.add(parts[0]);
                    }
                } else {
                    if (parts[1].equals(alterationType)
                            || parts[1].matches("^"+alterationType+"[;\\t]")
                            || parts[1].matches(".+[;\\t]"+alterationType+"[;\\t]")
                            || parts[1].matches(".+[;\\t]"+alterationType+"$")) {
                        ret.add(parts[0]);
                    }
                }
            } else {
                if (alterationType.equals("Any")) {
                    if (parts[ixGene].matches(".*[A-Za-z]+.*")) {
                        ret.add(parts[0]);
                    }
                } else {
                    if (parts[ixGene].equals(alterationType)
                            || parts[ixGene].matches("^"+alterationType+"[;\\t]")
                            || parts[ixGene].matches(".+[;\\t]"+alterationType+"[;\\t]")
                            || parts[ixGene].matches(".+[;\\t]"+alterationType+"$")) {
                        ret.add(parts[0]);
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * 
     * @param heatMap
     * @param indexGene
     * @param alterationType
     * @return array of map from alteration type to set of altered cases
     * @throws ServletException 
     */
    static Map<String,Set<String>>[] getAlteredCases(String[] lines, int nGenes)
            throws ServletException {  
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
    
    static Set<String> getAllCases(String[] lines) {
        Set<String> cases = new HashSet<String>();
        for (int i=1; i<lines.length; i++) {
            cases.add(lines[i].substring(0, lines[i].indexOf('\t')));
        }
        return cases;
    }
    
    static List<double[]> separateAbundance(Set<String> alteredCases, Map<String,Double> data) {        
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
    
    static String groupData2String(List<double[]> sepAbun, double dataScale) {
        StringBuilder sbdata = new StringBuilder();
        if (sepAbun.get(0).length>0) {
            sbdata.append("Unaltered:");
//            sbdata.append(StringUtils.join(ArrayUtils.toObject(sepAbun.get(0)),","));
            sbdata.append(encodingAbun(sepAbun.get(0),dataScale));
            sbdata.append(';');
        }
        if (sepAbun.get(1).length>0) {
            sbdata.append("Altered:");
//            sbdata.append(StringUtils.join(ArrayUtils.toObject(sepAbun.get(1)),","));
            sbdata.append(encodingAbun(sepAbun.get(1),dataScale));
            sbdata.append(';');
        }
        return sbdata.toString();
    }
    
    static String encodingAbun(double[] abun,double scale) {
        if (abun.length==0)
            return "";
        
        StringBuilder sb = new StringBuilder(Double.toString(encodingAbun(abun[0],scale)));
        for (int i=1; i<abun.length; i++) {
            sb.append(',');
            sb.append(encodingAbun(abun[i],scale));
        }
        
        return sb.toString();
    }
    
    static double encodingAbun(double abun,double scale) {
        if (scale==0)
            return abun;
        return ((int)(abun*scale))/scale;
    }
    
    /**
     * 
     * @param alterationMap
     * @param data
     * @return [unaltered mean, altered mean, p-value]
     */
    private static double[] ttest(double[] unalteredArray, double[] alteredArray) {        
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
