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

        Gistic gistic = new Gistic(1, 7, "1q11.1", 10000, 10001, 0.05f, genes, Gistic.AMPLIFIED);

        Map map = GisticJSON.Gistic_toMap(gistic);
        
        assertTrue(map.get("chromosome").equals(7));
        assertTrue(map.get("peakStart").equals(10000));
        assertTrue(map.get("peakEnd").equals(10001));
//        System.out.println(map.get("genes_in_ROI"));
        assertTrue(map.get("genes_in_ROI").equals(genes));
        assertTrue(map.get("qval").equals(0.05d));
        assertTrue(map.get("ampdel").equals(Gistic.AMPLIFIED));
    }

}
