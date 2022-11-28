package org.mskcc.cbio.portal.util;

import org.cbioportal.model.*;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CnaEvent;
import org.mskcc.cbio.portal.model.GeneticProfile;

import java.util.*;

import static org.mskcc.cbio.portal.scripts.ImportTabDelimData.*;

public class CnaUtil {

    private final HashMap<String, Integer> columnIndexMap;

    public static final String HUGO_SYMBOL = "Hugo_Symbol";
    public static final String ENTREZ_GENE_ID = "Entrez_Gene_Id";
    public static final String SAMPLE_ID = "Sample_Id";
    public static final String VALUE = "Value";
    public static final String CBP_DRIVER = "cbp_driver";
    public static final String CBP_DRIVER_ANNOTATION = "cbp_driver_annotation";
    public static final String CBP_DRIVER_TIERS = "cbp_driver_tiers";

    public static void storeCnaEvents(
        Set<CnaEvent.Event> existingCnaEvents,
        List<CnaEvent> cnaEventsToAdd
    ) throws DaoException {
        for (CnaEvent cnaEvent : cnaEventsToAdd) {
            if (!CNA.AMP.equals(cnaEvent.getAlteration()) && !CNA.HOMDEL.equals(cnaEvent.getAlteration())) {
                continue;
            }
            Optional<CnaEvent.Event> existingCnaEvent = existingCnaEvents
                .stream()
                .filter(e -> e.equals(cnaEvent.getEvent()))
                .findFirst();
            if (existingCnaEvent.isPresent()) {
                cnaEvent.setEventId(existingCnaEvent.get().getEventId());
                DaoCnaEvent.addCaseCnaEvent(cnaEvent, false);
            } else {
                DaoCnaEvent.addCaseCnaEvent(cnaEvent, true);
                existingCnaEvents.add(cnaEvent.getEvent());
            }
        }
    }

    public static final String CBP_DRIVER_TIERS_ANNOTATION = "cbp_driver_tiers_annotation";

    public CnaUtil(String headerRow) {
        this.columnIndexMap = new HashMap<>();
        String[] headerParts = headerRow.trim().split("\t");

        // Find header indices
        for (int i = 0; i < headerParts.length; i++) {
            // Put the index in the map
            this.columnIndexMap.put(headerParts[i].toLowerCase(), i);
        }

        if (getColumnIndex(HUGO_SYMBOL) == -1 && getColumnIndex(ENTREZ_GENE_ID) == -1) {
            throw new RuntimeException("Error: at least one of the following columns should be present: Hugo_Symbol or Entrez_Gene_Id");
        }
    }

    public CnaEvent createEvent(GeneticProfile geneticProfile, int sampleId, String[] parts) {
        int cnaProfileId = geneticProfile.getGeneticProfileId();
        long entrezGeneId = getEntrezSymbol(parts);
        short alteration = createAlteration(parts);
        CnaEvent cna = new CnaEvent(sampleId, cnaProfileId, entrezGeneId, alteration);
        cna.setDriverFilter(TabDelimitedFileUtil.getPartString(getColumnIndex(CnaUtil.CBP_DRIVER), parts));
        cna.setDriverFilterAnnotation(TabDelimitedFileUtil.getPartString(getColumnIndex(CnaUtil.CBP_DRIVER_ANNOTATION), parts));
        cna.setDriverTiersFilter(TabDelimitedFileUtil.getPartString(getColumnIndex(CnaUtil.CBP_DRIVER_TIERS), parts));
        cna.setDriverTiersFilterAnnotation(TabDelimitedFileUtil.getPartString(getColumnIndex(CnaUtil.CBP_DRIVER_TIERS_ANNOTATION), parts));
        return cna;
    }

    public long getEntrezSymbol(String[] parts) {
        String entrezAsString = TabDelimitedFileUtil.getPartString(getColumnIndex(CnaUtil.ENTREZ_GENE_ID), parts);
        if (entrezAsString.isEmpty()) {
            return 0;
        } else if (!entrezAsString.matches("^\\d+$")) {
            ProgressMonitor.logWarning("Ignoring line with invalid Entrez_Id " + entrezAsString);
            return 0;
        }
        return TabDelimitedFileUtil.getPartLong(getColumnIndex(CnaUtil.ENTREZ_GENE_ID), parts);
    }

    public String getHugoSymbol(String[] parts) {
        return TabDelimitedFileUtil.getPartString(getColumnIndex(CnaUtil.HUGO_SYMBOL), parts);
    }

    private short createAlteration(String[] parts) {
        String value = TabDelimitedFileUtil.getPartString(getColumnIndex(CnaUtil.VALUE), parts);
        String result = value;
        // temporary solution -- change partial deletion back to full deletion.
        if (value.equals(CNA_VALUE_PARTIAL_DELETION)) {
            result = CNA_VALUE_HOMOZYGOUS_DELETION;
        }
        return Integer.valueOf(result).shortValue();
    }

    public int getColumnIndex(String colName) {
        Integer index = this.columnIndexMap.get(colName.toLowerCase());
        if (index == null) {
            index = -1;
        }
        return index;
    }

    public String getSampleIdStr(String[] parts) {
        return TabDelimitedFileUtil.getPartString(getColumnIndex(CnaUtil.SAMPLE_ID), parts);
    }
}
