package org.mskcc.cgds.scripts.drug.internal;

import au.com.bytecode.opencsv.CSVReader;
import ca.drugbank.DrugType;
import ca.drugbank.Drugs;
import ca.drugbank.TargetBondType;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cgds.dao.DaoDrug;
import org.mskcc.cgds.dao.DaoDrugInteraction;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Drug;
import org.mskcc.cgds.scripts.drug.AbstractDrugInfoImporter;
import org.mskcc.cgds.scripts.drug.DrugDataResource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrugBankImporter extends AbstractDrugInfoImporter {
    Map<BigInteger, List<CanonicalGene>> geneMap = new HashMap<BigInteger, List<CanonicalGene>>();

    public DrugBankImporter(DrugDataResource dataResource) throws DaoException {
        super(dataResource);
    }

    @Override
    public void importData() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance("ca.drugbank");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
        DaoDrug daoDrug = DaoDrug.getInstance();

        Drugs drugs = (Drugs) unmarshaller.unmarshal(getDataResource().getResourceAsStream());

        int count = 0;
        for (DrugType dbDrug: drugs.getDrug()) {
            Drug drug = new Drug();

            drug.setId(dbDrug.getDrugbankId());
            drug.setResource(getDataResource().getName());
            drug.setName(dbDrug.getName());
            drug.setSynonyms(buildDelimitedText(dbDrug.getSynonyms().getSynonym()));
            drug.setDescription(dbDrug.getDescription());
            drug.setExternalReference(buildDelimitedText(dbDrug.getExternalLinks().getExternalLink()));
            List<String> groups = dbDrug.getGroups().getGroup();
            drug.setApprovedFDA(groups.contains("approved"));

            // If the drug is only nutraceutical, just ignore it (e.g. ATP and vitamin C)
            if(groups.contains("nutraceutical"))
                continue;

            // If the affected organism is not human, then skip it
            List<String> affectedOrganisms = dbDrug.getAffectedOrganisms().getAffectedOrganism();
            if(!affectedOrganisms.isEmpty() && !affectedOrganisms.contains("Humans and other mammals"))
                continue;

            List<String> atcCodes = dbDrug.getAtcCodes().getAtcCode();
            drug.setATCCode(StringUtils.join(atcCodes, ","));

            daoDrug.addDrug(drug);

            for (TargetBondType target: dbDrug.getTargets().getTarget()) {
                BigInteger partner = target.getPartner();

                List<CanonicalGene> canonicalGenes = geneMap.get(partner);
                if(canonicalGenes == null)
                    continue;

                for (CanonicalGene gene : canonicalGenes) {
                    daoDrugInteraction.addDrugInteraction(
                            drug,
                            gene,
                            DRUG_INTERACTION_TYPE,
                            getDataResource().getName(),
                            "",
                            "");
                    count++;
                }
            }
        }

        System.out.println(count + " drug interactions were imported.");
    }

    private String buildDelimitedText(List list) {
        String synonyms = "";
        for (Object o : list) {
            String synonym = o.toString();
            synonyms += synonym + ";";
        }
        if(synonyms.length() > 0)
            synonyms = synonyms.substring(0, synonyms.length()-1);
        return synonyms;
    }

    public void importDrugBankGeneList(CSVReader reader) throws DaoException, IOException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        int count = 0;
        String tokens[];
        while((tokens = reader.readNext()) != null) {
            if(tokens[0].startsWith("ID"))
                continue;

            assert(tokens.length > 1);

            BigInteger drugBankId = new BigInteger(tokens[0]);
            String geneName = tokens[2].trim();

            // Try to get the gene with this name
            List<CanonicalGene> genes = daoGeneOptimized.guessGene(geneName);
            geneMap.put(drugBankId, genes);
            if(!genes.isEmpty())
                count++;
        }

        System.out.println("Imported " + count + "/" + geneMap.size() + " genes");
    }

    public Map<BigInteger, List<CanonicalGene>> getGeneMap() {
        return geneMap;
    }

    public void setGeneMap(Map<BigInteger, List<CanonicalGene>> geneMap) {
        this.geneMap = geneMap;
    }
}