package org.cbioportal.legacy.service.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

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
}
