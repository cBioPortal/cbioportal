/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.servlet;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.XssRequestWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * A JSON servlet for checking gene symbols
 * Is it valid, if so is it an alias?
 * Returns valid gene symbols from the DB.
 *
 */
public class CheckGeneSymbolJSON extends HttpServlet {
    public static final String GENES = "genes";

    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException
    {
        this.doPost(request, response);
    }


    /**
     * Handles HTTP GET Request.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        new HashMap();
        JSONArray geneArray = new JSONArray();
        String genes = httpServletRequest.getParameter(GENES);

	    // we need the raw gene list
	    if (httpServletRequest instanceof XssRequestWrapper)
	    {
		    genes = ((XssRequestWrapper)httpServletRequest).getRawParameter(GENES);
	    }

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        //  Use the OQL Parser to Extract the Gene Symbols
        String[] geneList;
				if (genes.length() > 0) {
					geneList = genes.split(",");
				} else {
					geneList = new String[0];
				}

        for(String symbol: geneList) {
            Map map = new HashMap();
            JSONArray symbols = new JSONArray();
            for(CanonicalGene gene: daoGene.guessGene(symbol)) {
                symbols.add(gene.getStandardSymbol());
            }
            
            map.put("name", symbol);
            map.put("symbols", symbols);
            
            // If no valid match then get suggested symbols
            if (symbols.isEmpty()) {
            	JSONArray suggestions = new JSONArray();
            	suggestions.addAll(daoGene.getGeneSuggestions(symbol));
                map.put("suggestions", suggestions);
            }

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
