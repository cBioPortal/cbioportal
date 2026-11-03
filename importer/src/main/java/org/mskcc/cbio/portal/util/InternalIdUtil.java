/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.util.*;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;

public class InternalIdUtil
{

    public static List<Sample> getSamplesById(Collection<Integer> sampleIds) {
        List<Sample> samples = new ArrayList<Sample>();
        for (int id : sampleIds) {
            Sample sample = DaoSample.getSampleById(id);
            if (sample!=null) {
                samples.add(sample);
            }
        }
        return samples;
    }
    
    public static List<Patient> getPatientsById(Collection<Integer> patientIds) {
        List<Patient> patients = new ArrayList<Patient>();
        for (int id : patientIds) {
            Patient patient = DaoPatient.getPatientById(id);
            if (patient!=null) {
                patients.add(patient);
            }
        }
        return patients;
    }

}
