/*
 * Copyright (c) 2016 Berlin Institute of Health
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Berlin Institute of Health has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Berlin Institute of Health be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Berlin Institute of Health has been
 * advised of the possibility of such damage.
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
package org.mskcc.cbio.portal.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

// TODO(holtgrewe): The class is somewhat on the heavier side but having things in one place has its charms...
// TODO(holtgrewe): Remove debug output to stderr
// TODO(holtgrewe): Currently, many things ares hidden, could become more public in the future

/**
 * Utility class for accessing Civic annotation
 * 
 * @author Manuel Holtgrewe <manuel.holtgrewe@bihealth.de>
 */
public final class CivicAnotationUtil {

    /** Prefix of the file names (release date) */
    public static final String PREFIX = "01-Apr-2016-";
    /** File name of clinical evidence summary */
    public static final String CLINICAL_EVIDENCE_SUMMARIES = "ClinicalEvidenceSummaries.tsv";
    /** File name for gene summaries */
    public static final String GENE_SUMMARIES = "GeneSummaries.tsv";
    /** File name for variant group summaries */
    public static final String VARIANT_GROUP_SUMMARIES = "VariantGroupSummaries.tsv";
    /** File name for variant summaries */
    public static final String VARIANT_SUMMARIES = "VariantSummaries.tsv";

    /** List of all ClinicalEvidenceSummary object */
    private static ImmutableList<ClinicalEvidenceSummary> clinicalEvidenceSummaries = null;
    /** List of all ClinicalEvidenceStats object */
    private static ImmutableList<ClinicalEvidenceStats> clinicalEvidenceStats = null;
    /** List of all GeneSummary object */
    private static ImmutableList<GeneSummary> geneSummaries = null;
    /** List of all VariantGroupSummary object */
    private static ImmutableList<VariantGroupSummary> variantGroupSummaries = null; // by gene
    /** List of all VariantSummary object */
    private static ImmutableList<VariantSummary> variantSummaries = null;

    // In theory, we could have a map of (HUGO, alteration) to list of objects but as Java does not have nice
    // generic pairs and the lists will be small, it might even be faster to just iterate the lists.
    /** Access VariantSummary objects by HUGO gene name */
    private static ImmutableMap<String, ImmutableList<VariantSummary>> variantSummaryMap = null;
    /** Access ClinicalEvidenceSummary objects by HUGO gene name */
    private static ImmutableMap<String, ImmutableList<ClinicalEvidenceSummary>> clinicalEvidenceMap = null;
    /** Access ClinicalEvidenceStats objects by HUGO gene name */
    private static ImmutableMap<String, ImmutableList<ClinicalEvidenceStats>> clinicalEvidenceStatsMap = null;

    /**
     * Get list of VariantSummary objects for a given gene and alteration.
     * 
     * @param gene
     *            HUGO name of the gene
     * @param alteration
     *            Description of the protein change
     * @return List of VariantSummary describing the variant
     */
    public static ImmutableList<VariantSummary> getVariantSummary(String gene, String alteration) throws IOException {
        loadSummaries();
        System.err.println("Querying variant summary for " + gene + " " + alteration + " ...");
        ImmutableList.Builder<VariantSummary> result = new Builder<>();
        if (!variantSummaryMap.containsKey(gene))
            return result.build();

        for (VariantSummary vs : variantSummaryMap.get(gene)) {
            System.err.println("Considering " + vs);
            if (vs.variant.equals(alteration))
                result.add(vs);
        }

        System.err.println(" => " + result);

        return result.build();
    }

    /**
     * Get list of ClinicalEvidenceSummary objects for a given gene and alteration.
     * 
     * @param gene
     *            HUGO name of the gene
     * @param alteration
     *            Description of the protein change
     * @return List of ClinicalEvidenceSummary describing the evidence related to the variant
     */
    public static ImmutableList<ClinicalEvidenceSummary> getClinicalEvidenceSummary(String gene, String alteration)
            throws IOException {
        loadSummaries();
        System.err.println("Querying variant summary for " + gene + " " + alteration + " ...");
        ImmutableList.Builder<ClinicalEvidenceSummary> result = new Builder<>();
        if (!clinicalEvidenceMap.containsKey(gene))
            return result.build();

        for (ClinicalEvidenceSummary vs : clinicalEvidenceMap.get(gene)) {
            System.err.println("Considering " + vs);
            if (vs.variant.equals(alteration))
                result.add(vs);
        }

        System.err.println(" => " + result);

        return result.build();
    }

