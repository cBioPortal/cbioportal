/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.GeneticProfile;

/**
 *
 * @author abeshoua
 */

public class ProfilesController {

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
}