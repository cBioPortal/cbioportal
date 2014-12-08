package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.collect.Lists;

import java.util.List;

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
 * Created by criscuof on 11/30/14.
 */
public interface DarwinParserNames {

    public static final String CN_STATUS = "STATUS:";
    public static final String CN_REASON_FOR_VISIT = "REASON FOR VISIT:";
    public static final String CN_HISTORY = "HISTORY:";
    public static final String CN_INTERVAL_HISTORY = "INTERVAL HISTORY:";
    public static final String CN_MEDICATIONS = "MEDICATIONS:";
    public static final String CN_DIAGNOSIS = "DIAGNOSIS:";
    public static final String CN_CONSTIT = "CONSTIT.:";
    public static final String CN_REVIEW_OF_SYSTEMS = "REVIEW OF SYSTEMS:";
    public static final String CN_PHYSICAL_EXAMINATION = "PHYSICAL_EXAMINATION:";
    public static final String CN_PHYSICAL_EXAMINATION_GENERAL = "GENERAL:";
    public static final String CN_PHYSICAL_EXAMINATION_VITAL_SIGNS = "VITAL SIGNS:";
    public static final String CN_PHYSICAL_EXAMINATION_HEAD_NECK = "HEAD/NECK:";
    public static final String CN_PHYSICAL_EXAMINATION_NODES = "NODES:";
    public static final String CN_PHYSICAL_EXAMINATION_HEART = "HEART:";
    public static final String CN_PHYSICAL_EXAMINATION_LUNGS = "LUNGS:";
    public static final String CN_PHYSICAL_EXAMINATION_ABDOMEN = "ABDOMEN:";
    public static final String CN_PHYSICAL_EXAMINATION_SKIN = "SKIN:";
    public static final String CN_PHYSICAL_EXAMINATION_EXTREMITIES = "EXTREMITIES:";
    public static final String CN_PHYSICAL_EXAMINATION_NEURO = "NEURO:";
    public static final String CN_DATA_REVIEW = "DATA REVIEW:";
    public static final String CN_HEME = "HEME:";
    public static final String CN_IMPRESSION = "IMPRESSION";
    public static final String CN_PLAN = "PLAN:";
    public static final String CN_IMPRESSION_PLAN = "IMPRESSION/PLAN:";
    public static final String CN_SOCIAL = "SOCIAL:";
    public static final String CN_FAMILY_SOCIAL_HISTORY = "FAMILY, SOCIAL HISTORY:";
    public static final String CN_ALLERGIES = "ALLERGIES:";
    public static final String CN_CHIEF_COMPLAINT = "CHIEF COMPLAINT:";
    public static final String CN_PAST_SURGICAL_HISTORY = "PAST SURGICAL HISTORY:";
    public static final String CN_SUMMARY = "SUMMARY:";
    public static final String CN_PAGE = "PAGE:";
    public static final String CN_NAME = "NAME:";
    public static final String CN_DICT = "DICT:";
    public static final String CN_DATE = "DATE:";
    public static final String CN_DOB = "DOB:";
    public static final String CN_ATTENDING = "ATTENDING:";
    //Pathology Report Attributes
    public static final String PATH_CLIN_DIAG_HIST = "Clinical Diagnosis & History:";
    public static final String PATH_DIAGNOSIS = "DIAGNOSIS:";
    public static final String PATH_SPEC_SUB = "Specimens Submitted:";
    public static final String PATH_DATE_REPORT ="Date of Report:";
    public static final String PATH_DIAG_INTERP = "DIAGNOSTIC INTERPRETATION:";
    public static final String PATH_CLIN_PANEL = "CLINICAL PANEL";
    public static final String PATH_INVES_PANEL = "INVESTIGATIONAL PANEL";
    public static final String PATH_METHODOLOGY = "Methodology:";

    public static final List<String> PATH_ATTRIBUTE_LIST = Lists.newArrayList(
            PATH_CLIN_DIAG_HIST, PATH_DATE_REPORT, PATH_DIAGNOSIS, PATH_SPEC_SUB,
            PATH_DIAG_INTERP,PATH_CLIN_PANEL,PATH_METHODOLOGY, PATH_INVES_PANEL

    );

    public static final String PATH_STOP_SIGNAL = "I ATTEST";

    public static final List<String> PATH_FILTER_LIST = Lists.newArrayList(
            "PathDoc", "MRN", "Account", "Physician", "Accession", "Date of Collection",
            "Date of Receipt","PLATE#",
            "*** Report","==","[Report","..."
    );

    public static final List<String> CN_FILTER_LIST = Lists.newArrayList(
            CN_PAGE, CN_NAME, CN_DICT, CN_ATTENDING,"**INSTITUTION","OUTPATIENT PROGRESS", CN_PHYSICAL_EXAMINATION,
            "INTERNAL", "Report Electronically Signed","**NAME","TRANS","*JOB-NUM","OUTPATIENT","FOLLOWUP VISIT",
                    "B/","INITIAL CONSULTATION"

    );

    /*
    List of Review of Systems keywords
     */
    public static final List<String> ROS_KEYWORD_LIST = Lists.newArrayList(
            "CONSTIT.:", "EYES:","ENT:","RESP:","CV:","GI:","GU:",
            "EXTREMITIES:","HEME:","NEURO:","PSYCH:","SUMMARY:",
            "SKIN:","MS:"
    );

    public static final List<String> CN_ATTIBUTE_LIST = Lists.newArrayList(
            CN_STATUS, CN_REASON_FOR_VISIT, CN_HISTORY, CN_INTERVAL_HISTORY,
            CN_MEDICATIONS, CN_DIAGNOSIS, CN_REVIEW_OF_SYSTEMS, CN_PHYSICAL_EXAMINATION_GENERAL,
            CN_PHYSICAL_EXAMINATION_VITAL_SIGNS, CN_PHYSICAL_EXAMINATION_HEAD_NECK, CN_PHYSICAL_EXAMINATION_NODES,
            CN_PHYSICAL_EXAMINATION_HEART, CN_PHYSICAL_EXAMINATION_LUNGS, CN_PHYSICAL_EXAMINATION_ABDOMEN,
            CN_PHYSICAL_EXAMINATION_SKIN, CN_PHYSICAL_EXAMINATION_EXTREMITIES, CN_PHYSICAL_EXAMINATION_NEURO,
            CN_DATA_REVIEW, CN_IMPRESSION, CN_PLAN, CN_FAMILY_SOCIAL_HISTORY,CN_CHIEF_COMPLAINT,CN_PAST_SURGICAL_HISTORY,
            CN_ALLERGIES,CN_SOCIAL,CN_REVIEW_OF_SYSTEMS, CN_CONSTIT, CN_HEME, CN_SUMMARY,CN_DATE,CN_DOB,
            CN_IMPRESSION_PLAN
    );



}
