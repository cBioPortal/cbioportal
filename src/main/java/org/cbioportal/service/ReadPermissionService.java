package org.cbioportal.service;

import java.util.Collection;
import org.cbioportal.model.ReadPermission;
import org.springframework.security.core.Authentication;

public interface ReadPermissionService {
  public void setReadPermission(
      Collection<? extends ReadPermission> entities, Authentication authentication);
}