    /**
     * Get list of ClinicalEvidenceStats objects for a given gene and alteration.
     * 
     * @param gene
     *            HUGO name of the gene
     * @param alteration
     *            Description of the protein change
     * @return List of ClinicalEvidenceSummary describing the evidence related to the variant
     */
    public static ImmutableList<ClinicalEvidenceStats> getClinicalEvidenceStats(String gene, String alteration)
            throws IOException {
        loadSummaries();
        System.err.println("Querying evidence stats for " + gene + " " + alteration + " ...");
        ImmutableList.Builder<ClinicalEvidenceStats> result = new Builder<>();
        if (!clinicalEvidenceStatsMap.containsKey(gene))
            return result.build();

        for (ClinicalEvidenceStats vs : clinicalEvidenceStatsMap.get(gene)) {
            System.err.println("Considering " + vs);
            if (vs.variant.equals(alteration))
                result.add(vs);
        }

        System.err.println(" => " + result);

        return result.build();
    }

    /** @return A list of string-to-string hash maps for the variant summaries, suitable for serialization to JSON */
    public static List<HashMap<String, String>> prepareVariantSummariesForJSON(
            List<VariantSummary> civicVariantSummaries) {
        List<HashMap<String, String>> result = new ArrayList<>();
        for (VariantSummary vs : civicVariantSummaries)
            result.add(vs.forJSON());
        return result;
    }

    /**
     * @return A list of string-to-string hash maps for the clinical evidence summaries, suitable for serialization to
     *         JSON
     */
    public static List<HashMap<String, String>> prepareClinicalEvidenceSummariesForJSON(
            List<ClinicalEvidenceSummary> civicClinicalEvidenceSummaries) {
        List<HashMap<String, String>> result = new ArrayList<>();
        for (ClinicalEvidenceSummary vs : civicClinicalEvidenceSummaries)
            result.add(vs.forJSON());
        return result;
    }

    /**
     * @return A list of string-to-string hash maps for the clinical evidence stats, suitable for serialization to JSON
     */
    public static List<HashMap<String, String>> prepareClinicalEvidenceStatsForJSON(
            List<ClinicalEvidenceStats> civicClinicalEvidenceStats) {
        List<HashMap<String, String>> result = new ArrayList<>();
        for (ClinicalEvidenceStats vs : civicClinicalEvidenceStats)
            result.add(vs.forJSON());
        return result;
    }

    /**
     * Load all summaries
     */
    private synchronized static void loadSummaries() throws IOException {
        loadClinicalEvidenceSummary();
        buildClinicalEvidenceStats();
        loadGeneSummaries();
        loadVariantGroupSummaries();
        loadVariantSummaries();
    }

    /**
     * Open the civic file with the given file name
     * 
     * If the path in "civic.base_path" is empty or null then use the built-in files, otherwise files from the file
     * system as configured in configuration.
     */
    private static InputStream openCivicFile(String fileName) throws FileNotFoundException {
        String pathBase = GlobalProperties.civicPathBase();
        String prefix = GlobalProperties.civicPathPrefix();
        if (pathBase == null || pathBase.isEmpty()) {
            return CivicAnotationUtil.class.getClassLoader().getResourceAsStream("civic/" + PREFIX + fileName);
        } else {
            return new FileInputStream(pathBase + "/" + prefix + fileName);
        }
    }

