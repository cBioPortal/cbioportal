/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mskcc.cbio.portal.model.DBGeneticAltRow;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.model.DBProfileData;
import org.mskcc.cbio.portal.model.DBProfileDataCaseList;
import org.mskcc.cbio.portal.model.DBSimpleProfileData;
import org.mskcc.cbio.portal.persistence.ProfileDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class ProfileDataService {

    private class Key {

        private int x;
        private int y;

        public Key(int _x, int _y) {
            this.x = _x;
            this.y = _y;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return this.x == key.x && this.y == key.y;
        }
        @Override
        public int hashCode() {
            return 31*this.x + this.y;
        }
    }

    @Autowired
    private ProfileDataMapper profileDataMapper;
    @Autowired
    private GeneticProfileService geneticProfileService;

    @Transactional
    public List<DBProfileData> byInternalId(List<Integer> ids, List<Integer> entrez_gene_ids) {
        List<DBGeneticProfile> profiles = geneticProfileService.byInternalId(ids);
        List<Integer> mutation = new ArrayList<>();
        List<Integer> other = new ArrayList<>();
        for (DBGeneticProfile p : profiles) {
            if (p.genetic_alteration_type.equals("MUTATION_EXTENDED")) {
                mutation.add(p.internal_id);
            } else {
                other.add(p.internal_id);
            }
        }
        List<DBProfileData> ret = new ArrayList<>();
        if (!other.isEmpty()) {
            List<DBGeneticAltRow> geneticAlts = profileDataMapper.altRow(other, entrez_gene_ids);
            List<DBProfileDataCaseList> orderedCaseLists = profileDataMapper.profileCaseList(ids);
            // construct table
            Map<Key, Integer> caseMap = new HashMap<>();
            for (DBProfileDataCaseList dbcl : orderedCaseLists) {
                String[] caselist = dbcl.ordered_sample_list.split(",");
                for (int i = 0; i < caselist.length; i++) {
                    caseMap.put(new Key(dbcl.genetic_profile_id, i), Integer.parseInt(caselist[i], 10));
                }
            }
            for (DBGeneticAltRow row : geneticAlts) {
                String[] values = row.values.split(",");
                for (int i=0; i<values.length; i++) {
                    DBSimpleProfileData newPD = new DBSimpleProfileData();
                    newPD.entrez_gene_id = row.entrez_gene_id;
                    newPD.internal_case_id = caseMap.get(new Key(row.genetic_profile_id,i));
                    newPD.internal_id = row.genetic_profile_id;
                    newPD.profile_data = values[i];
                    ret.add(newPD);
                }
            }
        }
        if (!mutation.isEmpty()) {
            ret.addAll(profileDataMapper.mutByInternalId(mutation, entrez_gene_ids));
        }
        return ret;
    }

    @Transactional
    public List<DBProfileData> byInternalId(List<Integer> ids, List<Integer> entrez_gene_ids, List<Integer> internal_case_ids) {
        List<DBGeneticProfile> profiles = geneticProfileService.byInternalId(ids);
        List<Integer> mutation = new ArrayList<>();
        List<Integer> other = new ArrayList<>();
        for (DBGeneticProfile p : profiles) {
            if (p.genetic_alteration_type.equals("MUTATION_EXTENDED")) {
                mutation.add(p.internal_id);
            } else {
                other.add(p.internal_id);
            }
        }
        List<DBProfileData> ret = new ArrayList<>();
        if (!other.isEmpty()) {
            List<DBGeneticAltRow> geneticAlts = profileDataMapper.altRow(other, entrez_gene_ids);
            List<DBProfileDataCaseList> orderedCaseLists = profileDataMapper.profileCaseList(ids);
            // construct set
            Set<Integer> cases = new HashSet<>();
            cases.addAll(internal_case_ids);
            // construct table
            Map<Key, Integer> caseMap = new HashMap<>();
            for (DBProfileDataCaseList dbcl : orderedCaseLists) {
                String[] caselist = dbcl.ordered_sample_list.split(",");
                for (int i = 0; i < caselist.length; i++) {
                    caseMap.put(new Key(dbcl.genetic_profile_id, i), Integer.parseInt(caselist[i], 10));
                }
            }
            for (DBGeneticAltRow row : geneticAlts) {
                String[] values = row.values.split(",");
                for (int i=0; i<values.length; i++) {
                    int internal_case_id = caseMap.get(new Key(row.genetic_profile_id, i));
                    if(cases.contains(internal_case_id)) {
                        DBSimpleProfileData newPD = new DBSimpleProfileData();
                        newPD.entrez_gene_id = row.entrez_gene_id;
                        newPD.internal_case_id = internal_case_id;
                        newPD.internal_id = row.genetic_profile_id;
                        newPD.profile_data = values[i];
                        ret.add(newPD);
                    }
                }
            }
        }
        if (!mutation.isEmpty()) {
            ret.addAll(profileDataMapper.mutByInternalIdCaseId(mutation, entrez_gene_ids, internal_case_ids));
        }
        return ret;
    }
}
