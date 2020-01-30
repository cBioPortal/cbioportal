/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.util;

import java.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

public class CoExpUtil {

    public static ArrayList<String> getSampleIds(
        String sampleSetId,
        String sampleIdsKeys
    ) {
        try {
            DaoSampleList daoSampleList = new DaoSampleList();
            SampleList sampleList;
            ArrayList<String> sampleIdList = new ArrayList<String>();
            if (sampleSetId.equals("-1")) {
                String strSampleIds = SampleSetUtil.getSampleIds(sampleIdsKeys);
                String[] sampleArray = strSampleIds.split("\\s+");
                for (String item : sampleArray) {
                    sampleIdList.add(item);
                }
            } else {
                sampleList = daoSampleList.getSampleListByStableId(sampleSetId);
                sampleIdList = sampleList.getSampleList();
            }
            return sampleIdList;
        } catch (DaoException e) {
            System.out.println("Caught Dao Exception: " + e.getMessage());
            return null;
        }
    }

    public static GeneticProfile getPreferedGeneticProfile(
        String cancerStudyIdentifier
    ) {
        try {
            CancerStudy cs = DaoCancerStudy.getCancerStudyByStableId(
                cancerStudyIdentifier
            );
            ArrayList<GeneticProfile> gps = DaoGeneticProfile.getAllGeneticProfiles(
                cs.getInternalId()
            );
            GeneticProfile final_gp = null;
            for (GeneticProfile gp : gps) {
                // TODO: support miRNA later
                if (
                    gp.getGeneticAlterationType() ==
                    GeneticAlterationType.MRNA_EXPRESSION
                ) {
                    //rna seq profile (no z-scores applied) holds the highest priority)
                    if (
                        gp.getStableId().toLowerCase().contains("rna_seq") &&
                        !gp.getStableId().toLowerCase().contains("zscores")
                    ) {
                        final_gp = gp;
                        break;
                    } else if (
                        !gp.getStableId().toLowerCase().contains("zscores")
                    ) {
                        final_gp = gp;
                    }
                }
            }
            return final_gp;
        } catch (DaoException e) {
            return null;
        }
    }

    public static Map<Long, double[]> getExpressionMap(
        int profileId,
        String sampleSetId,
        String sampleIdsKeys
    )
        throws DaoException {
        GeneticProfile gp = DaoGeneticProfile.getGeneticProfileById(profileId);
        List<String> stableSampleIds = getSampleIds(sampleSetId, sampleIdsKeys);
        List<Integer> sampleIds = new ArrayList<Integer>();
        for (String sampleId : stableSampleIds) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(
                gp.getCancerStudyId(),
                sampleId
            );
            sampleIds.add(sample.getInternalId());
        }
        sampleIds.retainAll(
            DaoSampleProfile.getAllSampleIdsInProfile(profileId)
        );

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        Map<Long, HashMap<Integer, String>> mapStr = daoGeneticAlteration.getGeneticAlterationMap(
            profileId,
            null
        );
        Map<Long, double[]> map = new HashMap<Long, double[]>(mapStr.size());
        for (Map.Entry<Long, HashMap<Integer, String>> entry : mapStr.entrySet()) {
            Long gene = entry.getKey();
            Map<Integer, String> mapCaseValueStr = entry.getValue();
            double[] values = new double[sampleIds.size()];
            for (int i = 0; i < sampleIds.size(); i++) {
                Integer caseId = sampleIds.get(i);
                String value = mapCaseValueStr.get(caseId);
                Double d;
                try {
                    d = Double.valueOf(value);
                } catch (Exception e) {
                    d = Double.NaN;
                }
                values[i] = d;
            }

            map.put(gene, values);
        }

        return map;
    }
}