    /**
     * Load clinical evidence TSV file into clinicalEvidenceSummaries
     */
    private static void loadClinicalEvidenceSummary() throws IOException {
        if (clinicalEvidenceSummaries != null)
            return;

        ImmutableList.Builder<ClinicalEvidenceSummary> builder = new Builder<>();
        HashMap<String, List<ClinicalEvidenceSummary>> tmpMap = new HashMap<>();

        InputStream inputStream = openCivicFile(CLINICAL_EVIDENCE_SUMMARIES);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        // Mapping of key in header to position in list.
        HashMap<String, Integer> key2pos = null;

        while ((line = br.readLine()) != null) {
            if (key2pos == null) { // read header
                key2pos = new HashMap<>();
                String[] arr = line.split("\t");
                for (int i = 0; i < arr.length; ++i)
                    key2pos.put(arr[i], i);
                continue;
            }

            ArrayList<String> items = new ArrayList<>(Arrays.asList(line.split("\t", -1)));
            if (items.size() < key2pos.size())
                continue; // incomplete, skip
            for (String pVar : items.get(key2pos.get("variant")).split("[+/]")) {
                ClinicalEvidenceSummary summary = new ClinicalEvidenceSummary(items.get(key2pos.get("gene")),
                        Integer.parseInt(items.get(key2pos.get("entrez_id"))), pVar, items.get(key2pos.get("disease")),
                        items.get(key2pos.get("doid")), Arrays.asList(items.get(key2pos.get("drugs")).split(",")),
                        items.get(key2pos.get("evidence_type")), items.get(key2pos.get("evidence_direction")),
                        items.get(key2pos.get("clinical_significance")), items.get(key2pos.get("evidence_statement")),
                        items.get(key2pos.get("pubmed_id")), items.get(key2pos.get("citation")),
                        items.get(key2pos.get("rating")));

                System.err.println("Read evidence summary " + summary);

                // Add to the list
                builder.add(summary);
                // Add to the map
                if (tmpMap.containsKey(summary.gene)) {
                    tmpMap.get(summary.gene).add(summary);
                } else {
                    List<ClinicalEvidenceSummary> xs = new ArrayList<>();
                    xs.add(summary);
                    tmpMap.put(summary.gene, xs);
                }
            }
        }

        clinicalEvidenceSummaries = builder.build();

        ImmutableMap.Builder<String, ImmutableList<ClinicalEvidenceSummary>> mapBuilder = new ImmutableMap.Builder<>();
        for (Entry<String, List<ClinicalEvidenceSummary>> x : tmpMap.entrySet())
            mapBuilder.put(x.getKey(), ImmutableList.copyOf(x.getValue()));
        clinicalEvidenceMap = mapBuilder.build();
    }

    /**
     * Build statistics on clinical evidence
     */
    private static void buildClinicalEvidenceStats() {
        ImmutableList.Builder<ClinicalEvidenceStats> builder = new ImmutableList.Builder<>();
        HashMap<String, List<ClinicalEvidenceStats>> tmpMap = new HashMap<>();

        for (Entry<String, ImmutableList<ClinicalEvidenceSummary>> elem : clinicalEvidenceMap.entrySet()) {
            HashMap<String, Integer> numDiagnostic = new HashMap<>();
            HashMap<String, Integer> numPredictive = new HashMap<>();
            HashMap<String, Integer> numPrognostic = new HashMap<>();

            String gene = null;
            int entrezID = -1;

            for (ClinicalEvidenceSummary summary : elem.getValue()) {
                gene = summary.gene;
                entrezID = summary.entrezID;

                if (!numDiagnostic.containsKey(summary.variant))
                    numDiagnostic.put(summary.variant, 0);
                if (!numPredictive.containsKey(summary.variant))
                    numPredictive.put(summary.variant, 0);
                if (!numPrognostic.containsKey(summary.variant))
                    numPrognostic.put(summary.variant, 0);

                if (summary.evidenceType.equals("Diagnostic"))
                    numDiagnostic.put(summary.variant, numDiagnostic.get(summary.variant) + 1);
                if (summary.evidenceType.equals("Predictive"))
                    numPredictive.put(summary.variant, numPredictive.get(summary.variant) + 1);
                if (summary.evidenceType.equals("Prognostic"))
                    numPrognostic.put(summary.variant, numPrognostic.get(summary.variant) + 1);
            }

            assert gene != null;
            assert entrezID != -1;

            for (Entry<String, Integer> elem2 : numDiagnostic.entrySet()) {
                final String variant = elem2.getKey();

                assert numDiagnostic.get(variant) != null;
                assert numPredictive.get(variant) != null;
                assert numPrognostic.get(variant) != null;

                ClinicalEvidenceStats stats = new ClinicalEvidenceStats(gene, entrezID, variant,
                        numDiagnostic.get(variant), numPredictive.get(variant), numPrognostic.get(variant));
                builder.add(stats);

                // Add to the map
                if (tmpMap.containsKey(gene)) {
                    tmpMap.get(gene).add(stats);
                } else {
                    List<ClinicalEvidenceStats> xs = new ArrayList<>();
                    xs.add(stats);
                    tmpMap.put(gene, xs);
                }
            }
        }

        clinicalEvidenceStats = builder.build();

        ImmutableMap.Builder<String, ImmutableList<ClinicalEvidenceStats>> mapBuilder = new ImmutableMap.Builder<>();
        for (Entry<String, List<ClinicalEvidenceStats>> elem : tmpMap.entrySet())
            mapBuilder.put(elem.getKey(), ImmutableList.copyOf(elem.getValue()));
        clinicalEvidenceStatsMap = mapBuilder.build();
    }

