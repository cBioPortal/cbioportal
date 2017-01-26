/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.model;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * JUnit Tests for the Categorized GeneticProfileSet.
 *
 * @author Ethan Cerami.
 */
public class TestCategorizedGeneticProfileSet {

    @Test
    public void testEmptyList() {
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        CategorizedGeneticProfileSet categorizedSet = new CategorizedGeneticProfileSet(profileList);
        assertEquals (0, categorizedSet.getNumDefaultMutationAndCopyNumberProfiles());
        assertEquals (null, categorizedSet.getDefaultCnaProfile());
        assertEquals (null, categorizedSet.getDefaultMutationProfile());
    }

    @Test
    public void testMRNADataOnly() {
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        addMRNAProfile(profileList);
        CategorizedGeneticProfileSet categorizedSet = new CategorizedGeneticProfileSet(profileList);
        assertEquals (0, categorizedSet.getNumDefaultMutationAndCopyNumberProfiles());
        assertEquals (null, categorizedSet.getDefaultCnaProfile());
        assertEquals (null, categorizedSet.getDefaultMutationProfile());
    }

    @Test
    public void testGisticProfile() {
        ArrayList<GeneticProfile> profileList = buildProfileList1();
        CategorizedGeneticProfileSet categorizedSet = new CategorizedGeneticProfileSet(profileList);
        assertEquals (2, categorizedSet.getNumDefaultMutationAndCopyNumberProfiles());
        GeneticProfile defaultCnaProfile = categorizedSet.getDefaultCnaProfile();
        assertEquals ("gbm_gistic", defaultCnaProfile.getStableId());
        GeneticProfile defaultMutProfile = categorizedSet.getDefaultMutationProfile();
        assertEquals ("gbm_mut", defaultMutProfile.getStableId());
    }

    @Test
    public void testRaeProfile() {
        ArrayList<GeneticProfile> profileList = buildProfileList2();
        CategorizedGeneticProfileSet categorizedSet = new CategorizedGeneticProfileSet(profileList);
        assertEquals (2, categorizedSet.getNumDefaultMutationAndCopyNumberProfiles());
        GeneticProfile defaultCnaProfile = categorizedSet.getDefaultCnaProfile();
        assertEquals ("gbm_rae", defaultCnaProfile.getStableId());
        GeneticProfile defaultMutProfile = categorizedSet.getDefaultMutationProfile();
        assertEquals ("gbm_mut", defaultMutProfile.getStableId());
    }

    private ArrayList<GeneticProfile> buildProfileList1() {
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        addCnaPathwayProfile(profileList);
        addGisticProfile(profileList);
        addMutationProfile(profileList);
        return profileList;
    }

    private ArrayList<GeneticProfile> buildProfileList2() {
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        addCnaPathwayProfile(profileList);
        addRaeProfile(profileList);
        addMutationProfile(profileList);
        return profileList;
    }

    private void addMRNAProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile geneticProfile = new GeneticProfile("gbm_mrna", 1,
                                                           GeneticAlterationType.MRNA_EXPRESSION,
                                                           "CONTINUOUS", "GBM mRNA", "GBM Affymetrix mRNA", true);
        profileList.add(geneticProfile);
    }

    private void addRaeProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile gisticProfile = new GeneticProfile("gbm_rae", 1,
                                                          GeneticAlterationType.COPY_NUMBER_ALTERATION,
                                                          "DISCRETE", "GBM RAE", "GBM RAE Results", true);
        profileList.add(gisticProfile);
    }

    private void addMutationProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile mutationProfile = new GeneticProfile("gbm_mut", 1,
                                                            GeneticAlterationType.MUTATION_EXTENDED,
                                                            "MAF", "GBM Mutations", "GBM Whole Exome Mutations", true);
        profileList.add(mutationProfile);
    }

    private void addGisticProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile gisticProfile = new GeneticProfile("gbm_gistic", 1,
                                                          GeneticAlterationType.COPY_NUMBER_ALTERATION,
                                                          "DISCRETE", "GBM GISTIC", "GBM GISTIC Results", true);
        profileList.add(gisticProfile);
    }

    private void addCnaPathwayProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile otherCNAProfile = new GeneticProfile("cna_pathways", 1,
                                                            GeneticAlterationType.COPY_NUMBER_ALTERATION,
                                                            "DISCRETE", "CNA Pathways", "CNA Pathway Results", true);
        profileList.add(otherCNAProfile);
    }
}
