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

package org.mskcc.cbio.portal.util;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGistic;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.util.GisticReader;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class TestGisticReader extends TestCase {

    public void testGisticReader() throws DaoException, IOException, Exception {
//    File metadata = new File(".//testCancerStudy.txt");
//    File gisticFile = new File("/test-gistic-amp.txt");
//    File gisticTable_file = new File("/test-gistic-table-amp.txt");
//    ProgressMonitor pm = new ProgressMonitor();
//    Gistic dummyGistic = new Gistic();
//
//    GisticReader reader = new GisticReader();
//
//    ArrayList<Gistic> table_reads = reader.parse_Table(gisticTable_file);
//    ArrayList<Gistic> nontable_reads = reader.parse_NonTabular(gisticFile);
//    assertTrue(reader.parseAmpDel(gisticTable_file) == Gistic.AMPLIFIED);
//    assertTrue(reader.parseAmpDel(gisticFile) == Gistic.AMPLIFIED);
//
//    // mergeGistic
//    CanonicalGene madeup_gene1 = new CanonicalGene(1l, "DIO2");
//    CanonicalGene madeup_gene2 = new CanonicalGene(2l, "NIKI");
//
//    ArrayList<CanonicalGene> testgenes;
//    testgenes = new ArrayList<CanonicalGene>();
//    testgenes.add(madeup_gene1);
//    testgenes.add(madeup_gene2);
//
//    Gistic testgistic1 = new Gistic(1, -1, 1, 2, 0.05d, 0.05d, testgenes, Gistic.AMPLIFIED);
//    Gistic testgistic2 = new Gistic(1, 1, -1, 2, 0.05d, 0.05d, testgenes, Gistic.AMPLIFIED);
//
//    testgistic1 = reader.mergeGistics(testgistic1, testgistic2);
//    assertTrue(testgistic1.getChromosome() == 1);
//
//    // database test
//    DaoGistic.addGistic(testgistic1);
//    DaoGistic.getGisticByROI(testgistic1.getChromosome(), testgistic1.getPeakStart(), testgistic2.getPeakEnd());
//    DaoGistic.deleteGistic(testgistic1.getInternalId());
//
//
//    ArrayList<Gistic> merged = reader.mergeGisticLists(table_reads, nontable_reads, Gistic.AMPLIFIED);
    }
}
