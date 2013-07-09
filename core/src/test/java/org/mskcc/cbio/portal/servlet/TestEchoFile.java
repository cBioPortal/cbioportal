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
package org.mskcc.cbio.portal.servlet;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class TestEchoFile extends TestCase {

    public void testProcessStagingCsv() {

        String testString = "Hugo_Symbol\tEntrez_Gene_Id\tTCGA-BL-A0C8\tTCGA-BL-A13I\tTCGA-BL-A13J\n" +
                "ACAP3\t116983\t-1\t0\t0\n";

        Reader testReader = new StringReader(testString);

        List<ImmutableMap<String, String>> data = null;
        try {
            data = EchoFile.processStagingCsv(new CSVReader(testReader, '\t'));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        assertEquals(
                data.get(0),
                ImmutableMap.of("sample_id", "TCGA-BL-A0CB", "hugo", "ACAP3", "value", "-1")
        );

        assertTrue(data.size() == 3);
    }

    public void testGene2cna() {

        // reformat!!
        String cna = "Hugo_Symbol\tEntrez_Gene_Id\tTCGA-BL-A0C8\tTCGA-BL-A13I\tTCGA-BL-A13J\n" +
                "ACAP3\t116983\t-1\t0\t0\n";

        EchoFile testEchoFile = new EchoFile();
        testEchoFile.processCnaString(cna);
    }
}
