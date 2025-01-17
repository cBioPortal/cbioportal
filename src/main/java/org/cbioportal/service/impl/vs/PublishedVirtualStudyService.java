package org.cbioportal.service.impl.vs;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.exception.CancerTypeNotFoundException;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.web.parameter.VirtualStudySamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

//TODO implement caching here?
@Service
public class PublishedVirtualStudyService {

    public static final String ALL_USERS = "*";
    private static final Logger LOG = LoggerFactory.getLogger(PublishedVirtualStudyService.class);
    private final CancerTypeService cancerTypeService;
    private final SessionServiceRequestHandler sessionServiceRequestHandler;
    private final TypeOfCancer defaultCancerType;

    public PublishedVirtualStudyService(SessionServiceRequestHandler sessionServiceRequestHandler,
                                        CancerTypeService cancerTypeService,
                                        @Value("${default_type_of_cancer_id:mixed}") String defaultTypeOfCancerId) throws CancerTypeNotFoundException {
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.cancerTypeService = cancerTypeService;
        this.defaultCancerType = cancerTypeService.getCancerType(defaultTypeOfCancerId);
    }

    public List<VirtualStudy> getAllPublishedVirtualStudies() {
        return sessionServiceRequestHandler.getVirtualStudiesAccessibleToUser(ALL_USERS); 
    }

    public List<VirtualStudy> getPublishedVirtualStudiesByKeyword(String keyword) {
        return getAllPublishedVirtualStudies().stream()
            .filter(cs -> shouldSelect(cs, keyword)).toList();
    }

    public Optional<VirtualStudy> getPublishedVirtualStudyById(String id) {
        VirtualStudy virtualStudyById = sessionServiceRequestHandler.getVirtualStudyById(id);
        boolean notPublishedVs = virtualStudyById != null && !virtualStudyById.getData().getUsers().contains(ALL_USERS);
        if (notPublishedVs) {
            return Optional.empty();
        }
        return Optional.ofNullable(virtualStudyById);
    }
    
