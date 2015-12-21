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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

/**
 *
 * @author jgao
 */
public final class MyCancerGenomeLinkUtil {
    private MyCancerGenomeLinkUtil() {}
    private static Map<String,Map<String, Map<String, String>>> linkMap = null;


    /**
     *
     * @param gene
     * @param alteration
     * @return
     */
    public static List<String> getMyCancerGenomeLinks(String gene, String alteration, boolean includeGeneralMutation) throws IOException {
        List<String> list = new ArrayList<String>();

        String mcgUrl = GlobalProperties.getMyCancerGenomeUrl();
        if(mcgUrl != null && !mcgUrl.isEmpty()) {
            if (linkMap==null) {
                linkMap = getMyCancerGenomeLinks();
            }


            Map<String, Map<String, String>> mapVariantCancerLink = linkMap.get(gene);
            if (mapVariantCancerLink != null) {
                Map<String, String> mapCancerLink = mapVariantCancerLink.get(alteration);
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
    
    /**
     * 
     * @return Map<Cancer, URL>
     * @throws IOException 
     */
    private static Map<String, String> getCancerTypeLinks() throws IOException {
        Map<String, String> mapCancerLink = new HashMap<String, String>();
        
        URL url = new URL(GlobalProperties.getMyCancerGenomeUrl()+"/sitemap");
        InputStream is = url.openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = in.readLine()) != null
                && !line.contains("<a id=\"cancer_types\">")) {}

        if (line==null) {
            System.err.println("MyCancerGenome format change: no cancer_types in site map");
            return mapCancerLink;
        }
        
        line = in.readLine();
        
        return getLinks(line, "<a href=\"([^\"]+)\">([^<]+)</a>");
    }

    /**
     *
     * @return Map<Gene, Map<Varaint, Map<Cancer, URL>>>
     * @throws IOException
     */
    private static Map<String,Map<String, Map<String, String>>> getMyCancerGenomeLinks() throws IOException {
        Map<String,Map<String, Map<String, String>>> mapGeneVariantCancerLink
                = new HashMap<String,Map<String, Map<String, String>>>();

        Map<String, String> mapCancerTypeLink = getCancerTypeLinks();
        
        for (Map.Entry<String,String> entry : mapCancerTypeLink.entrySet()) {
            getVariantLinksForCancerType(mapGeneVariantCancerLink, entry.getValue(), GlobalProperties.getMyCancerGenomeUrl()+entry.getKey());
        }
        
        return mapGeneVariantCancerLink;
    }
        
    private static void getVariantLinksForCancerType(Map<String,Map<String, Map<String, String>>> mapGeneVariantCancerLink,
            String cancer, String cancerink) throws IOException {
        
        URL url = new URL(cancerink);
        
        InputStream is = url.openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        
        StringBuilder sb = new StringBuilder();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            sb.append(line);
        }
        
        Map<String, String> links = getLinks(sb.toString(), "<a [^>]*href=\"("+cancerink+"[^\"]+)\"[^>]*>([^<]+)</a>");

        Pattern pM = Pattern.compile("([A-Z0-9\\-]+) +c\\. *[^ ]+ +\\((.+)\\)",Pattern.CASE_INSENSITIVE);
        Pattern pM2 = Pattern.compile("([A-Z0-9\\-]+) ?\\(([A-Z0-9\\-]+)\\) c\\. *[^ ]+ \\((.+)\\)",Pattern.CASE_INSENSITIVE);
        Pattern pM3 = Pattern.compile("([A-Z0-9\\-]+) Mutations?",Pattern.CASE_INSENSITIVE);
        Pattern pA = Pattern.compile("([A-Z0-9\\-]+) Amplifications?",Pattern.CASE_INSENSITIVE);
        Pattern pA2 = Pattern.compile("([A-Z0-9\\-]+) \\(([A-Z0-9\\-]+)\\) Amplifications?",Pattern.CASE_INSENSITIVE);
        Pattern pF = Pattern.compile("([A-Z0-9\\-]+) Fusions?",Pattern.CASE_INSENSITIVE);
        Pattern pF2 = Pattern.compile("([A-Z0-9\\-]+) \\(([A-Z0-9\\-]+)\\) Fusions?",Pattern.CASE_INSENSITIVE);

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        for (Map.Entry<String,String> entry : links.entrySet()) {
            String href = entry.getKey();
            String text = StringEscapeUtils.unescapeHtml(entry.getValue());
            Set<String> geneSymbols = new HashSet<String>();
            String mutation = null;
            Matcher m = pM.matcher(text);
            if (m.matches()) {
                CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                if (gene == null) {
                    System.err.println("Could not recoganize gene: "+text);
                    continue;
                }
                geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                mutation = m.group(2);
            } else {
                m = pM2.matcher(text);
                if (m.matches()) {
                    CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                    if (gene == null) {
                        gene = daoGeneOptimized.getNonAmbiguousGene(m.group(2));
                    }
                    if (gene == null) {
                        System.err.println("Could not recoganize gene: "+text);
                        continue;
                    }
                    geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                    mutation = m.group(3);
                } else {
                    m = pM3.matcher(text);
                    if (m.matches()) {
                        CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                        if (gene == null) {
                            System.err.println("Could not recoganize gene: "+text);
                            continue;
                        }
                        geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                        mutation = "mutation";
                    } else {
                        m = pA.matcher(text);
                        if (m.matches()) {
                            CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                            if (gene == null) {
                                System.err.println("Could not recoganize gene: "+text);
                                continue;
                            }
                            geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                            mutation = "amplification";
                        } else {
                            m = pA2.matcher(text);
                            if (m.matches()) {
                                CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                                if (gene == null) {
                                    gene = daoGeneOptimized.getNonAmbiguousGene(m.group(2));
                                }
                                if (gene == null) {
                                    System.err.println("Could not recoganize gene: "+text);
                                    continue;
                                }
                                geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                                mutation = "amplification";
                            } else {
                                m = pF.matcher(text);
                                if (m.matches()) {
                                    CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                                    if (gene!=null) {
                                        geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                                    }

                                    if (m.group(1).contains("-")) {
                                        for (String part : m.group(1).split("-")) {
                                            gene = daoGeneOptimized.getNonAmbiguousGene(part);
                                            if (gene!=null) {
                                                geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                                            }
                                        }
                                    }
                                    if (geneSymbols.isEmpty()) {
                                        System.err.println("Could not recoganize gene: "+text);
                                        continue;
                                    }
                                    mutation = "fusion";
                                } else {
                                    m = pF.matcher(text);
                                    if (m.matches()) {
                                        CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                                        if (gene!=null) {
                                            geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                                        }

                                        if (m.group(1).contains("-")) {
                                            for (String part : m.group(1).split("-")) {
                                                gene = daoGeneOptimized.getNonAmbiguousGene(part);
                                                if (gene!=null) {
                                                    geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                                                }
                                            }
                                        }

                                        gene = daoGeneOptimized.getNonAmbiguousGene(m.group(2));
                                        if (gene!=null) {
                                            geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                                        }

                                        if (m.group(1).contains("-")) {
                                            for (String part : m.group(2).split("-")) {
                                                gene = daoGeneOptimized.getNonAmbiguousGene(part);
                                                if (gene!=null) {
                                                    geneSymbols.add(gene.getHugoGeneSymbolAllCaps());
                                                }
                                            }
                                        }

                                        if (geneSymbols.isEmpty()) {
                                            System.err.println("Could not recoganize gene: "+text);
                                            continue;
                                        }
                                        mutation = "fusion";
                                    } else {


                                        System.out.println(text);
                                    }
                                }
                            }
                        }
                    }

                }
            }

            if (!geneSymbols.isEmpty()) {
                for (String geneSymbol : geneSymbols) {
                    Map<String, Map<String, String>> mapVariantCancerLink = mapGeneVariantCancerLink.get(geneSymbol);
                    if (mapVariantCancerLink==null) {
                        mapVariantCancerLink = new HashMap<String, Map<String, String>>();
                        mapGeneVariantCancerLink.put(geneSymbol, mapVariantCancerLink);
                    }

                    Map<String, String> mapCancerLink = mapVariantCancerLink.get(mutation);
                    if (mapCancerLink==null) {
                        mapCancerLink = new TreeMap<String,String>();
                        mapVariantCancerLink.put(mutation, mapCancerLink);
                    }
                    mapCancerLink.put(cancer, "<a \"target=\"_blank\" href=\">"+ href +"\">"+ entry.getValue() + " in " + cancer +"</a>");
                }

            } else {
                System.out.println("Cannot process "+text+" "+href);
            }
        }
    }

    private static Map<String,String> getLinks(String str, String pattern) {
        Map<String,String> links = new HashMap<String,String>();
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        while (m.find()) {
            String href = m.group(1);
            String text = m.group(2);
            links.put(href, text);
        }
        return links;
    }

    public static void main(String[] args) throws IOException {
        getMyCancerGenomeLinks();
    }
}

