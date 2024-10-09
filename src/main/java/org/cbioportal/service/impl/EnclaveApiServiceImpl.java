package org.cbioportal.service.impl;

import org.cbioportal.service.*;
import org.cbioportal.model.enclave.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnclaveApiServiceImpl implements EnclaveApiService {
    
    private static final String[] GENES = new String[] {
        "TP53",
        "KRAS",
        "TERT",
        "PIK3CA",
        "APC",
        "ARID1A",
        "KMT2D",
        "PTEN",
        "KMT2C",
        "EGFR",
        "FAT1"
    };
    
    private final List<Patient> patients;
    
    public EnclaveApiServiceImpl() {
        this.patients = new ArrayList<>();
        
        Random rnd = new Random(1337);
        int studySize = 1000;
        
        for (int i = 0; i < studySize; i++) {
            this.patients.add(genPatient(rnd));
        }
    }
    
    Patient genPatient(Random rnd) {
        Patient p = new Patient();
        p.ageAtDiagnosis = 5 + rnd.nextInt(91); // [5, 95]
        p.primaryDiagnosis = pick(
            rnd,
            "Adenocarcinoma, NOS",
            "Infiltrating lobular mixed with other types of carcinoma",
            "Adenoid cystic carcinoma",
            "Infiltrating duct mixed with other types of carcinoma",
            "Lobular carcinoma, NOS",
            "Paget disease and infiltrating duct carcinoma of breast",
            "Adenocarcinoma with mixed subtypes",
            "Pleomorphic carcinoma",
            "Infiltrating duct and lobular carcinoma",
            "Apocrine adenocarcinoma"
        );
        p.ethnicity = pick(rnd, "hispanic or latino", "not hispanic or latino", "not reported");
        p.gender = pick(rnd, "male", "female", "not reported");
        p.race = pick(
            rnd,
            "white",
            "black or african american",
            "asian",
            "american indian or alaska native",
            "native hawaiian or other pacific islander",
            "not reported"
        );
        p.vitalStatus = pick(rnd, "Alive", "Dead", "Not Reported");
        p.mutatedGenes = new HashMap<String, Integer>();
        for (String gene : GENES) {
            int count = rnd.nextInt(5);
            if (count != 0) {
                p.mutatedGenes.put(gene, count);
            }
        }
        return p;
    }
    
    <T> T pick(Random rnd, T... args) {
        List<T> items = Arrays.asList(args);
        return items.get(rnd.nextInt(items.size()));
    }

    @Override
    public CohortInfo fetchCohortInfo(FilterParams filters) {
        
        // Let's say that there is 1000 patients in the study?
        // eg. race=white, gender=male
        
        List<Patient> patientsMatchingFilters = this.patients
            .stream()
            .filter(p -> matchesFilters(p, filters))
            .collect(Collectors.toList());
        
        CohortInfo result = new CohortInfo();
        result.count = patientsMatchingFilters.size();
        return result;
    }
    
    boolean matchesFilters(Patient p, FilterParams filters) {
        return (filters.ageAtDiagnosisMin == null || p.ageAtDiagnosis >= filters.ageAtDiagnosisMin)
            && (filters.ageAtDiagnosisMax == null || p.ageAtDiagnosis <= filters.ageAtDiagnosisMax)
            && (filters.primaryDiagnosis.isEmpty() || filters.primaryDiagnosis.contains(p.primaryDiagnosis))
            && (filters.ethnicity.isEmpty() || filters.ethnicity.contains(p.ethnicity))
            && (filters.gender.isEmpty() || filters.gender.contains(p.gender))
            && (filters.race.isEmpty() || filters.race.contains(p.race))
            && (filters.vitalStatus.isEmpty() || filters.vitalStatus.contains(p.vitalStatus));
    }

    @Override
    public TopMutations fetchTopMutations(FilterParams filters, int n) {
        Map<String, Integer> mutCounts = new HashMap<>();
        
        for (Patient p : this.patients) {
            if (!matchesFilters(p, filters)) {
                continue;
            }
            
            // Add the patient's mutation counts to the result
            Map<String, Integer> toAdd = p.mutatedGenes;
            for (var entry : toAdd.entrySet()) {
                String gene = entry.getKey();
                int count = entry.getValue();

                mutCounts.compute(gene, (g, tc) -> tc == null ? 0 : (tc + count));
            }
        }
        
        TopMutations res = new TopMutations();
        res.data = mutCounts.entrySet()
            .stream()
            // map to custom class
            .map(e -> new MutationCount(e.getKey(), e.getValue()))
            // sort by count descending
            .sorted((mc1, mc2) -> mc2.count.compareTo(mc1.count))
            .collect(Collectors.toList());
        return res;
    }
}
