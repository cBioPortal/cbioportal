package org.cbioportal.model;

import org.cbioportal.model.summary.GeneSummary;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gene extends GeneSummary {

    private List<String> aliases;

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getChromosome() {

        if (getCytoband() == null) {
            return null;
        }
        if (getCytoband().toUpperCase().startsWith("X")) {
            return "X";
        }
        if (getCytoband().toUpperCase().startsWith("Y")) {
            return "Y";
        }

        Pattern p = Pattern.compile("([0-9]+).*");
        Matcher m = p.matcher(getCytoband());
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }
}