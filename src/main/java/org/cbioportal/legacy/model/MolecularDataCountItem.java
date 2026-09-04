package org.cbioportal.legacy.model;

import java.io.Serializable;

/**
 * Represents a count of molecular data items for a specific molecular profile.
 * <p>
 * This model is typically used in API responses to convey the number of molecular data records
 * associated with a given molecular profile in the cBioPortal legacy API.
 * </p>
 * <p>
 * Fields:
 * <ul>
 *   <li><b>molecularProfileId</b>: The unique identifier of the molecular profile.</li>
 *   <li><b>count</b>: The number of molecular data items associated with the profile.</li>
 * </ul>
 * </p>
 */
public class MolecularDataCountItem implements Serializable {

  private String molecularProfileId;
  private Integer count;

  public String getMolecularProfileId() {
    return molecularProfileId;
  }

  public void setMolecularProfileId(String molecularProfileId) {
    this.molecularProfileId = molecularProfileId;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }
}
