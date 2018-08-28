package org.cbioportal.web.interceptor;

import org.cbioportal.model.Alteration;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.MolecularData;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.model.NumericGeneMolecularData;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

import java.util.Base64;
import java.util.List;

@ControllerAdvice("org.cbioportal.web")
public class UniqueKeyInterceptor extends AbstractMappingJacksonResponseBodyAdvice {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder().withoutPadding();
    public static final String DELIMITER = ":";

    @Override
    protected void beforeBodyWriteInternal(MappingJacksonValue mappingJacksonValue, MediaType mediaType, 
                                           MethodParameter methodParameter, ServerHttpRequest serverHttpRequest, 
                                           ServerHttpResponse serverHttpResponse) {
        
        Object value = mappingJacksonValue.getValue();
        if (value instanceof List) {
            List list = (List) value;
            for (Object object : list) {
                if (object instanceof Alteration) {
                    
                    Alteration alteration = (Alteration) object;
                    alteration.setUniqueSampleKey(calculateBase64(alteration.getSampleId(), alteration.getStudyId()));
                    alteration.setUniquePatientKey(calculateBase64(alteration.getPatientId(), alteration.getStudyId()));
                } else if (object instanceof ClinicalData) {
                    
                    ClinicalData clinicalData = (ClinicalData) object;
                    if (clinicalData.getSampleId() != null) {
                        clinicalData.setUniqueSampleKey(calculateBase64(clinicalData.getSampleId(), 
                            clinicalData.getStudyId()));
                    }
                    clinicalData.setUniquePatientKey(calculateBase64(clinicalData.getPatientId(), 
                        clinicalData.getStudyId()));
                } else if (object instanceof ClinicalEvent) {
                    
                    ClinicalEvent clinicalEvent = (ClinicalEvent) object;
                    clinicalEvent.setUniquePatientKey(calculateBase64(clinicalEvent.getPatientId(), 
                        clinicalEvent.getStudyId()));
                } else if (object instanceof CopyNumberSeg) {
                    
                    CopyNumberSeg copyNumberSeg = (CopyNumberSeg) object;
                    copyNumberSeg.setUniqueSampleKey(calculateBase64(copyNumberSeg.getSampleStableId(), 
                        copyNumberSeg.getCancerStudyIdentifier()));
                    copyNumberSeg.setUniquePatientKey(calculateBase64(copyNumberSeg.getPatientId(), 
                        copyNumberSeg.getCancerStudyIdentifier()));
                } else if (object instanceof GenePanelData) {
                    
                    GenePanelData genePanelData = (GenePanelData) object;
                    genePanelData.setUniqueSampleKey(calculateBase64(genePanelData.getSampleId(), 
                        genePanelData.getStudyId()));
                    genePanelData.setUniquePatientKey(calculateBase64(genePanelData.getPatientId(), 
                        genePanelData.getStudyId()));
                } else if (object instanceof MolecularData) {
                    
                    MolecularData molecularData = (MolecularData) object;
                    molecularData.setUniqueSampleKey(calculateBase64(molecularData.getSampleId(), 
                        molecularData.getStudyId()));
                    molecularData.setUniquePatientKey(calculateBase64(molecularData.getPatientId(), 
                        molecularData.getStudyId()));
                } else if (object instanceof MrnaPercentile) {
                    
                    MrnaPercentile mrnaPercentile = (MrnaPercentile) object;
                    mrnaPercentile.setUniqueSampleKey(calculateBase64(mrnaPercentile.getSampleId(), 
                        mrnaPercentile.getStudyId()));
                    mrnaPercentile.setUniquePatientKey(calculateBase64(mrnaPercentile.getPatientId(), 
                        mrnaPercentile.getStudyId()));
                } else if (object instanceof MutationSpectrum) {
                    
                    MutationSpectrum mutationSpectrum = (MutationSpectrum) object;
                    mutationSpectrum.setUniqueSampleKey(calculateBase64(mutationSpectrum.getSampleId(), 
                        mutationSpectrum.getStudyId()));
                    mutationSpectrum.setUniquePatientKey(calculateBase64(mutationSpectrum.getPatientId(), 
                        mutationSpectrum.getStudyId()));
                } else if (object instanceof NumericGeneMolecularData) {
                    
                    NumericGeneMolecularData numericGeneMolecularData = (NumericGeneMolecularData) object;
                    numericGeneMolecularData.setUniqueSampleKey(calculateBase64(numericGeneMolecularData.getSampleId(), 
                        numericGeneMolecularData.getStudyId()));
                    numericGeneMolecularData.setUniquePatientKey(calculateBase64(numericGeneMolecularData.getPatientId(), 
                        numericGeneMolecularData.getStudyId()));
                }else if (object instanceof Patient) {
                    
                    Patient patient = (Patient) object;
                    patient.setUniquePatientKey(calculateBase64(patient.getStableId(), 
                        patient.getCancerStudyIdentifier()));
                } else if (object instanceof Sample) {
                    
                    Sample sample = (Sample) object;
                    sample.setUniqueSampleKey(calculateBase64(sample.getStableId(), sample.getCancerStudyIdentifier()));
                    sample.setUniquePatientKey(calculateBase64(sample.getPatientStableId(), 
                        sample.getCancerStudyIdentifier()));
                }
            }
        }
    }
    
    private String calculateBase64(String firstInput, String secondInput) {
        return BASE64_ENCODER.encodeToString((firstInput + DELIMITER + secondInput).getBytes());
    }
}