    /**
     * Load gene summary TSV file into geneSummaries
     */
    private static void loadGeneSummaries() throws IOException {
        if (geneSummaries != null)
            return;

        ImmutableList.Builder<GeneSummary> builder = new Builder<>();

        InputStream inputStream = openCivicFile(GENE_SUMMARIES);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        // Mapping of key in header to position in list.
        HashMap<String, Integer> key2pos = null;

        while ((line = br.readLine()) != null) {
            if (key2pos == null) { // read header
                key2pos = new HashMap<>();
                String[] arr = line.split("\t");
                for (int i = 0; i < arr.length; ++i)
                    key2pos.put(arr[i], i);
                continue;
            }

            ArrayList<String> items = new ArrayList<>(Arrays.asList(line.split("\t", -1)));
            if (items.size() < key2pos.size())
                continue; // incomplete, skip
            builder.add(new GeneSummary(items.get(key2pos.get("name")),
                    Integer.parseInt(items.get(key2pos.get("entrez_id"))), items.get(key2pos.get("description"))));
        }

        geneSummaries = builder.build();
    }

    /**
     * Load gene summary TSV file into geneSummaries
     */
    private static void loadVariantGroupSummaries() throws IOException {
        if (variantGroupSummaries != null)
            return;

        ImmutableList.Builder<VariantGroupSummary> builder = new Builder<>();

        InputStream inputStream = openCivicFile(VARIANT_GROUP_SUMMARIES);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        // Mapping of key in header to position in list.
        HashMap<String, Integer> key2pos = null;

        while ((line = br.readLine()) != null) {
            if (key2pos == null) { // read header
                key2pos = new HashMap<>();
                String[] arr = line.split("\t");
                for (int i = 0; i < arr.length; ++i)
                    key2pos.put(arr[i], i);
                continue;
            }

            String[] items = line.split("\t", -1);
            if (items.length < key2pos.size())
                continue; // incomplete, skip
            builder.add(
                    new VariantGroupSummary(items[key2pos.get("variant_group")], items[key2pos.get("description")]));
        }

        variantGroupSummaries = builder.build();
    }

    /**
     * Load gene summary TSV file into geneSummaries
     */
    private static void loadVariantSummaries() throws IOException {
        if (variantSummaries != null)
            return;

        ImmutableList.Builder<VariantSummary> builder = new Builder<>();
        HashMap<String, List<VariantSummary>> tmpMap = new HashMap<>();

        System.err.println("Reading CIVIC file civic/" + PREFIX + VARIANT_SUMMARIES);
        InputStream inputStream = openCivicFile(VARIANT_SUMMARIES);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        // Mapping of key in header to position in list.
        HashMap<String, Integer> key2pos = null;

        while ((line = br.readLine()) != null) {
            if (key2pos == null) { // read header
                key2pos = new HashMap<>();
                String[] arr = line.split("\t");
                for (int i = 0; i < arr.length; ++i)
                    key2pos.put(arr[i], i);
                continue;
            }

            ArrayList<String> items = new ArrayList<>(Arrays.asList(line.split("\t", -1)));
            if (items.size() < key2pos.size()) {
                System.err.println("Skipping (" + items.size() + " != " + key2pos.size() + "): " + line);
                continue; // incomplete, skip
            }
            for (String pVar : items.get(key2pos.get("variant")).split("[+/]")) {
                VariantSummary summary = new VariantSummary(items.get(key2pos.get("gene")),
                        Integer.parseInt(items.get(key2pos.get("entrez_id"))), pVar, items.get(key2pos.get("summary")),
                        Arrays.asList(items.get(key2pos.get("variant_groups")).split(",")),
                        items.get(key2pos.get("variant_civic_url")));

                System.err.println("Read variant summary " + summary);

                // Add to the list
                builder.add(summary);
                // Add to the map
                if (tmpMap.containsKey(summary.gene)) {
                    tmpMap.get(summary.gene).add(summary);
                } else {
                    List<VariantSummary> xs = new ArrayList<>();
                    xs.add(summary);
                    tmpMap.put(summary.gene, xs);
                }
            }
        }

        variantSummaries = builder.build();

        ImmutableMap.Builder<String, ImmutableList<VariantSummary>> mapBuilder = new ImmutableMap.Builder<>();
        for (Entry<String, List<VariantSummary>> x : tmpMap.entrySet())
            mapBuilder.put(x.getKey(), ImmutableList.copyOf(x.getValue()));
        variantSummaryMap = mapBuilder.build();
    }

