package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/24/14.
 */
public class PatientClinicalReportGenerator {
    /*
    responsible for generating a tsv file containing all persisted clinical findings
    for a specified patient within the Darwin data repository
     */
    private final Integer patientId;
    private static final Logger logger = Logger.getLogger(PatientClinicalReportGenerator.class);

    public PatientClinicalReportGenerator(Integer anId){
        Preconditions.checkArgument(null != anId && anId > 0 ,
                "A patient id is required");
        this.patientId = anId;
    }

    

}
