package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.function.BiFunction;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantFilterQuery;
import org.cbioportal.legacy.model.StructuralVariantQuery;
import org.cbioportal.legacy.persistence.StructuralVariantRepository;

public class VSAwareStructuralVariantRepository implements StructuralVariantRepository {

  private final VirtualizationService virtualizationService;
  private final StructuralVariantRepository structuralVariantRepository;

  public VSAwareStructuralVariantRepository(
      VirtualizationService virtualizationService,
      StructuralVariantRepository structuralVariantRepository) {
    this.virtualizationService = virtualizationService;
    this.structuralVariantRepository = structuralVariantRepository;
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariants(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<StructuralVariantQuery> structuralVariantQueries) {
    return fetchStructuralVariants(
        molecularProfileIds,
        sampleIds,
        (mpIds, sIds) ->
            structuralVariantRepository.fetchStructuralVariants(
                mpIds, sIds, entrezGeneIds, structuralVariantQueries));
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByGeneQueries(
      List<String> molecularProfileIds, List<String> sampleIds, List<GeneFilterQuery> geneQueries) {
    return fetchStructuralVariants(
        molecularProfileIds,
        sampleIds,
        (mpIds, sIds) ->
            structuralVariantRepository.fetchStructuralVariantsByGeneQueries(
                mpIds, sIds, geneQueries));
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByStructVarQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<StructuralVariantFilterQuery> structVarQueries) {
    return fetchStructuralVariants(
        molecularProfileIds,
        sampleIds,
        (mpIds, sIds) ->
            structuralVariantRepository.fetchStructuralVariantsByStructVarQueries(
                mpIds, sIds, structVarQueries));
  }

  private List<StructuralVariant> fetchStructuralVariants(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      BiFunction<List<String>, List<String>, List<StructuralVariant>> fetch) {
    return virtualizationService.handleMolecularData(
        molecularProfileIds,
        sampleIds,
        StructuralVariant::getMolecularProfileId,
        StructuralVariant::getSampleId,
        fetch,
        this::virtualizeStructuralVariant);
  }

  private StructuralVariant virtualizeStructuralVariant(
      MolecularProfile molecularProfile, StructuralVariant sv) {
    StructuralVariant virtualStructuralVariant = new StructuralVariant();
    virtualStructuralVariant.setMolecularProfileId(molecularProfile.getStableId());
    virtualStructuralVariant.setSampleId(sv.getSampleId());
    virtualStructuralVariant.setPatientId(sv.getPatientId());
    virtualStructuralVariant.setStudyId(molecularProfile.getCancerStudyIdentifier());
    virtualStructuralVariant.setSite1EntrezGeneId(sv.getSite1EntrezGeneId());
    virtualStructuralVariant.setSite1HugoSymbol(sv.getSite1HugoSymbol());
    virtualStructuralVariant.setSite1EnsemblTranscriptId(sv.getSite1EnsemblTranscriptId());
    virtualStructuralVariant.setSite1Chromosome(sv.getSite1Chromosome());
    virtualStructuralVariant.setSite1Position(sv.getSite1Position());
    virtualStructuralVariant.setSite1Contig(sv.getSite1Contig());
    virtualStructuralVariant.setSite1Region(sv.getSite1Region());
    virtualStructuralVariant.setSite1RegionNumber(sv.getSite1RegionNumber());
    virtualStructuralVariant.setSite1Description(sv.getSite1Description());
    virtualStructuralVariant.setSite2EntrezGeneId(sv.getSite2EntrezGeneId());
    virtualStructuralVariant.setSite2HugoSymbol(sv.getSite2HugoSymbol());
    virtualStructuralVariant.setSite2EnsemblTranscriptId(sv.getSite2EnsemblTranscriptId());
    virtualStructuralVariant.setSite2Chromosome(sv.getSite2Chromosome());
    virtualStructuralVariant.setSite2Position(sv.getSite2Position());
    virtualStructuralVariant.setSite2Contig(sv.getSite2Contig());
    virtualStructuralVariant.setSite2Region(sv.getSite2Region());
    virtualStructuralVariant.setSite2RegionNumber(sv.getSite2RegionNumber());
    virtualStructuralVariant.setSite2Description(sv.getSite2Description());
    virtualStructuralVariant.setSite2EffectOnFrame(sv.getSite2EffectOnFrame());
    virtualStructuralVariant.setNcbiBuild(sv.getNcbiBuild());
    virtualStructuralVariant.setDnaSupport(sv.getDnaSupport());
    virtualStructuralVariant.setRnaSupport(sv.getRnaSupport());
    virtualStructuralVariant.setNormalReadCount(sv.getNormalReadCount());
    virtualStructuralVariant.setTumorReadCount(sv.getTumorReadCount());
    virtualStructuralVariant.setNormalVariantCount(sv.getNormalVariantCount());
    virtualStructuralVariant.setTumorVariantCount(sv.getTumorVariantCount());
    virtualStructuralVariant.setNormalPairedEndReadCount(sv.getNormalPairedEndReadCount());
    virtualStructuralVariant.setTumorPairedEndReadCount(sv.getTumorPairedEndReadCount());
    virtualStructuralVariant.setNormalSplitReadCount(sv.getNormalSplitReadCount());
    virtualStructuralVariant.setTumorSplitReadCount(sv.getTumorSplitReadCount());
    virtualStructuralVariant.setAnnotation(sv.getAnnotation());
    virtualStructuralVariant.setBreakpointType(sv.getBreakpointType());
    virtualStructuralVariant.setConnectionType(sv.getConnectionType());
    virtualStructuralVariant.setEventInfo(sv.getEventInfo());
    virtualStructuralVariant.setVariantClass(sv.getVariantClass());
    virtualStructuralVariant.setLength(sv.getLength());
    virtualStructuralVariant.setComments(sv.getComments());
    virtualStructuralVariant.setDriverFilter(sv.getDriverFilter());
    virtualStructuralVariant.setDriverFilterAnn(sv.getDriverFilterAnn());
    virtualStructuralVariant.setDriverTiersFilter(sv.getDriverTiersFilter());
    virtualStructuralVariant.setDriverTiersFilterAnn(sv.getDriverTiersFilterAnn());
    virtualStructuralVariant.setSvStatus(sv.getSvStatus());
    return virtualStructuralVariant;
  }
}
