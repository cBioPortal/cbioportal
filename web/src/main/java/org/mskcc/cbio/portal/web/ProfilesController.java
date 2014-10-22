/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;

/**
 *
 * @author abeshoua
 */

public class ProfilesController {

    /* META */
    public static ArrayList<GeneticProfileJSON> getProfiles(Integer cancer_study_internal_id) throws DaoException {
        ArrayList<GeneticProfile> profiles = DaoGeneticProfile.getAllGeneticProfiles(cancer_study_internal_id);
        ArrayList<GeneticProfileJSON> ret = new ArrayList<>();
        for (GeneticProfile p: profiles) {
            ret.add(new GeneticProfileJSON(p));
        }
        return ret;
    }
    public static ArrayList<GeneticProfileJSON> getProfiles(String cancer_study_id) throws DaoException {
        ArrayList<GeneticProfile> profiles = DaoGeneticProfile.getAllGeneticProfiles(StudiesController.getStudy(cancer_study_id).get(0).internal_id);
        ArrayList<GeneticProfileJSON> ret = new ArrayList<>();
        for (GeneticProfile p: profiles) {
            ret.add(new GeneticProfileJSON(p));
        }
        return ret;
    }
    public static ArrayList<GeneticProfileJSON> getProfile(String id) throws DaoException {
        ArrayList<GeneticProfileJSON> ret = new ArrayList<>();
        ret.add(new GeneticProfileJSON(DaoGeneticProfile.getGeneticProfileByStableId(id)));
        return ret;
    }
    public static ArrayList<GeneticProfileJSON> getProfile(Integer internalId) throws DaoException {
        ArrayList<GeneticProfileJSON> ret = new ArrayList<>();
        ret.add(new GeneticProfileJSON(DaoGeneticProfile.getGeneticProfileById(internalId)));
        return ret;
    }
    
    /* DATA */
    // TODO: this whole thing!!!!
    
    /*ArrayList<ExtendedMutation> mutationList =
                    DaoMutation.getMutations(geneticProfile.getGeneticProfileId(),
                            canonicalGene.getEntrezGeneId());
            for (ExtendedMutation mutation:  mutationList) {
        return null;*/
    
    public static ArrayList<ProfileDataJSON> getProfileData(String id, List<Integer> internal_case_ids, List<Long> genes) throws DaoException {
        ArrayList<ProfileDataJSON> ret = new ArrayList<>();
        GeneticProfileJSON profile = ProfilesController.getProfile(id).get(0);
        if (profile.genetic_alteration_type == GeneticAlterationType.MUTATION_EXTENDED) {
            for (Long gene: genes) {
                ArrayList<ExtendedMutation> mutationList = DaoMutation.getMutations(profile.internal_id, gene);
                for (ExtendedMutation em: mutationList) {
                    ret.add(new MutationDataJSON(em));
                }
            }
        } else if (profile.genetic_alteration_type == GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) {
            for (Long gene: genes) {
                
            }
        } else {
            for (Long gene: genes) {
                Map<Integer, String> sampleMap = DaoGeneticAlteration.getInstance().getGeneticAlterationMap(profile.internal_id, gene);
                ProfileDataJSON toAdd = new ProfileDataJSON();
                toAdd.entrez_id = gene;
                toAdd.gene = "yo"; //TODO
                toAdd.genetic_alteration_type = profile.genetic_alteration_type;
            }
        }
        
            
        
        
        return ret;
    }
    public static ArrayList<ProfileDataJSON> getProfileData(int internal_id, List<Integer> internal_case_ids, List<Long> genes) throws DaoException {
        ArrayList<ProfileDataJSON> ret = new ArrayList<>();
        return ret;
    }
    /* Delegating methods */
    public static ArrayList<ProfileDataJSON> getProfileData(String id, int internal_case_list_id, List<Long> genes) throws DaoException {
        return getProfileData(id, CaseListsController.getCaseList(internal_case_list_id).get(0).getInternalCaseIds(), genes);
    }
    public static ArrayList<ProfileDataJSON> getProfileData(int internal_id, int internal_case_list_id, List<Long> genes) throws DaoException {
        return getProfileData(internal_id, CaseListsController.getCaseList(internal_case_list_id).get(0).getInternalCaseIds(), genes);
    }
    public static ArrayList<ProfileDataJSON> getProfileDataByIds(List<String> ids, int internal_case_list_id, List<Long> genes) throws DaoException {
        ArrayList<ProfileDataJSON> ret = new ArrayList<>();
        for(String id: ids) {
            ret.addAll(ProfilesController.getProfileData(id, internal_case_list_id, genes));
        }
        return ret;
    }
    public static ArrayList<ProfileDataJSON> getProfileDataByInternalIds(List<Integer> internal_ids, int internal_case_list_id, List<Long> genes) throws DaoException{
        ArrayList<ProfileDataJSON> ret = new ArrayList<>();
        for(int id: internal_ids) {
            ret.addAll(ProfilesController.getProfileData(id, internal_case_list_id, genes));
        }
        return ret;
    }
    public static ArrayList<ProfileDataJSON> getProfileDataByIds(List<String> ids, List<Integer> internal_case_ids, List<Long> genes) throws DaoException {
        ArrayList<ProfileDataJSON> ret = new ArrayList<>();
        for(String id: ids) {
            ret.addAll(ProfilesController.getProfileData(id, internal_case_ids, genes));
        }
        return ret;
    }
    public static ArrayList<ProfileDataJSON> getProfileDataByInternalIds(List<Integer> internal_ids, List<Integer> internal_case_ids, List<Integer> genes) throws DaoException {
        ArrayList<ProfileDataJSON> ret = new ArrayList<>();
        for(int id: internal_ids) {
            //ret.addAll(ProfilesController.getProfileData(id, internal_case_ids, genes));
        }
        return ret;
    }
}
    