package org.cbioportal.service;

import org.cbioportal.model.ReadPermission;
import org.springframework.security.core.Authentication;

import java.util.Collection;

public interface ReadPermissionService {
    public void setReadPermission(Collection<? extends ReadPermission> entities, Authentication authentication);
}
