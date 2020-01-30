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

public class InternalIdUtil {

    public static List<String> getStableSampleIds(
        List<Integer> internalSampleIds
    ) {
        List<String> sampleIds = new ArrayList<String>();
        for (Integer internalSampleId : internalSampleIds) {
            Sample s = DaoSample.getSampleById(internalSampleId);
            sampleIds.add(s.getStableId());
        }
        return sampleIds;
    }

    public static List<Integer> getInternalPatientIds(int cancerStudyId) {
        List<Integer> patientIds = new ArrayList<Integer>();
        for (Patient patient : DaoPatient.getPatientsByCancerStudyId(
            cancerStudyId
        )) {
            patientIds.add(patient.getInternalId());
        }
        return patientIds;
    }

    public static List<Integer> getInternalNonNormalSampleIds(
        int cancerStudyId
    ) {
        List<Integer> sampleIds = new ArrayList<Integer>();
        List<Sample> samples = DaoSample.getSamplesByCancerStudy(cancerStudyId);
        for (Sample sample : samples) {
            sampleIds.add(sample.getInternalId());
        }
        return sampleIds;
    }

    public static List<Integer> getInternalNonNormalSampleIds(
        int cancerStudyId,
        List<String> stableSampleIds
    ) {
        List<Integer> sampleIds = new ArrayList<Integer>();
        for (String sampleId : stableSampleIds) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(
                cancerStudyId,
                sampleId
            );
            if (Sample.Type.normalTypes().contains(sample.getType())) {
                continue;
            }
            sampleIds.add(sample.getInternalId());
        }
        return sampleIds;
    }

    public static List<Integer> getInternalSampleIds(
        int cancerStudyId,
        List<String> stableSampleIds
    ) {
        ArrayList<Integer> sampleIds = new ArrayList<Integer>();
        for (String sampleId : stableSampleIds) {
            if (
                DaoSample.getSampleByCancerStudyAndSampleId(
                    cancerStudyId,
                    sampleId
                ) !=
                null
            ) {
                Sample s = DaoSample.getSampleByCancerStudyAndSampleId(
                    cancerStudyId,
                    sampleId
                );
                sampleIds.add(s.getInternalId());
            }
        }
        return sampleIds;
    }

    public static List<Integer> getInternalNonNormalSampleIdsFromPatientIds(
        int cancerStudyId,
        List<String> stablePatientIds
    ) {
        return getInternalSampleIdsFromPatientIds(
            cancerStudyId,
            stablePatientIds,
            Sample.Type.normalTypes()
        );
    }

    public static List<Integer> getInternalSampleIdsFromPatientIds(
        int cancerStudyId,
        List<String> stablePatientIds,
        Set<Sample.Type> excludes
    ) {
        List<Integer> sampleIds = new ArrayList<Integer>();
        for (String patientId : stablePatientIds) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                cancerStudyId,
                patientId
            );
            for (Sample sample : DaoSample.getSamplesByPatientId(
                patient.getInternalId()
            )) {
                if (excludes.contains(sample.getType())) {
                    continue;
                }
                sampleIds.add(sample.getInternalId());
            }
        }
        return sampleIds;
    }

    public static List<Sample> getSamplesById(Collection<Integer> sampleIds) {
        List<Sample> samples = new ArrayList<Sample>();
        for (int id : sampleIds) {
            Sample sample = DaoSample.getSampleById(id);
            if (sample != null) {
                samples.add(sample);
            }
        }
        return samples;
    }

    public static List<Patient> getPatientsById(
        Collection<Integer> patientIds
    ) {
        List<Patient> patients = new ArrayList<Patient>();
        for (int id : patientIds) {
            Patient patient = DaoPatient.getPatientById(id);
            if (patient != null) {
                patients.add(patient);
            }
        }
        return patients;
    }

    public static List<String> getStablePatientIdsFromSampleIds(
        int cancerStudyId,
        List<String> stableSampleIds
    ) {
        Set<String> patientIds = new HashSet<String>();
        for (String sampleId : stableSampleIds) {
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(
                cancerStudyId,
                sampleId
            );
            Patient p = DaoPatient.getPatientById(s.getInternalPatientId());
            patientIds.add(p.getStableId());
        }
        return new ArrayList<String>(patientIds);
    }
}
