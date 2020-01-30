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

package org.mskcc.cbio.portal.util;

import java.io.IOException;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoException;

public class TestGisticReader {

    @Test
    public void testGisticReader() throws DaoException, IOException, Exception {
        //    File metadata = new File(".//testCancerStudy.txt");
        //    File gisticFile = new File("/test-gistic-amp.txt");
        //    File gisticTable_file = new File("/test-gistic-table-amp.txt");
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
