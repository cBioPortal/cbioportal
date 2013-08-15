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
package org.mskcc.cbio.importer.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DataMatrixTest {

    @Test
    public void fromStringTest() throws Exception {
        String in = "H1\tH2\tH3\n" +
                "A\tB\tC\n" +
                "X\tY\tZ\n";

        DataMatrix dataMatrix = DataMatrix.fromString(in, "\n", "\t");

        assertEquals("A", dataMatrix.getColumnData(0).get(0));
        assertEquals("X", dataMatrix.getColumnData(0).get(1));

        assertEquals("A", dataMatrix.getColumnData("H1").get(0).get(0));
        assertEquals("X", dataMatrix.getColumnData("H1").get(0).get(1));

        assertEquals(2, dataMatrix.getNumberOfRows());
        assertEquals(3, dataMatrix.getColumnHeaders().size());
    }

    @Test
    public void asTableTest() throws Exception {
        String in = "H1\tH2\tH3\n" +
                "A\tB\tC\n" +
                "X\tY\tZ\n";
        DataMatrix dataMatrix = DataMatrix.fromString(in, "\n", "\t");

        Table table = dataMatrix.toTable();

        System.out.println(
                table.column("H1")
        );

    }

}
