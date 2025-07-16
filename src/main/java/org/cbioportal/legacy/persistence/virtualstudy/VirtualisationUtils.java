package org.cbioportal.legacy.persistence.virtualstudy;

public class VirtualisationUtils {
  public static String calculateVirtualMoleculaProfileId(
      String virtualStudyId, String molecularProfileId) {
    return virtualStudyId + "_" + molecularProfileId;
  }
}
