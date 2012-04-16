package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoClinicalFreeForm;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ClinicalParameterMap;
import org.owasp.validator.html.PolicyException;

public class ClinicalFreeFormJSON extends HttpServlet
{
	public static final String STUDY_ID = "studyId";
	
	private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Handles HTTP GET Request.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
    		HttpServletResponse httpServletResponse)throws ServletException,
            IOException {
        
        String studyId = httpServletRequest.getParameter(STUDY_ID);
        
        try
        {
        	 CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(studyId);
        	 // TODO check if cancerStudy really exists
        	 
             DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();
             HashSet<String> clinicalCaseSet = daoClinicalFreeForm.getAllCases(cancerStudy.getInternalId());
             HashSet<String> paramSet = daoClinicalFreeForm.getDistinctParameters(cancerStudy.getInternalId());
             
             // map to be send as a JSON value
             Map<String, Object> map = new HashMap<String, Object>();
             
             // add size of the clinical case set
             map.put("sizeOfSet", clinicalCaseSet.size());
             
             // add the rest of the table
             for (String param : paramSet) {
                 ClinicalParameterMap paramMap = daoClinicalFreeForm.getDataSlice(cancerStudy.getInternalId(), param);
                 HashSet<String> distinctCategorySet = paramMap.getDistinctCategories();
                 JSONArray distinctCategories = new JSONArray();
                 
                 for (String category : distinctCategorySet)
                 {
                	 if (category.trim().length() > 0)
                	 {
                		 distinctCategories.add(category);
                	 }
                 }
                 
                 map.put(param, distinctCategories);
             }
             
             httpServletResponse.setContentType("application/json");
             PrintWriter out = httpServletResponse.getWriter();
             
             try
             {
            	 JSONValue.writeJSONString(map, out);
             }
             finally
             {
                 out.close();
             }
        }
        catch (DaoException e) 
        {
            throw new ServletException(e);
        }
    }
}
