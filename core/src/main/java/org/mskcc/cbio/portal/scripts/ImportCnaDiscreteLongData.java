/*
 * Copyright (c) 2018 - 2022 The Hyve B.V.
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
package org.mskcc.cbio.portal.scripts;

import com.google.common.base.*;
import com.google.common.collect.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import java.util.Optional;
import java.util.*;
import java.util.stream.*;

import static com.google.common.collect.Lists.*;
import static java.lang.String.*;

public class ImportCnaDiscreteLongData {

    private final File cnaFile;
    private final int geneticProfileId;
    private final GeneticAlterationImporter geneticAlterationGeneImporter;
    private String genePanel;
    private final DaoGeneOptimized daoGene;
    private CnaUtil cnaUtil;
    private Set<CnaEvent.Event> existingCnaEvents = new HashSet<>();
    private int samplesSkipped = 0;

    private final ArrayList<SampleIdGeneticProfileId> sampleIdGeneticProfileIds = new ArrayList<>();

    public ImportCnaDiscreteLongData(
        File cnaFile,
        int geneticProfileId,
        String genePanel,
        DaoGeneOptimized daoGene,
        DaoGeneticAlteration daoGeneticAlteration
    ) {
        this.cnaFile = cnaFile;
        this.geneticProfileId = geneticProfileId;
        this.genePanel = genePanel;
        this.daoGene = daoGene;
        this.geneticAlterationGeneImporter = new GeneticAlterationImporter(geneticProfileId, daoGeneticAlteration);
    }

    public void importData() throws Exception {
        FileReader reader = new FileReader(this.cnaFile);
        BufferedReader buf = new BufferedReader(reader);

        // Pass first line with headers to util:
        String line = buf.readLine();
        int lineIndex = 1;
        this.cnaUtil = new CnaUtil(line);

        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);

        boolean isDiscretizedCnaProfile = geneticProfile != null
            && geneticProfile.getGeneticAlterationType() == GeneticAlterationType.COPY_NUMBER_ALTERATION
            && geneticProfile.showProfileInAnalysisTab();

        if (isDiscretizedCnaProfile) {
            existingCnaEvents.addAll(DaoCnaEvent.getAllCnaEvents());
            MySQLbulkLoader.bulkLoadOn();
        }

        CnaImportData toImport = new CnaImportData();

        while ((line = buf.readLine()) != null) {
            lineIndex++;
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            this.extractDataToImport(geneticProfile, line, lineIndex, toImport);
        }

        DaoGeneticProfileSamples.addGeneticProfileSamples(
            geneticProfileId,
            newArrayList(toImport.eventsTable.columnKeySet())
        );

        for (Long entrezId : toImport.eventsTable.rowKeySet()) {
            boolean added = storeGeneticAlterations(toImport, entrezId);
            if (added) {
                storeCnaEvents(toImport, entrezId);
            } else {
                ProgressMonitor.logWarning("Values not added to gene with entrezId: " + entrezId + ". Skip creation of cna events.");
            }
        }

        ProgressMonitor.setCurrentMessage(" --> total number of samples skipped (normal samples): " + getSamplesSkipped()
        );
        buf.close();
        MySQLbulkLoader.flushAll();
    }

    /**
     * First we collect all the events
     * to import all events related to a single gene in one query:
     */
    public void extractDataToImport(
        GeneticProfile geneticProfile,
        String line,
        int lineIndex,
        CnaImportData importContainer
    ) throws Exception {
        boolean hasData = !line.startsWith("#") && line.trim().length() > 0;
        if (!hasData) {
            return;
        }
        String[] lineParts = line.split("\t", -1);
        CanonicalGene gene = this.getGene(cnaUtil.getEntrezSymbol(lineParts), lineParts, cnaUtil);
        importContainer.genes.add(gene);

        if (gene == null) {
            return;
        }

        int cancerStudyId = geneticProfile.getCancerStudyId();

        String sampleIdStr = cnaUtil.getSampleIdStr(lineParts);
        Sample sample = findSample(sampleIdStr, cancerStudyId);
        createSampleProfile(sample);

        long entrezId = gene.getEntrezGeneId();
        int sampleId = sample.getInternalId();
        CnaEventImportData eventContainer = new CnaEventImportData();
        Table<Long, Integer, CnaEventImportData> geneBySampleEventTable = importContainer.eventsTable;

        if (!geneBySampleEventTable.contains(entrezId, sample.getInternalId())) {
            geneBySampleEventTable.put(entrezId, sampleId, eventContainer);
        } else {
            ProgressMonitor.logWarning(format("Skipping line %d with duplicate gene %d and sample %d", lineIndex, entrezId, sampleId));
        }

        eventContainer.geneticEvent = cnaUtil.createEvent(geneticProfile, sample.getInternalId(), lineParts);
    }

    /**
     * Store all cna events related to a single gene
     */
    private void storeCnaEvents(CnaImportData toImport, Long entrezId) throws DaoException {
        List<CnaEvent> events = toImport.eventsTable
            .row(entrezId)
            .values()
            .stream()
            .filter(v -> v.geneticEvent != null)
            .map(v -> v.geneticEvent)
            .collect(Collectors.toList());
        CnaUtil.storeCnaEvents(existingCnaEvents, events);
    }

    /**
     * Store all events related to a single gene
     */
    private boolean storeGeneticAlterations(CnaImportData toImport, Long entrezId) throws DaoException {
        String[] values = toImport.eventsTable
            .row(entrezId)
            .values()
            .stream()
            .filter(v -> v.geneticEvent != null)
            .map(v -> "" + v
                .geneticEvent
                .getAlteration()
                .getCode()
            )
            .toArray(String[]::new);

        Optional<CanonicalGene> gene = toImport.genes
            .stream()
            .filter(g -> g != null && g.getEntrezGeneId() == entrezId)
            .findFirst();

        if (!gene.isPresent()) {
            ProgressMonitor.logWarning("No gene found for entrezId: " + entrezId);
            return false;
        }

        String geneSymbol = !Strings.isNullOrEmpty(gene.get().getHugoGeneSymbolAllCaps())
            ? gene.get().getHugoGeneSymbolAllCaps()
            : "" + entrezId;

        return this.geneticAlterationGeneImporter.store(values, gene.get(), geneSymbol);
    }

    /**
     * @return null when no gene could be found
     */
    private CanonicalGene getGene(
        long entrez,
        String[] parts,
        CnaUtil util
    ) {

        String hugoSymbol = util.getHugoSymbol(parts);

        if (Strings.isNullOrEmpty(hugoSymbol) && entrez == 0) {
            ProgressMonitor.logWarning("Ignoring line with no Hugo_Symbol and no Entrez_Id");
            return null;
        }
        if (entrez != 0) {
            //try entrez:
            return this.daoGene.getGene(entrez);
        } else if (!Strings.isNullOrEmpty(hugoSymbol)) {
            //try hugo:
            if (hugoSymbol.contains("///") || hugoSymbol.contains("---")) {
                //  Ignore gene IDs separated by ///.  This indicates that
                //  the line contains information regarding multiple genes, and
                //  we cannot currently handle this.
                //  Also, ignore gene IDs that are specified as ---.  This indicates
                //  the line contains information regarding an unknown gene, and
                //  we cannot currently handle this.
                ProgressMonitor.logWarning("Ignoring gene ID:  " + hugoSymbol);
                return null;
            }
            int ix = hugoSymbol.indexOf("|");
            if (ix > 0) {
                hugoSymbol = hugoSymbol.substring(0, ix);
            }
            List<CanonicalGene> genes = daoGene.getGene(hugoSymbol, true);
            if (genes.size() > 1) {
                throw new IllegalStateException("Found multiple genes for Hugo symbol " + hugoSymbol + " while importing cna");
            }
            return genes.get(0);
        } else {
            ProgressMonitor.logWarning("Entrez_Id " + entrez + " not found. Record will be skipped for this gene.");
            return null;
        }
    }

    /**
     * Find sample and create sample profile when needed
     *
     * @return boolean created or not
     */
    public boolean createSampleProfile(
        Sample sample
    ) throws Exception {
        boolean inDatabase = DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId);
        Integer genePanelID = (genePanel == null) ? null : GeneticProfileUtil.getGenePanelId(genePanel);
        SampleIdGeneticProfileId toCreate = new SampleIdGeneticProfileId(sample.getInternalId(), geneticProfileId);
        boolean isQueued = this.sampleIdGeneticProfileIds.contains(toCreate);
        if (!inDatabase && !isQueued) {
            DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId, genePanelID);
            this.sampleIdGeneticProfileIds.add(toCreate);
            return true;
        }
        return false;
    }


    private static class SampleIdGeneticProfileId {
        public int sampleId;
        public int geneticProfileId;

        public SampleIdGeneticProfileId(int sampleId, int geneticProfileId) {
            this.sampleId = sampleId;
            this.geneticProfileId = geneticProfileId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            SampleIdGeneticProfileId that = (SampleIdGeneticProfileId) o;
            return sampleId == that.sampleId
                && geneticProfileId == that.geneticProfileId;
        }
    }

    /**
     * Find sample and create sample profile when needed
     */
    public Sample findSample(
        String sampleId,
        int cancerStudyId
    ) throws Exception {
        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(
            cancerStudyId,
            StableIdUtil.getSampleId(sampleId)
        );
        // can be null in case of 'normal' sample, throw exception if not 'normal' and sample not found in db
        if (sample == null) {
            if (StableIdUtil.isNormal(sampleId)) {
                samplesSkipped++;
                return null;
            } else {
                throw new RuntimeException("Unknown sample id '" + StableIdUtil.getSampleId(sampleId));
            }
        }
        return sample;
    }

    private class CnaImportData {
        // Entrez ID x Sample ID table:
        public Table<Long, Integer, CnaEventImportData> eventsTable = HashBasedTable.create();
        public Set<CanonicalGene> genes = new HashSet<>();
    }
    
    private class CnaEventImportData {
        public int line;
        public CnaEvent geneticEvent;
        public String geneSymbol;
    }

    public int getSamplesSkipped() {
        return samplesSkipped;
    }
}

