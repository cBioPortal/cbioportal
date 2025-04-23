package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.model.TableRow;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.Function;

public abstract class GeneSampleWideTableDatatypeExporter extends GeneticProfileDatatypeExporter {

    private final GeneticProfileDataService geneticProfileDataService;

    public GeneSampleWideTableDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        super(geneticProfileService);
        this.geneticProfileDataService = geneticProfileDataService;
    }

    @Override
    protected Exporter composeExporterFor(GeneticProfileDatatypeMetadata metadata) {
        return new ProfileDataExporter(metadata);
    }


    private static final LinkedHashMap<String, Function<GeneticProfileData, String>> GENE_ROW = new LinkedHashMap<>();

    static {
        GENE_ROW.put("Hugo_Symbol", data -> data.getGene() == null ? null : data.getGene().getHugoGeneSymbol());
        GENE_ROW.put("Entrez_Gene_Id", data -> data.getGene() == null || data.getGene().getEntrezGeneId() == null ? null : data.getGene().getEntrezGeneId().toString());
    }

    private class ProfileDataExporter extends GeneticProfileExporter {
        private final GeneticProfileDatatypeMetadata metatdata;

        public ProfileDataExporter(GeneticProfileDatatypeMetadata metadata) {
            this.metatdata = metadata;
        }

        private static CloseableIterator<TableRow> composeRows(CloseableIterator<GeneticProfileData> geneticProfileData, List<String> sampleStableIds) {
            return new CloseableIterator<>() {
                @Override
                public void close() throws IOException {
                    geneticProfileData.close();
                }

                @Override
                public boolean hasNext() {
                    return geneticProfileData.hasNext();
                }

                @Override
                public TableRow next() {
                    var data = geneticProfileData.next();
                    if (data.getValues().size() != sampleStableIds.size()) {
                        throw new IllegalStateException("Number of values does not match number of sample stable IDs");
                    }
                    var row = new LinkedHashMap<String, String>();
                    for (var entry : GENE_ROW.entrySet()) {
                        row.put(entry.getKey(), entry.getValue().apply(data));
                    }
                    for (int i = 0; i < sampleStableIds.size(); i++) {
                        row.put(sampleStableIds.get(i), data.getValues().get(i));
                    }
                    return () -> row;
                }
            };
        }

        @Override
        protected Optional<GeneticProfileDatatypeMetadata> getMetadata(String studyId) {
            return Optional.of(metatdata);
        }

        @Override
        protected CloseableIterator<SequencedMap<String, String>> getData(String studyId) {
            var sampleStableIds = geneticProfileDataService.getSampleStableIds(metatdata.getStableId());
            for (String sampleStableId : sampleStableIds) {
                if (sampleStableId == null) {
                    throw new IllegalStateException("Sample stable ID is null");
                }
            }
            var geneticProfileData = geneticProfileDataService.getData(metatdata.getStableId());
            var header = new LinkedHashSet<String>();
            header.addAll(GENE_ROW.keySet());
            header.addAll(sampleStableIds);
            return new Table(composeRows(geneticProfileData, sampleStableIds), header);
        }
    }
}
