package org.cbioportal.service.impl.vs;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.cbioportal.persistence.cachemaputil.CacheMapUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO enable cache for this class. Think on how to evict it when a new virtual study is created
public class VSAwareCacheMapUtil implements CacheMapUtil {
    private final CacheMapUtil cacheMapUtil;
    private final PublishedVirtualStudyService publishedVirtualStudyService;

    public VSAwareCacheMapUtil(CacheMapUtil cacheMapUtil, PublishedVirtualStudyService publishedVirtualStudyService) {
        this.cacheMapUtil = cacheMapUtil;
        this.publishedVirtualStudyService = publishedVirtualStudyService;
    }

    @Override
    public Map<String, MolecularProfile> getMolecularProfileMap() {
        List<MolecularProfile> molecularProfiles = cacheMapUtil.getMolecularProfileMap().values().stream().toList();
        return Stream.concat(
                publishedVirtualStudyService.getAllVirtualMolecularProfiles(molecularProfiles).stream(),
                molecularProfiles.stream())
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity())); 
    }

    @Override
    public Map<String, SampleList> getSampleListMap() {
        return cacheMapUtil.getSampleListMap();
    }

    @Override
    public Map<String, CancerStudy> getCancerStudyMap() {
        return Stream.concat(
            publishedVirtualStudyService.getAllPublishedVirtualStudies().stream().map(publishedVirtualStudyService::toCancerStudy), 
            cacheMapUtil.getCancerStudyMap().values().stream())
        .collect(Collectors.toMap(CancerStudy::getCancerStudyIdentifier, Function.identity()));
    }

    @Override
    public boolean hasCacheEnabled() {
        return true;
    }
}
