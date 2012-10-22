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

package org.mskcc.cbio.cgds.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.SangerCancerGene;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;

/**
 * JUnit tests for DaoSangerCensus class.
 */
public class TestDaoSangerCensus extends TestCase {

    public void testDaoMutation() throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene brca1 = new CanonicalGene(675, "BRCA1");
        daoGene.addGene(brca1);

        DaoSangerCensus daoCensus = DaoSangerCensus.getInstance();
        daoCensus.deleteAllRecords();
        daoCensus.addGene(brca1, true, true, "CML, ALL, T-ALL", "AML, leukemia, breast",
                "Familial neuroblastoma", "L, E, M", "T, Mis, A", "BCR, ETV6, NUP214", true,
                "Cardio-facio-cutaneous syndrome");

        HashMap<String, SangerCancerGene> cancerCensus = daoCensus.getCancerGeneSet();
        SangerCancerGene cancerGene = cancerCensus.get("BRCA1");
        assertEquals (true, cancerGene.isCancerGermlineMutation());
        assertEquals (true, cancerGene.isCancerSomaticMutation());

        assertEquals ("CML, ALL, T-ALL", cancerGene.getTumorTypesSomaticMutation());
        assertEquals (3, cancerGene.getTumorTypesSomaticMutationList().size());
        assertEquals ("chronic myeloid leukemia", cancerGene.getTumorTypesSomaticMutationList().get(0));
        assertEquals ("AML, leukemia, breast", cancerGene.getTumorTypesGermlineMutation());
        assertEquals (3, cancerGene.getTumorTypesGermlineMutationList().size());
        assertEquals ("acute myelogenous leukemia", cancerGene.getTumorTypesGermlineMutationList().get(0));
        assertEquals ("Familial neuroblastoma", cancerGene.getCancerSyndrome());
        assertEquals ("L, E, M", cancerGene.getTissueType());
        assertEquals (3, cancerGene.getTissueTypeList().size());
        assertEquals ("leukaemia/lymphoma", cancerGene.getTissueTypeList().get(0));
        assertEquals ("T, Mis, A", cancerGene.getMutationType());
        assertEquals (3, cancerGene.getMutationTypeList().size());
        assertEquals ("translocation", cancerGene.getMutationTypeList().get(0));
        assertEquals ("BCR, ETV6, NUP214", cancerGene.getTranslocationPartner());
        assertEquals (true, cancerGene.getOtherGermlineMut());
        assertEquals ("Cardio-facio-cutaneous syndrome", cancerGene.getOtherDisease());
    }
}