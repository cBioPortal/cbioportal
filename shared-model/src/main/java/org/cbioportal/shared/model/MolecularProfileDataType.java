package org.cbioportal.shared.model;

/**
 * Values of a molecular profile's {@code datatype} that code checks for. The column holds other
 * values as well (for example CONTINUOUS, Z-SCORE or MAF), so this is not a complete list.
 */
public final class MolecularProfileDataType {

  public static final String DISCRETE = "DISCRETE";

  /**
   * An input format only: the importer converts discrete long copy-number data to {@link #DISCRETE}
   * when it stores the profile.
   */
  public static final String DISCRETE_LONG = "DISCRETE_LONG";

  private MolecularProfileDataType() {}
}
