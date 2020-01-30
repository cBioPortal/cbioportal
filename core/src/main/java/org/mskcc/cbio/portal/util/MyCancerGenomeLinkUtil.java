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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang.StringEscapeUtils;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

/**
 *
 * @author jgao
 */
public final class MyCancerGenomeLinkUtil {

    private MyCancerGenomeLinkUtil() {}

    private static final Map<String, Map<String, Map<String, String>>> LINK_MAP = new HashMap<String, Map<String, Map<String, String>>>();

    static {
        if (GlobalProperties.showMyCancerGenomeUrl()) {
            setMyCancerGenomeLinkFromLocal();
        }
    }

    /**
     *
     * @param gene
     * @param alteration
     * @return
     */
    public static List<String> getMyCancerGenomeLinks(
        String gene,
        String alteration,
        boolean includeGeneralMutation
    )
        throws IOException {
        List<String> list = new ArrayList<String>();

        boolean showMyCancerGenomeUrl = GlobalProperties.showMyCancerGenomeUrl();
        if (showMyCancerGenomeUrl) {
            Map<String, Map<String, String>> mapVariantCancerLink = LINK_MAP.get(
                gene
            );
            if (mapVariantCancerLink != null) {
                Map<String, String> mapCancerLink = mapVariantCancerLink.get(
                    alteration
                );
                if (mapCancerLink != null) {
                    list.addAll(mapCancerLink.values());
                }

                if (includeGeneralMutation) {
                    mapCancerLink = mapVariantCancerLink.get("mutation");
                    if (mapCancerLink != null) {
                        list.addAll(mapCancerLink.values());
                    }
                }
            }
        }
        return list;
    }

    private static final String MYCANCERGENOME_FILE = "/mycancergenome.txt";

    private static void setMyCancerGenomeLinkFromLocal() {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    MyCancerGenomeLinkUtil.class.getResourceAsStream(
                            MYCANCERGENOME_FILE
                        )
                )
            )
        ) {
            String line;
            while ((line = in.readLine()) != null && line.startsWith("#")) {}

            for (; line != null; line = in.readLine()) {
                String[] parts = line.trim().split("\t", -1);
                if (parts.length < 4) {
                    continue;
                }

                CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(
                    parts[0]
                );
                if (gene != null) {
                    String hugo = gene.getHugoGeneSymbolAllCaps();
                    Map<String, Map<String, String>> mapVariantCancerLink = LINK_MAP.get(
                        hugo
                    );
                    if (mapVariantCancerLink == null) {
                        mapVariantCancerLink =
                            new HashMap<String, Map<String, String>>();
                        LINK_MAP.put(hugo, mapVariantCancerLink);
                    }

                    Map<String, String> mapCancerLink = mapVariantCancerLink.get(
                        parts[1]
                    );
                    if (mapCancerLink == null) {
                        mapCancerLink = new HashMap<String, String>();
                        mapVariantCancerLink.put(parts[1], mapCancerLink);
                    }

                    mapCancerLink.put(parts[2], parts[3]);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
