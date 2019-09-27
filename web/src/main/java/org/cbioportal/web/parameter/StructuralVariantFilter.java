/*
 * Copyright (c) 2018 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;

public class StructuralVariantFilter {

    @Size(min=1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> molecularProfileIds;
    private List<Integer> entrezGeneIds;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<SampleMolecularIdentifier> sampleMolecularIdentifiers;

    @AssertTrue
    private boolean isEitherMolecularProfileIdsOrSampleMolecularIdentifiersPresent() {
        return molecularProfileIds != null ^ sampleMolecularIdentifiers != null;
    }

    public List<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(List<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }

    public List<Integer> getEntrezGeneIds(){
        return entrezGeneIds;
    }

    public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
        this.entrezGeneIds = entrezGeneIds;
    }

    public List<SampleMolecularIdentifier> getSampleMolecularIdentifiers(){
        return sampleMolecularIdentifiers;
    }

    public void setSampleMolecularIdentifiers(List<SampleMolecularIdentifier> sampleMolecularIdentifiers) {
        this.sampleMolecularIdentifiers = sampleMolecularIdentifiers;
    }

}
