package org.mskcc.cbio.portal.servlet;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoGistic;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Gistic;
import org.mskcc.cbio.portal.servlet.GisticJSON;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class TestGisticJSON extends TestCase {

    public void testGisticJSON() {
        
        // test JSON map
        ArrayList<CanonicalGene> genes = new ArrayList<CanonicalGene>();

        CanonicalGene niki = new CanonicalGene("NIKI");
        CanonicalGene jj = new CanonicalGene("JJ");
        CanonicalGene tp53 = new CanonicalGene("TP53");

        genes.add(niki);
        genes.add(jj);
        genes.add(tp53);

        Gistic gistic = new Gistic(1, 7, "1q11.1", 10000, 10001, 0.05f, 0.05f, genes, Gistic.AMPLIFIED);

        Map map = GisticJSON.Gistic_toMap(gistic);
        
        assertTrue(map.get("chromosome").equals(7));
        assertTrue(map.get("peakStart").equals(10000));
        assertTrue(map.get("peakEnd").equals(10001));
//        System.out.println(map.get("genes_in_ROI"));
        assertTrue(map.get("genes_in_ROI").equals(genes));
        assertTrue(map.get("qval").equals(0.05d));
        assertTrue(map.get("res_qval").equals(0.05d));
        assertTrue(map.get("ampdel").equals(Gistic.AMPLIFIED));

    }

}
