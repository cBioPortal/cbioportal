package org.cbioportal.application.file.export.exporters;

import com.google.common.collect.Iterators;
import org.cbioportal.application.file.export.services.GenePanelMatrixService;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.model.TableRow;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GenePanelMatrixDatatypeExporter extends DataTypeExporter<GeneticProfileDatatypeMetadata, Table> {

    private final GenePanelMatrixService genePanelMatrixService;

    public GenePanelMatrixDatatypeExporter(GenePanelMatrixService genePanelMatrixService) {
        this.genePanelMatrixService = genePanelMatrixService;
    }

    @Override
    protected Optional<GeneticProfileDatatypeMetadata> getMetadata(String studyId, Set<String> sampleIds) {
        if (!genePanelMatrixService.hasGenePanelMatrix(studyId, sampleIds)) {
            return Optional.empty();
        }
        GeneticProfileDatatypeMetadata metadata = new GeneticProfileDatatypeMetadata();
        metadata.setCancerStudyIdentifier(studyId);
        metadata.setGeneticAlterationType("GENE_PANEL_MATRIX");
        metadata.setDatatype("GENE_PANEL_MATRIX");
        return Optional.of(metadata);
    }

    @Override
    protected Table getData(String studyId, Set<String> sampleIds) {
        CloseableIterator<GenePanelMatrixItem> genePanelMatrixItems = genePanelMatrixService.getGenePanelMatrix(studyId, sampleIds);
        var rowIterator = Iterators.peekingIterator(genePanelMatrixItems);
        List<String> geneProfileIds = genePanelMatrixService.getDistinctGeneProfileIdsWithGenePanelMatrix(studyId, sampleIds);
        var header = new LinkedHashSet<String>();
        header.add("SAMPLE_ID");
        List<String> genePlatforms = geneProfileIds.stream().map(stableId -> withoutStudySuffix(studyId, stableId)).toList();
        header.addAll(genePlatforms);
        return new Table(new CloseableIterator<>() {
            @Override
            public void close() throws IOException {
                genePanelMatrixItems.close();
            }

            @Override
            public boolean hasNext() {
                return genePanelMatrixItems.hasNext();
            }

            @Override
            public TableRow next() {
                GenePanelMatrixItem genePanelMatrixItem = rowIterator.next();
                if (rowIterator.hasNext() && genePanelMatrixItem.getRowKey().compareTo(rowIterator.peek().getRowKey()) > 0) {
                    throw new IllegalStateException("The keys are not in ascending order:" + genePanelMatrixItem.getRowKey() + " and " + rowIterator.peek().getRowKey());
                }
                var row = new HashMap<String, String>();
                row.put("SAMPLE_ID", genePanelMatrixItem.getSampleStableId());
                row.put(withoutStudySuffix(studyId, genePanelMatrixItem.getGeneticProfileStableId()), genePanelMatrixItem.getGenePanelStableId());
                while (rowIterator.hasNext()
                    && rowIterator.peek().getRowKey().equals(genePanelMatrixItem.getRowKey())) {
                    genePanelMatrixItem = rowIterator.next();
                    row.put(withoutStudySuffix(studyId, genePanelMatrixItem.getGeneticProfileStableId()), genePanelMatrixItem.getGenePanelStableId());
                }
                var result = new LinkedHashMap<String, String>();
                header.forEach(h -> {
                    if (row.containsKey(h)) {
                        result.put(h, row.remove(h));
                    } else {
                        result.put(h, "NA");
                    }
                });
                if (!row.isEmpty()) {
                    throw new IllegalStateException("The following sample profile has not been included in the row: " + row.keySet());
                }
                return () -> result;
            }
        }, header);
    }

    private static String withoutStudySuffix(String studyId, String stableId) {
        var removePrefix = studyId + "_";
        return stableId.replace(removePrefix, "");
    }
}
