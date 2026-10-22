/*
 * Copyright (c) 2018 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.web.parameter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.cbioportal.legacy.model.StructuralVariantQuery;

public class StructuralVariantFilter implements Serializable {

  @Size(max = PagingConstants.MAX_PAGE_SIZE)
  private List<SampleMolecularIdentifier> sampleMolecularIdentifiers;

  @Size(max = PagingConstants.MAX_PAGE_SIZE)
  private List<String> molecularProfileIds;

  @Size(max = PagingConstants.MAX_PAGE_SIZE)
  private List<Integer> entrezGeneIds;

  @Valid
  @Size(max = PagingConstants.MAX_PAGE_SIZE)
  private List<StructuralVariantQuery> structuralVariantQueries;

  @AssertTrue
  private boolean isEitherMolecularProfileIdsOrSampleMolecularIdentifiersPresent() {
    return !CollectionUtils.isEmpty(molecularProfileIds)
        ^ !CollectionUtils.isEmpty(sampleMolecularIdentifiers);
  }

  public List<String> getMolecularProfileIds() {
    return molecularProfileIds;
  }

  public void setMolecularProfileIds(List<String> molecularProfileIds) {
    this.molecularProfileIds = molecularProfileIds;
  }

  public List<Integer> getEntrezGeneIds() {
    return entrezGeneIds;
  }

  public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
    this.entrezGeneIds = entrezGeneIds;
  }

  public List<SampleMolecularIdentifier> getSampleMolecularIdentifiers() {
    return sampleMolecularIdentifiers;
  }

  public void setSampleMolecularIdentifiers(
      List<SampleMolecularIdentifier> sampleMolecularIdentifiers) {
    this.sampleMolecularIdentifiers = sampleMolecularIdentifiers;
  }

  public List<StructuralVariantQuery> getStructuralVariantQueries() {
    return structuralVariantQueries;
  }

  public void setStructuralVariantQueries(List<StructuralVariantQuery> structuralVariantQueries) {
    this.structuralVariantQueries = structuralVariantQueries;
  }
}
