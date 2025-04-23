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

public abstract class ProteinLevelDatatypeExporter extends GeneticProfileDatatypeExporter {

    private final GeneticProfileDataService geneticProfileDataService;

    public ProteinLevelDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        super(geneticProfileService);
        this.geneticProfileDataService = geneticProfileDataService;
    }

    @Override
    protected Exporter composeExporterFor(GeneticProfileDatatypeMetadata metadata) {
        return new MrnaExpressionGeneticProfileExporter(metadata);
    }

    @Override
    protected String getGeneticAlterationType() {
        return "PROTEIN_LEVEL";
    }

    private static final LinkedHashMap<String, Function<GeneticProfileData, String>> ROW = new LinkedHashMap<>();

    static {
        ROW.put("Composite.Element.REF", data -> {
            if (data.getGene() == null) {
                return null;
            }
            String hugoGeneSymbol = data.getGene().getHugoGeneSymbol();
            if ("phosphoprotein".equals(data.getGene().getType())) {
                String[] parts = hugoGeneSymbol.split("_");
                //first part is actual hugo gene symbol. the second part is the phosphosite. example of composed string: PDK1|PDK1_pS24
                if (parts.length != 2) {
                    throw new IllegalStateException("Unexpected format for phosphoprotein: " + hugoGeneSymbol);
                }
                String hgs = parts[0];
                String phosphosite = parts[1];
                if (phosphosite.charAt(0) != 'p' && phosphosite.charAt(0) != 'P') {
                    throw new IllegalStateException("Unexpected format for phosphosite: " + phosphosite);
                }
                return hgs + "|" + hgs + "_p" + phosphosite.substring(1);
            } else {
                return hugoGeneSymbol + "|" + hugoGeneSymbol;
            }
        });
    }

    private class MrnaExpressionGeneticProfileExporter extends GeneticProfileExporter {
        private final GeneticProfileDatatypeMetadata metatdata;

        public MrnaExpressionGeneticProfileExporter(GeneticProfileDatatypeMetadata metadata) {
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
                    for (var entry : ROW.entrySet()) {
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
            header.addAll(ROW.keySet());
            header.addAll(sampleStableIds);
            return new Table(composeRows(geneticProfileData, sampleStableIds), header);
        }
    }
}
