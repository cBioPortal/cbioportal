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

package org.mskcc.cbio.cgds.model;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.model.CategorizedGeneticProfileSet;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;

import java.util.ArrayList;

/**
 * JUnit Tests for the Categorized GeneticProfileSet.
 *
 * @author Ethan Cerami.
 */
public class TestCategorizedGeneticProfileSet extends TestCase {

    public void testEmptyList() {
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        CategorizedGeneticProfileSet categorizedSet = new CategorizedGeneticProfileSet(profileList);
        assertEquals (0, categorizedSet.getNumDefaultMutationAndCopyNumberProfiles());
        assertEquals (null, categorizedSet.getDefaultCnaProfile());
        assertEquals (null, categorizedSet.getDefaultMutationProfile());
    }

    public void testMRNADataOnly() {
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        addMRNAProfile(profileList);
        CategorizedGeneticProfileSet categorizedSet = new CategorizedGeneticProfileSet(profileList);
        assertEquals (0, categorizedSet.getNumDefaultMutationAndCopyNumberProfiles());
        assertEquals (null, categorizedSet.getDefaultCnaProfile());
        assertEquals (null, categorizedSet.getDefaultMutationProfile());
    }

    public void testGisticProfile() {
        ArrayList<GeneticProfile> profileList = buildProfileList1();
        CategorizedGeneticProfileSet categorizedSet = new CategorizedGeneticProfileSet(profileList);
        assertEquals (2, categorizedSet.getNumDefaultMutationAndCopyNumberProfiles());
        GeneticProfile defaultCnaProfile = categorizedSet.getDefaultCnaProfile();
        assertEquals ("gbm_gistic", defaultCnaProfile.getStableId());
        GeneticProfile defaultMutProfile = categorizedSet.getDefaultMutationProfile();
        assertEquals ("gbm_mut", defaultMutProfile.getStableId());
    }

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
                GeneticAlterationType.MRNA_EXPRESSION, "GBM mRNA", "GBM Affymetrix mRNA", true);
        profileList.add(geneticProfile);
    }
    
    private void addRaeProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile gisticProfile = new GeneticProfile("gbm_rae", 1,
                GeneticAlterationType.COPY_NUMBER_ALTERATION, "GBM RAE",
                "GBM RAE Results", true);
        profileList.add(gisticProfile);
    }

    private void addMutationProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile mutationProfile = new GeneticProfile("gbm_mut", 1,
                GeneticAlterationType.MUTATION_EXTENDED, "GBM Mutations",
                "GBM Whole Exome Mutations", true);
        profileList.add(mutationProfile);
    }

    private void addGisticProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile gisticProfile = new GeneticProfile("gbm_gistic", 1,
                GeneticAlterationType.COPY_NUMBER_ALTERATION, "GBM GISTIC",
                "GBM GISTIC Results", true);
        profileList.add(gisticProfile);
    }

    private void addCnaPathwayProfile(ArrayList<GeneticProfile> profileList) {
        GeneticProfile otherCNAProfile = new GeneticProfile("cna_pathways", 1,
                GeneticAlterationType.COPY_NUMBER_ALTERATION, "CNA Pathways",
                "CNA Pathway Results", true);
        profileList.add(otherCNAProfile);
    }
}
