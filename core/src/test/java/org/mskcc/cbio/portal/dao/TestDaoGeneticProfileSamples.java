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

package org.mskcc.cbio.portal.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

import java.util.ArrayList;

/**
 * JUnit Tests for the Dao Genetic Profile Cases Class.
 *
 * @author Ethan Cerami.
 */
public class TestDaoGeneticProfileSamples extends TestCase {

    /**
     * Tests the Dao Genetic Profile Samples Class.
     * @throws DaoException Database Exception.
     */
    public void testDaoGeneticProfileSamples() throws DaoException {
        ResetDatabase.resetDatabase();
        createSamples();

        ArrayList<Integer> orderedSampleList = new ArrayList<Integer>();
        orderedSampleList.add(1);
        orderedSampleList.add(2);
        orderedSampleList.add(3);
        orderedSampleList.add(4);
        int numRows = DaoGeneticProfileSamples.addGeneticProfileSamples(1, orderedSampleList);

        assertEquals (1, numRows);

        orderedSampleList = DaoGeneticProfileSamples.getOrderedSampleList(1);
        assertEquals (4, orderedSampleList.size());

        //  Test the Delete method
        DaoGeneticProfileSamples.deleteAllSamplesInGeneticProfile(1);
        orderedSampleList = DaoGeneticProfileSamples.getOrderedSampleList(1);
        assertEquals (0, orderedSampleList.size());
    }

    private void createSamples() throws DaoException {
        CancerStudy study = new CancerStudy("study", "description", "id", "brca", true);
        Patient p = new Patient(study, "TCGA-1");
        int pId = DaoPatient.addPatient(p);
        Sample s = new Sample("TCGA-1", pId, "type");
        DaoSample.addSample(s);
        s = new Sample("TCGA-2", pId, "type");
        DaoSample.addSample(s);
        s = new Sample("TCGA-3", pId, "type");
        DaoSample.addSample(s);
        s = new Sample("TCGA-4", pId, "type");
        DaoSample.addSample(s);
    }

}