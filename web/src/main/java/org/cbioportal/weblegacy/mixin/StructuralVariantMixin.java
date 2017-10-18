/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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

package org.cbioportal.weblegacy.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.Gene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;

public class StructuralVariantMixin {
    @JsonIgnore
    private Integer sampleId;
    private String annotation;
    private String breakpointType;
    private String comments;
    private String confidenceClass;
    private String connectionType;
    private String eventInfo;
    private Integer mapq;
    private Integer normalReadCount;
    private Integer normalVariantCount;
    private Integer pairedEndReadSupport;
    private String site1Chrom;
    private String site1Desc;
    private String site1Gene;
    private Integer site1Pos;
    private String site2Chrom;
    private String site2Desc;
    private String site2Gene;
    private Integer site2Pos;
    private Integer splitReadSupport;
    private String svClassName;
    private String svDesc;
    private Integer svLength;
    private Integer tumorReadCount;
    private Integer tumorVariantCount;
    private String variantStatusName;
    private String geneticProfileId;
    private Gene gene1;
    private Gene gene2;
    @JsonUnwrapped
    private Sample sample;
    @JsonUnwrapped
    private MolecularProfile geneticProfile;
}