    /**
     * Statistics about clinical evidence.
     */
    public final static class ClinicalEvidenceStats {
        private final String gene;
        private final int entrezID;
        private final String variant;
        private final int numDiagnostic;
        private final int numPredictive;
        private final int numPrognostic;

        public ClinicalEvidenceStats(String gene, int entrezID, String variant, int numDiagnostic, int numPredictive,
                int numPrognostic) {
            this.gene = gene;
            this.entrezID = entrezID;
            this.variant = variant;
            this.numDiagnostic = numDiagnostic;
            this.numPredictive = numPredictive;
            this.numPrognostic = numPrognostic;
        }

        /** @return HashMap representation for JSON */
        public HashMap<String, String> forJSON() {
            HashMap<String, String> result = new HashMap<>();
            result.put("gene", gene);
            result.put("entrezID", Integer.toString(entrezID));
            result.put("variant", variant);
            result.put("numDiagnostic", Integer.toString(numDiagnostic));
            result.put("numPredictive", Integer.toString(numPredictive));
            result.put("numPrognostic", Integer.toString(numPrognostic));
            return result;
        }

        @Override
        public String toString() {
            return "ClinicalEvidenceStats [gene=" + gene + ", entrezID=" + entrezID + ", variant=" + variant
                    + ", numDiagnostic=" + numDiagnostic + ", numPredictive=" + numPredictive + ", numPrognostic="
                    + numPrognostic + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + entrezID;
            result = prime * result + ((gene == null) ? 0 : gene.hashCode());
            result = prime * result + numDiagnostic;
            result = prime * result + numPredictive;
            result = prime * result + numPrognostic;
            result = prime * result + ((variant == null) ? 0 : variant.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ClinicalEvidenceStats other = (ClinicalEvidenceStats) obj;
            if (entrezID != other.entrezID)
                return false;
            if (gene == null) {
                if (other.gene != null)
                    return false;
            } else if (!gene.equals(other.gene))
                return false;
            if (numDiagnostic != other.numDiagnostic)
                return false;
            if (numPredictive != other.numPredictive)
                return false;
            if (numPrognostic != other.numPrognostic)
                return false;
            if (variant == null) {
                if (other.variant != null)
                    return false;
            } else if (!variant.equals(other.variant))
                return false;
            return true;
        }
    }

    /**
     * Clinical evidence summary
     */
    public final static class ClinicalEvidenceSummary {
        private final String gene;
        private final int entrezID;
        private final String variant;
        private final String disease;
        private final String doid;
        private final ImmutableList<String> drugs;
        private final String evidenceType;
        private final String evidenceDirection;
        private final String clinicalSignificance;
        private final String statement;
        private final String pubmedID;
        private final String citation;
        private final String rating;

        public ClinicalEvidenceSummary(String gene, int entrezID, String variant, String disease, String doid,
                List<String> drugs, String evidenceTye, String evidenceDirection, String clinicalSignificance,
                String statement, String pubmedID, String citation, String rating) {
            this.gene = gene;
            this.entrezID = entrezID;
            this.variant = variant;
            this.disease = disease;
            this.doid = doid;
            this.drugs = ImmutableList.copyOf(drugs);
            this.evidenceType = evidenceTye;
            this.evidenceDirection = evidenceDirection;
            this.clinicalSignificance = clinicalSignificance;
            this.statement = statement;
            this.pubmedID = pubmedID;
            this.citation = citation;
            this.rating = rating;
        }

