package org.cbioportal.legacy.service;

import java.util.Collection;
import org.cbioportal.legacy.model.ReadPermission;
import org.springframework.security.core.Authentication;

public interface ReadPermissionService {
  public void setReadPermission(
      Collection<? extends ReadPermission> entities, Authentication authentication);
}
