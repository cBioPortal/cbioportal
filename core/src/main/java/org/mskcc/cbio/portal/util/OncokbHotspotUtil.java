/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jgao
 */
public final class OncokbHotspotUtil {

    private OncokbHotspotUtil() {}

    private static Map<String, Map<String, Integer>> linkMap = null;

    /**
     *
     * @param gene
     * @param alteration
     * @return
     */
    public static Boolean getOncokbHotspot(String gene, String alteration)
        throws IOException {
        if (linkMap == null) {
            linkMap = getOncokbHotspot();
        }

        Map<String, Integer> geneInfo = linkMap.get(gene);
        if (geneInfo != null) {
            Pattern p = Pattern.compile("([A-Z][0-9]+)([A-Z])");
            Matcher m = p.matcher(alteration);
            if (m.matches()) {
                String codon = m.group(1);
                if (geneInfo.containsKey(codon)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return false;
    }

    /**
     *
     * @return Map<Gene, Map<Varaint, number exists in samples>>
     * @throws IOException
     */
    private static Map<String, Map<String, Integer>> getOncokbHotspot()
        throws IOException {
        Map<String, Map<String, Integer>> hotspots = new HashMap<>();

        InputStream inputStream =
            OncokbHotspotUtil.class.getClassLoader()
                .getResourceAsStream("chang_hotspot.txt");
        BufferedReader br = new BufferedReader(
            new InputStreamReader(inputStream)
        );
        String line;
        while ((line = br.readLine()) != null) {
            String[] items = line.split("\t");
            if (items.length > 1 && items.length < 4) {
                String hugoSymbol = items[0];
                String codon = items[1];
                //                String[] variants = items.length>2?items[2].split("\\|"):null;

                if (codon != null) {
                    if (!hotspots.containsKey(hugoSymbol)) {
                        hotspots.put(
                            hugoSymbol,
                            new HashMap<String, Integer>()
                        );
                    }
                    //                    if(variants != null && variants.length > 0) {
                    //                        for(String aa : variants) {
                    //                            String [] datum = aa.split(":");
                    //                            if(datum.length == 2) {
                    //                                hotspots.get(hugoSymbol).put(codon+datum[0], Integer.parseInt(datum[1]));
                    //                            }
                    //                        }
                    //                    }else{
                    hotspots.get(hugoSymbol).put(codon, 1000000);
                    //                    }
                }
            }
        }

        return hotspots;
    }
}