        /** @return HashMap representation for JSON */
        public HashMap<String, String> forJSON() {
            HashMap<String, String> result = new HashMap<>();
            result.put("gene", gene);
            result.put("entrezID", Integer.toString(entrezID));
            result.put("variant", variant);
            result.put("disease", disease);
            result.put("doid", doid);
            result.put("drugs", Joiner.on(", ").join(drugs));
            result.put("evidenceType", evidenceType);
            result.put("evidenceDirection", evidenceDirection);
            result.put("clinicalSignificance", clinicalSignificance);
            result.put("statement", statement);
            result.put("pubmedID", pubmedID);
            result.put("citation", citation);
            result.put("rating", rating);
            return result;
        }

        @Override
        public String toString() {
            return "ClinicalEvidenceSummary [gene=" + gene + ", entrezID=" + entrezID + ", variant=" + variant
                    + ", disease=" + disease + ", doid=" + doid + ", drugs=" + drugs + ", evidenceType=" + evidenceType
                    + ", evidenceDirection=" + evidenceDirection + ", clinicalSignificance=" + clinicalSignificance
                    + ", statement=" + statement + ", pubmedID=" + pubmedID + ", citation=" + citation + ", rating="
                    + rating + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((citation == null) ? 0 : citation.hashCode());
            result = prime * result + ((clinicalSignificance == null) ? 0 : clinicalSignificance.hashCode());
            result = prime * result + ((disease == null) ? 0 : disease.hashCode());
            result = prime * result + ((doid == null) ? 0 : doid.hashCode());
            result = prime * result + ((drugs == null) ? 0 : drugs.hashCode());
            result = prime * result + entrezID;
            result = prime * result + ((evidenceDirection == null) ? 0 : evidenceDirection.hashCode());
            result = prime * result + ((evidenceType == null) ? 0 : evidenceType.hashCode());
            result = prime * result + ((gene == null) ? 0 : gene.hashCode());
            result = prime * result + ((pubmedID == null) ? 0 : pubmedID.hashCode());
            result = prime * result + ((rating == null) ? 0 : rating.hashCode());
            result = prime * result + ((statement == null) ? 0 : statement.hashCode());
            result = prime * result + ((variant == null) ? 0 : variant.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ClinicalEvidenceSummary other = (ClinicalEvidenceSummary) obj;
            if (citation == null) {
                if (other.citation != null)
                    return false;
            } else if (!citation.equals(other.citation))
                return false;
            if (clinicalSignificance == null) {
                if (other.clinicalSignificance != null)
                    return false;
            } else if (!clinicalSignificance.equals(other.clinicalSignificance))
                return false;
            if (disease == null) {
                if (other.disease != null)
                    return false;
            } else if (!disease.equals(other.disease))
                return false;
            if (doid == null) {
                if (other.doid != null)
                    return false;
            } else if (!doid.equals(other.doid))
                return false;
            if (drugs == null) {
                if (other.drugs != null)
                    return false;
            } else if (!drugs.equals(other.drugs))
                return false;
            if (entrezID != other.entrezID)
                return false;
            if (evidenceDirection == null) {
                if (other.evidenceDirection != null)
                    return false;
            } else if (!evidenceDirection.equals(other.evidenceDirection))
                return false;
            if (evidenceType == null) {
                if (other.evidenceType != null)
                    return false;
            } else if (!evidenceType.equals(other.evidenceType))
                return false;
            if (gene == null) {
                if (other.gene != null)
                    return false;
            } else if (!gene.equals(other.gene))
                return false;
            if (pubmedID == null) {
                if (other.pubmedID != null)
                    return false;
            } else if (!pubmedID.equals(other.pubmedID))
                return false;
            if (rating == null) {
                if (other.rating != null)
                    return false;
            } else if (!rating.equals(other.rating))
                return false;
            if (statement == null) {
                if (other.statement != null)
                    return false;
            } else if (!statement.equals(other.statement))
                return false;
            if (variant == null) {
                if (other.variant != null)
                    return false;
            } else if (!variant.equals(other.variant))
                return false;
            return true;
        }
    }

