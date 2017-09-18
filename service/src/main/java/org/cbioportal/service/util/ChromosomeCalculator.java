package org.cbioportal.service.util;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChromosomeCalculator {

    private String getChromosome(String cytoband) {

        if (cytoband == null) {
            return null;
        }

        cytoband = cytoband.toUpperCase();
        if (cytoband.startsWith("X")) {
            return "X";
        } else if (cytoband.startsWith("Y")) {
            return "Y";
        }

        Pattern p = Pattern.compile("([0-9]+).*");
        Matcher m = p.matcher(cytoband);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    public void setChromosome(Gene gene) {

        if (gene != null) {
            gene.setChromosome(getChromosome(gene.getCytoband()));
        }
    }
    
    public void renameChromosome(Mutation mutation) {
        if (mutation != null) {
            if (mutation.getChromosome().equals("23")) {
                mutation.setChromosome("X");
            } else if (mutation.getChromosome().equals("24")) {
                mutation.setChromosome("Y");
            }
        }
    }
    
}
