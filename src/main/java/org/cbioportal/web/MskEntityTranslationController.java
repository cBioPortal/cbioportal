/*
 * Copyright (c) 2017-2021 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.web;

import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class checks for the existence and location of entities {samples, patients}
 * based on MSK entity stable id patterns, such as DMP (Department of Molecular Pathology)
 * sample identifiers.
 */
@Controller
@PropertySources({
    @PropertySource(value="classpath:application.properties", ignoreResourceNotFound=true),
    @PropertySource(value="file:///${PORTAL_HOME}/application.properties", ignoreResourceNotFound=true)
})
@ConditionalOnProperty(name = "msk_entity_translation_enabled", havingValue = "true")
public class MskEntityTranslationController {

    @Autowired
    private SampleService sampleService;

    @Autowired
    private PatientService patientService;

    private String sampleViewURL;
    @Value("${sample_view.url}")
    public void setSampleViewURL(String property) { this.sampleViewURL = property; }

    private String patientViewURL;
    @Value("${patient_view.url}")
    public void setPatientViewURL(String property) { this.patientViewURL = property; }

    private static final String ARCHER = "mskarcher";
    private static final String RAINDANCE = "mskraindance";
    private static final String IMPACT = "mskimpact";

    private static Pattern dmpSampleIDPattern = initDMPSampleIDPattern();
    private static Pattern initDMPSampleIDPattern() {
        return Pattern.compile("(P-[0-9]{7,})-T[0-9]{2,}-(\\w{3,})");
    }

    @RequestMapping(
        value={"/cis/{sampleID}", "/darwin/{sampleID}"},
        method=RequestMethod.GET
    )
    public ModelAndView redirectIMPACT(@PathVariable String sampleID, ModelMap model) {
        return new ModelAndView(getRedirectURL(sampleID), model);
    }

    @RequestMapping(
        value="/crdb/{sampleID}",
        method=RequestMethod.GET
    )
    public ModelAndView redirectCRDB(@PathVariable String sampleID, ModelMap model) {
        return new ModelAndView(getRedirectURL(sampleID), model);
    }

    private String getRedirectURL(String sampleID) {
        String redirectURL = "redirect:" + sampleViewURL;
        String studyID = getCancerStudy(sampleID);
        if (!checkIfSampleExistsInStudy(studyID, sampleID)) {
            if (studyID.equals(ARCHER)) {
                String patientID = getPatientID(sampleID);
                if (patientID != null) {
                    redirectURL = "redirect:" + patientViewURL;
                    redirectURL = redirectURL.replace("STUDY_ID", IMPACT);
                    redirectURL = redirectURL.replace("CASE_ID", patientID);
                    return redirectURL;
                }
                // else patientID is null
            }
            // else sample doesn't exist in this study and it is not archer
        }
        // this will not work for invalid sample ids and sample
        // ids that do not belong to the expected study,
        // but in practice that should not happen because users are meant to
        // call /{cis|darwin|crdb}/{sampleID}/exists before displaying any URL
        redirectURL = redirectURL.replace("STUDY_ID", studyID);
        redirectURL = redirectURL.replace("SAMPLE_ID", sampleID);
        return redirectURL;
    }

    @RequestMapping(
        value={"/cis/{sampleID}/exists", "/darwin/{sampleID}/exists", "/crdb/{sampleID}/exists"},
        method=RequestMethod.GET
    )
    public @ResponseBody HashMap<String, Boolean> exists(@PathVariable String sampleID, ModelMap model) {
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        result.put("exists", new Boolean(checkIfSampleExists(sampleID)));
        return result;
    }

    private boolean checkIfPatientExists(String studyID, String sampleID) {
        try {
            String patientID = getPatientID(sampleID);
            if (patientID != null) {
                patientService.getPatientInStudy(studyID, patientID);
            } else { // could not parse out patient id
                return false;
            }
        } catch (PatientNotFoundException e) {
            return false;
        } catch (StudyNotFoundException e) {
            return false;
        }
        return true;
    }

    private boolean checkIfSampleExists(String sampleID) {
        String studyID = getCancerStudy(sampleID);
        // note if we map to a study that they do not have permission to view
        // then an AccessDeniedException will be thrown that we cannot catch here
        if (!checkIfSampleExistsInStudy(studyID, sampleID)) {
            if (studyID.equals(ARCHER)) {
                // check if patient exists in mskimpact
                return checkIfPatientExists(IMPACT, sampleID);
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfSampleExistsInStudy(String studyID, String sampleID) {
        try {
            sampleService.getSampleInStudy(studyID, sampleID);
        } catch (SampleNotFoundException e) {
            return false;
        } catch (StudyNotFoundException e) {
            return false;
        }
        // note if we map to a study that they do not have permission to view
        // then an AccessDeniedException will be thrown that we cannot catch here
        return true;
    }

    private String getPatientID(String sampleID) {
        Matcher matcher = dmpSampleIDPattern.matcher(sampleID);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getCancerStudy(String sampleID) {
        // TODO: create tech stack to get study id given sample id
        Matcher matcher = dmpSampleIDPattern.matcher(sampleID);
        if (matcher.find()) {
            String sampleIDSuffix = matcher.group(2);
            if (sampleIDSuffix.contains("TS") || sampleIDSuffix.contains("TB")) {
                return RAINDANCE;
            }
            else if (sampleIDSuffix.contains("AS") || sampleIDSuffix.contains("AH")) {
                return ARCHER;
            }
        }
        return IMPACT;
    }
}
