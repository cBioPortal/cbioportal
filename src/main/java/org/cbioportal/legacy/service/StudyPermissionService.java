package org.cbioportal.legacy.service;

import java.util.Collection;
import org.cbioportal.legacy.model.StudyPermission;
import org.springframework.security.core.Authentication;

public interface StudyPermissionService {
  public void setPermissions(
      Collection<? extends StudyPermission> entities, Authentication authentication);
}
