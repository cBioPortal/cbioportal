package org.cbioportal.persistence.cachemaputil;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
// This implementation of the CacheMapUtils is instantiated on portals where all uses can access any study.
@ConditionalOnProperty(name = "security.method_authorization_enabled", havingValue = "false", matchIfMissing = true)
public class InactiveCacheMapUtil implements CacheMapUtil {

    // Since user-permission evaluation is not needed when this bean is present, throw an error when it is accessed.

    @Override
    public Map<String, MolecularProfile> getMolecularProfileMap() {
        throw new RuntimeException("A CacheMapUtils method was called on a portal where studies are accessible to all users.");
    }

    @Override
    public Map<String, SampleList> getSampleListMap() {
        throw new RuntimeException("A CacheMapUtils method was called on a portal where studies are accessible to all users.");
    }

    @Override
    public Map<String, CancerStudy> getCancerStudyMap() {
        throw new RuntimeException("A CacheMapUtils method was called on a portal where studies are accessible to all users.");
    }

    //  bean is only instantiated when there is no user authorization
    @Override
    public boolean hasCacheEnabled() {
        return false;
    }

}