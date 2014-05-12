/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
/*
import cpath.client.CPath2Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.sbgn.L3ToSBGNPDConverter;
import org.biopax.paxtools.io.sbgn.ListUbiqueDetector;
import org.biopax.paxtools.io.sbgn.idmapping.HGNC;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Xref;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

import javax.servlet.ServletException;*/
import javax.servlet.http.HttpServlet;
/*import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
*/
/**
 * Servlet class to request SBGN directly from cpath web service.
 */
public class NetworkSbgnServlet extends HttpServlet
{
/*    private final static Log log = LogFactory.getLog(NetworkSbgnServlet.class);

	public final static String HGNC_GENE_PREFIX = "urn:biopax:RelationshipXref:HGNC_HGNC%3A";
	public final static String CPATH_SERVICE = "http://awabi.cbio.mskcc.org/cpath2/";
	public final static String NA = "NA";
    public final static Integer GRAPH_QUERY_LIMIT = 1;
    public final static String ATTRIBUTES_FIELD = "attributes";
    public final static String SBGN_FIELD = "sbgn";
    public final static String GENES_FIELD = "genes";

    private static ArrayList<String> convert(String[] geneList) {
		ArrayList<String> convertedList = new ArrayList<String>();
		DaoGeneOptimized daoGeneOptimized;

		daoGeneOptimized = DaoGeneOptimized.getInstance();

		for(String gene: geneList) {
			CanonicalGene cGene = daoGeneOptimized.getGene(gene);
            String hgncId = HGNC.getID(cGene.getHugoGeneSymbolAllCaps());
            convertedList.add(HGNC_GENE_PREFIX.replace("+", "%2B") + hgncId);
        }

		return convertedList;
	}

	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)throws ServletException, IOException
	{
		doPost(httpServletRequest, httpServletResponse);
	}

	protected void doPost(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)throws ServletException, IOException
	{
		PrintWriter out = httpServletResponse.getWriter();
		String sourceSymbols = httpServletRequest.getParameter(QueryBuilder.GENE_LIST);

		String[] sourceGeneSet = sourceSymbols.split("\\s");

        CPath2Client client = CPath2Client.newInstance();
        client.setEndPointURL(CPATH_SERVICE);
        client.setGraphQueryLimit(GRAPH_QUERY_LIMIT);
        client.setDirection(CPath2Client.Direction.BOTHSTREAM);

        ArrayList<String> convertedList = convert(sourceGeneSet);
        Model model = convertedList.size() > 1
                ? client.getPathsBetween(convertedList)
                : client.getNeighborhood(convertedList);

        L3ToSBGNPDConverter converter
                = new L3ToSBGNPDConverter(new ListUbiqueDetector(new HashSet<String>()), null, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        converter.writeSBGN(model, byteArrayOutputStream);

        // This is going to be our ultimate output
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put(SBGN_FIELD, byteArrayOutputStream.toString());

        // This will populate the genes field with all the genes found in the BioPAX model
        outputMap.put(GENES_FIELD, extractGenes(model));

        // The following will include RDF Id -> attributes map
        HashMap<String, Map<String, List<String>>> attrMap = new HashMap<String, Map<String, List<String>>>();
        for (BioPAXElement bpe : model.getObjects())
            attrMap.put(bpe.getRDFId(), extractAttributes(bpe));
        outputMap.put(ATTRIBUTES_FIELD, attrMap);

        httpServletResponse.setContentType("application/json");
		JSONValue.writeJSONString(outputMap, out);
	}

    private List<String> extractGenes(Model model) {
        HashSet<String> genes = new HashSet<String>();

        for (Xref xref : model.getObjects(Xref.class)) {
            if(xref.getDb() != null && xref.getDb().equalsIgnoreCase("HGNC")) {
                String rawId = xref.getId();
                String id = rawId.split(":")[1].trim();
                String symbol = HGNC.getSymbol(id);
                if(symbol != null)
                    genes.add(symbol);
            }
        }

        return new ArrayList<String>(genes);
    }

    private Map<String, List<String>> extractAttributes(BioPAXElement bpe) {
        EditorMap editorMap = SimpleEditorMap.L3;
        Set<org.biopax.paxtools.controller.PropertyEditor> editors = editorMap.getEditorsOf(bpe);

        Map<String, List<String>> attributes = new HashMap<String, List<String>>();
        for (PropertyEditor editor : editors) {
            String key = editor.getProperty();
            Set valueFromBean = editor.getValueFromBean(bpe);
            ArrayList<String> strings = new ArrayList<String>();
            for (Object o : valueFromBean) {
                strings.add(o.toString());
            }
            attributes.put(key, strings);
        }

        return attributes;
    }
*/
}
