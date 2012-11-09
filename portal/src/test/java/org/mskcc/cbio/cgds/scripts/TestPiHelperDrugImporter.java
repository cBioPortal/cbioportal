/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.cgds.scripts;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoDrug;
import org.mskcc.cbio.cgds.scripts.drug.DrugDataResource;
import org.mskcc.cbio.cgds.scripts.drug.internal.PiHelperImporter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TestPiHelperDrugImporter extends TestCase {
    public void testImporter() throws Exception {
        ResetDatabase.resetDatabase();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        String today = format.format(cal.getTime());
        DrugDataResource pihelper = new DrugDataResource(
                "PiHelper",
                "https://bitbucket.org/armish/pihelper/downloads/pihelper_data_20121107.zip",
                today
        );

        PiHelperImporter importer = new PiHelperImporter(pihelper);

        ClassLoader classLoader = this.getClass().getClassLoader();
        importer.setDrugInfoFile(classLoader.getResourceAsStream("test_pihelper_drugs.tsv"));
        importer.setDrugTargetsFile(classLoader.getResourceAsStream("test_pihelper_drugtargets.tsv"));

        importer.importData();

        DaoDrug daoDrug = DaoDrug.getInstance();
        assertEquals(6, daoDrug.getCount());
    }
}
