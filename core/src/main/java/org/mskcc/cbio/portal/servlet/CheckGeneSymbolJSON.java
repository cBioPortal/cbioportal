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

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.util.OncoPrintSpecificationDriver;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
        new HashMap();
        JSONArray geneArray = new JSONArray();
        String genes = httpServletRequest.getParameter(GENES);
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        //  Use the OQL Parser to Extract the Gene Symbols
        ParserOutput parserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(genes);
        ArrayList<String> geneList = new ArrayList<String>();
        geneList.addAll(parserOutput.getTheOncoPrintSpecification().listOfGenes());

        for(String symbol: geneList) {
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

    }
}
