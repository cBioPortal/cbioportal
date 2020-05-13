package org.cbioportal.service.util;

import java.util.List;
import java.util.stream.Collectors;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantCountByGene;
import org.springframework.stereotype.Component;

@Component
public class MutationMapperUtils {

    public List<StructuralVariant> mapFusionsToStructuralVariants(List<Mutation> fusions) {

        return fusions.stream().map(fusion -> {
            StructuralVariant structuralVariant = new StructuralVariant();

            // Sample details
            structuralVariant.setPatientId(fusion.getPatientId());
            structuralVariant.setSampleId(fusion.getSampleId());
            structuralVariant.setStudyId(fusion.getStudyId());
            structuralVariant.setMolecularProfileId(fusion.getMolecularProfileId());

            // Fusion details
            structuralVariant.setSite1EntrezGeneId(fusion.getEntrezGeneId());
            structuralVariant.setSite1HugoSymbol(fusion.getGene().getHugoGeneSymbol());
            structuralVariant.setSite1Chromosome(fusion.getChr());
            structuralVariant.setCenter(fusion.getCenter());
            structuralVariant.setSite1Position(fusion.getStartPosition().intValue());
            structuralVariant.setComments(fusion.getKeyword());
            structuralVariant.setNcbiBuild(fusion.getNcbiBuild());

            return structuralVariant;
        }).collect(Collectors.toList());
    }

    public List<StructuralVariantCountByGene> mapFusionCoutsToStructuralVariantCounts(
            List<MutationCountByGene> mutationCountByGenes) {

        return mutationCountByGenes.stream().map(fusion -> {
            StructuralVariantCountByGene structuralVariantCountByGene = new StructuralVariantCountByGene();
            structuralVariantCountByGene.setEntrezGeneId(fusion.getEntrezGeneId());
            structuralVariantCountByGene.setHugoGeneSymbol(fusion.getHugoGeneSymbol());
            structuralVariantCountByGene.setNumberOfAlteredCases(fusion.getNumberOfAlteredCases());
            structuralVariantCountByGene.setNumberOfProfiledCases(fusion.getNumberOfProfiledCases());
            structuralVariantCountByGene.setTotalCount(fusion.getTotalCount());
            structuralVariantCountByGene.setMatchingGenePanelIds(fusion.getMatchingGenePanelIds());
            return structuralVariantCountByGene;
        }).collect(Collectors.toList());
    }

}