    /**
     * A Gene summary
     */
    public final static class GeneSummary {
        private final String name;
        private final int entrezID;
        private final String description;

        public GeneSummary(String name, int entrezID, String description) {
            this.name = name;
            this.entrezID = entrezID;
            this.description = description;
        }

        @Override
        public String toString() {
            return "GeneSummary [name=" + name + ", entrezID=" + entrezID + ", description=" + description + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((description == null) ? 0 : description.hashCode());
            result = prime * result + entrezID;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GeneSummary other = (GeneSummary) obj;
            if (description == null) {
                if (other.description != null)
                    return false;
            } else if (!description.equals(other.description))
                return false;
            if (entrezID != other.entrezID)
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }

    /**
     * A variant group summary
     */
    public final static class VariantGroupSummary {
        private final String variantGroup;
        private final String description;

        public VariantGroupSummary(String variantGroup, String description) {
            this.variantGroup = variantGroup;
            this.description = description;
        }

        @Override
        public String toString() {
            return "VariantGroupSummary [variantGroup=" + variantGroup + ", description=" + description + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((description == null) ? 0 : description.hashCode());
            result = prime * result + ((variantGroup == null) ? 0 : variantGroup.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VariantGroupSummary other = (VariantGroupSummary) obj;
            if (description == null) {
                if (other.description != null)
                    return false;
            } else if (!description.equals(other.description))
                return false;
            if (variantGroup == null) {
                if (other.variantGroup != null)
                    return false;
            } else if (!variantGroup.equals(other.variantGroup))
                return false;
            return true;
        }
    }

    /**
     * A variant summary
     */
    public final static class VariantSummary {
        private final String gene;
        private final int entrezID;
        private final String variant;
        private final String summary;
        private final ImmutableList<String> variantGroups;
        private final String variantCivicUrl;

        public VariantSummary(String gene, int entrezID, String variant, String summary, List<String> variantGroups,
                String variantCivicUrl) {
            this.gene = gene;
            this.entrezID = entrezID;
            this.variant = variant;
            this.summary = summary;
            this.variantGroups = ImmutableList.copyOf(variantGroups);
            this.variantCivicUrl = variantCivicUrl;
        }

        /** @return HashMap representation for JSON */
        public HashMap<String, String> forJSON() {
            HashMap<String, String> result = new HashMap<>();
            result.put("gene", gene);
            result.put("entrezID", Integer.toString(entrezID));
            result.put("variant", variant);
            result.put("summary", summary);
            result.put("variantGroups", Joiner.on(", ").join(variantGroups));
            result.put("variantCivicUrl", variantCivicUrl);
            return result;
        }

        @Override
        public String toString() {
            return "VariantSummary [gene=" + gene + ", entrezID=" + entrezID + ", variant=" + variant + ", summary="
                    + summary + ", variantGroups=" + variantGroups + ", variantCivicUrl=" + variantCivicUrl + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + entrezID;
            result = prime * result + ((gene == null) ? 0 : gene.hashCode());
            result = prime * result + ((summary == null) ? 0 : summary.hashCode());
            result = prime * result + ((variant == null) ? 0 : variant.hashCode());
            result = prime * result + ((variantCivicUrl == null) ? 0 : variantCivicUrl.hashCode());
            result = prime * result + ((variantGroups == null) ? 0 : variantGroups.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VariantSummary other = (VariantSummary) obj;
            if (entrezID != other.entrezID)
                return false;
            if (gene == null) {
                if (other.gene != null)
                    return false;
            } else if (!gene.equals(other.gene))
                return false;
            if (summary == null) {
                if (other.summary != null)
                    return false;
            } else if (!summary.equals(other.summary))
                return false;
            if (variant == null) {
                if (other.variant != null)
                    return false;
            } else if (!variant.equals(other.variant))
                return false;
            if (variantCivicUrl == null) {
                if (other.variantCivicUrl != null)
                    return false;
            } else if (!variantCivicUrl.equals(other.variantCivicUrl))
                return false;
            if (variantGroups == null) {
                if (other.variantGroups != null)
                    return false;
            } else if (!variantGroups.equals(other.variantGroups))
                return false;
            return true;
        }
    }
}
