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

package org.cbioportal.web.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetCorrelation;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MutSig;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.model.Patient;
import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.web.mixin.CancerStudyMixin;
import org.cbioportal.web.mixin.ClinicalAttributeCountMixin;
import org.cbioportal.web.mixin.ClinicalAttributeMixin;
import org.cbioportal.web.mixin.ClinicalDataCountMixin;
import org.cbioportal.web.mixin.ClinicalDataMixin;
import org.cbioportal.web.mixin.ClinicalEventDataMixin;
import org.cbioportal.web.mixin.ClinicalEventMixin;
import org.cbioportal.web.mixin.CopyNumberSegMixin;
import org.cbioportal.web.mixin.GeneMixin;
import org.cbioportal.web.mixin.GenePanelMixin;
import org.cbioportal.web.mixin.GenePanelToGeneMixin;
import org.cbioportal.web.mixin.GenesetCorrelationMixin;
import org.cbioportal.web.mixin.GenesetMixin;
import org.cbioportal.web.mixin.GenesetMolecularDataMixin;
import org.cbioportal.web.mixin.GisticMixin;
import org.cbioportal.web.mixin.GisticToGeneMixin;
import org.cbioportal.web.mixin.MolecularProfileMixin;
import org.cbioportal.web.mixin.MutSigMixin;
import org.cbioportal.web.mixin.MutationMixin;
import org.cbioportal.web.mixin.MutationSpectrumMixin;
import org.cbioportal.web.mixin.PatientMixin;
import org.cbioportal.web.mixin.ResourceDefinitionMixin;
import org.cbioportal.web.mixin.SampleListMixin;
import org.cbioportal.web.mixin.SampleMixin;
import org.cbioportal.web.mixin.TypeOfCancerMixin;

public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {

        super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        super.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Map<Class<?>, Class<?>> mixinMap = new HashMap<>();
        mixinMap.put(CancerStudy.class, CancerStudyMixin.class);
        mixinMap.put(ClinicalAttribute.class, ClinicalAttributeMixin.class);
        mixinMap.put(ClinicalAttributeCount.class, ClinicalAttributeCountMixin.class);
        mixinMap.put(ClinicalData.class, ClinicalDataMixin.class);
        mixinMap.put(ClinicalDataCount.class, ClinicalDataCountMixin.class);
        mixinMap.put(ClinicalEvent.class, ClinicalEventMixin.class);
        mixinMap.put(ClinicalEventData.class, ClinicalEventDataMixin.class);
        mixinMap.put(CopyNumberSeg.class, CopyNumberSegMixin.class);
        mixinMap.put(Gene.class, GeneMixin.class);
        mixinMap.put(GenePanel.class, GenePanelMixin.class);
        mixinMap.put(GenePanelToGene.class, GenePanelToGeneMixin.class);
        mixinMap.put(Geneset.class, GenesetMixin.class);
        mixinMap.put(GenesetMolecularData.class, GenesetMolecularDataMixin.class);
        mixinMap.put(GenesetCorrelation.class, GenesetCorrelationMixin.class);
        mixinMap.put(MolecularProfile.class, MolecularProfileMixin.class);
        mixinMap.put(Gistic.class, GisticMixin.class);
        mixinMap.put(GisticToGene.class, GisticToGeneMixin.class);
        mixinMap.put(Mutation.class, MutationMixin.class);
        mixinMap.put(MutationSpectrum.class, MutationSpectrumMixin.class);
        mixinMap.put(MutSig.class, MutSigMixin.class);
        mixinMap.put(Patient.class, PatientMixin.class);
        mixinMap.put(Sample.class, SampleMixin.class);
        mixinMap.put(SampleList.class, SampleListMixin.class);
        mixinMap.put(TypeOfCancer.class, TypeOfCancerMixin.class);
        mixinMap.put(ResourceDefinition.class, ResourceDefinitionMixin.class);
        super.setMixIns(mixinMap);
    }
}
