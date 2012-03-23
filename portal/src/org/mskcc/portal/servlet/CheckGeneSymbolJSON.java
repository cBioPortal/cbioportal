package org.mskcc.portal.servlet;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A JSON servlet for checking gene symbols
 * Is it valid, if so is it an alias?
 * Returns valid gene symbols from the DB.
 *
 */
public class CheckGeneSymbolJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String GENES = "genes";

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
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        JSONArray geneArray = new JSONArray();
        String genes = httpServletRequest.getParameter(GENES);
        try {
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

            for(String symbol: genes.split(" ")) {
                Map map = new HashMap();
                JSONArray symbols = new JSONArray();
                for(CanonicalGene gene: daoGene.guessGene(symbol)) {
                    symbols.add(gene.getStandardSymbol());
                }
                map.put("symbols", symbols);
                map.put("name", symbol);

                geneArray.add(map);
            }

            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            try {
                JSONValue.writeJSONString(geneArray, out);
            } finally {
                out.close();
            }
        } catch (DaoException e) {
            throw new ServletException(e);
        }

    }
}