    public List<MolecularProfile> getAllVirtualMolecularProfiles(List<MolecularProfile> molecularProfiles) {
        Map<String, List<MolecularProfile>> molecularProfilesByStudyId = molecularProfiles.stream().collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));
        return getAllPublishedVirtualStudies().stream().flatMap(virtualStudy -> {
            List<String> studyIds = virtualStudy.getData().getStudies().stream().map(VirtualStudySamples::getId).toList();
            //TODO can we check if any of samples in the virtual study is in the molecular profile?
            return studyIds.stream().flatMap(studyId ->
                molecularProfilesByStudyId.get(studyId).stream().map(molecularProfile -> {
                    MolecularProfile virtualMolecularProfile =  new MolecularProfile();
                    virtualMolecularProfile.setMolecularAlterationType(molecularProfile.getMolecularAlterationType());
                    virtualMolecularProfile.setCancerStudyIdentifier(virtualStudy.getId());
                    virtualMolecularProfile.setDatatype(molecularProfile.getDatatype());
                    virtualMolecularProfile.setStableId(molecularProfile.getStableId().replace(molecularProfile.getCancerStudyIdentifier(), virtualStudy.getId()));
                    virtualMolecularProfile.setName(molecularProfile.getName());
                    virtualMolecularProfile.setDescription(molecularProfile.getDescription());
                    virtualMolecularProfile.setCancerStudy(toCancerStudy(virtualStudy));
                    virtualMolecularProfile.setPatientLevel(molecularProfile.getPatientLevel());
                    virtualMolecularProfile.setGenericAssayType(molecularProfile.getGenericAssayType());
                    virtualMolecularProfile.setPivotThreshold(molecularProfile.getPivotThreshold());
                    virtualMolecularProfile.setShowProfileInAnalysisTab(molecularProfile.getShowProfileInAnalysisTab());
                    virtualMolecularProfile.setSortOrder(molecularProfile.getSortOrder());
                    return virtualMolecularProfile;
                    //TODO check if molecular profile can be merged
                }).collect(Collectors.groupingBy(MolecularProfile::getStableId)).values().stream().map(List::getFirst)
            );
        }).toList();
    }

    public CancerStudy toCancerStudy(VirtualStudy vs) {
        VirtualStudyData vsd = vs.getData();
        CancerStudy cs = new CancerStudy();
        cs.setCancerStudyIdentifier(vs.getId());
        cs.setName(vsd.getName());
        try {
            cs.setTypeOfCancer(vsd.getTypeOfCancerId() == null ? this.defaultCancerType : cancerTypeService.getCancerType(vsd.getTypeOfCancerId()));
            cs.setTypeOfCancerId(vsd.getTypeOfCancerId() == null ? this.defaultCancerType.getTypeOfCancerId() : vsd.getTypeOfCancerId());
        } catch (CancerTypeNotFoundException e) {
            throw new RuntimeException(e);
        }
        cs.setDescription(vsd.getDescription());
        cs.setPmid(vsd.getPmid());
        //TODO run filters on the dynamic virtual study
        cs.setAllSampleCount(vsd.getStudies().stream().map(s -> s.getSamples().size()).reduce(0, Integer::sum));
        //TODO we can implement this field for published virtual studies to predefine rights on groups even before the study is created
        cs.setGroups("PUBLIC");
        return cs;
    }

    private static boolean shouldSelect(VirtualStudy vs, String keyword) {
        //TODO improve the search. The keyword can be also sent to mongo to search for virtual studies
        if (keyword == null) {
            return true;
        }
        return vs.getData().getName().toLowerCase().contains(keyword.toLowerCase());
    }

    /**
     * Publishes virtual study optionally updating metadata fields
     * @param id - id of public virtual study to publish
     * @param typeOfCancerId - if specified (not null) update type of cancer of published virtual study
     * @param pmid  - if specified (not null) update PubMed ID of published virtual study
     */
    public void publishVirtualStudy(String id, String typeOfCancerId, String pmid) {
        VirtualStudy virtualStudyDataToPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
        VirtualStudyData virtualStudyData = virtualStudyDataToPublish.getData();
        updateStudyMetadataFieldsIfSpecified(virtualStudyData, typeOfCancerId, pmid);
        virtualStudyData.setUsers(Set.of(ALL_USERS));
        sessionServiceRequestHandler.updateVirtualStudy(virtualStudyDataToPublish);
    }

    /**
     * Un-publish virtual study
     * @param id - id of public virtual study to un-publish
     */
    public void unPublishVirtualStudy(String id) {
        VirtualStudy virtualStudyToUnPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
        if (virtualStudyToUnPublish == null) {
            throw new NoSuchElementException("The virtual study with id=" + id + " has not been found in the public list.");
        }
        VirtualStudyData virtualStudyData = virtualStudyToUnPublish.getData();
        Set<String> users = virtualStudyData.getUsers();
        if (users == null || users.isEmpty() || !users.contains(ALL_USERS)) {
            throw new NoSuchElementException("The virtual study with id=" + id + " has not been found in the public list.");
        }
        virtualStudyData.setUsers(Set.of(virtualStudyData.getOwner()));
        sessionServiceRequestHandler.updateVirtualStudy(virtualStudyToUnPublish);
    }

    private void updateStudyMetadataFieldsIfSpecified(VirtualStudyData virtualStudyData, String typeOfCancerId, String pmid) {
        if (typeOfCancerId != null) {
            try {
                cancerTypeService.getCancerType(typeOfCancerId);
                virtualStudyData.setTypeOfCancerId(typeOfCancerId);
            } catch (CancerTypeNotFoundException e) {
                LOG.error("No cancer type with id={} were found.", typeOfCancerId);
                throw new IllegalArgumentException( "The cancer type is not valid: " + typeOfCancerId);
            }
        }
        if (pmid != null) {
            virtualStudyData.setPmid(pmid);
        }
    }
}
