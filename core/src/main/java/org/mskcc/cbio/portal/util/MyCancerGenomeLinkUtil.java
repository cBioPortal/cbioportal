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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * @param mutation
     * @return Map<Cancer, URL>
     */
    public static Map<String, String> getMayCancerGenomeLinks(String gene, String alteration) throws IOException {
        if (linkMap==null) {
            linkMap = getMyCancerGenomeLinks();
        }
        
        Map<String, Map<String, String>> mapVariantCancerLink =  linkMap.get(gene);
        if (mapVariantCancerLink==null) {
            return Collections.emptyMap();
        }
        
        Map<String, String> mapCancerLink = mapVariantCancerLink.get(alteration);
        if (mapCancerLink == null) {
            return Collections.emptyMap();
        }
        
        return Collections.unmodifiableMap(mapCancerLink);
    }
    
    /**
     * 
     * @return Map<Gene, Map<Varaint, Map<Cancer, URL>>>
     * @throws IOException 
     */
    private static Map<String,Map<String, Map<String, String>>> getMyCancerGenomeLinks() throws IOException {
        Map<String,Map<String, Map<String, String>>> mapGeneVariantCancerLink
                = new HashMap<String,Map<String, Map<String, String>>>();
        
        URL url = new URL(GlobalProperties.getMyCancerGenomeUrl()+"/sitemap");
        InputStream is = url.openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        
        String line;
        while ((line = in.readLine()) != null
                && !line.contains("<a id=\"variants\">")) {}
        
        if (line==null) {
            System.err.println("MyCancerGenome format change");
            return mapGeneVariantCancerLink;
        }
        
        line = in.readLine();
        
        Pattern pM = Pattern.compile("([A-Z0-9\\-]+) +c\\. *[^ ]+ +\\((.+)\\) Mutations? in (.+)",Pattern.CASE_INSENSITIVE);
        Pattern pM2 = Pattern.compile("([A-Z0-9\\-]+) ?\\(([A-Z0-9\\-]+)\\) c\\. *[^ ]+ \\((.+)\\) Mutations? in (.+)",Pattern.CASE_INSENSITIVE);
        Pattern pM3 = Pattern.compile("([A-Z0-9\\-]+) Mutations? in (.+)",Pattern.CASE_INSENSITIVE);
        Pattern pA = Pattern.compile("([A-Z0-9\\-]+) Amplifications? in (.+)",Pattern.CASE_INSENSITIVE);
        Pattern pA2 = Pattern.compile("([A-Z0-9\\-]+) \\(([A-Z0-9\\-]+)\\) Amplifications? in (.+)",Pattern.CASE_INSENSITIVE);
        
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        
        Map<String,String> links = getLinks(line);
        for (Map.Entry<String,String> entry : links.entrySet()) {
            String href = entry.getKey();
            String text = entry.getValue();
            String geneSymbol = null;
            String mutation = null;
            String cancer = null;
            Matcher m = pM.matcher(text);
            if (m.matches()) {
                CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                if (gene == null) {
                    System.err.println("Could not recoganize gene: "+text);
                    continue;
                }
                geneSymbol = gene.getHugoGeneSymbolAllCaps();
                mutation = m.group(2);
                cancer = m.group(3);
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
                    geneSymbol = gene.getHugoGeneSymbolAllCaps();
                    mutation = m.group(3);
                    cancer = m.group(4);
                } else {
                    m = pM3.matcher(text);
                    if (m.matches()) {
                        CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                        if (gene == null) {
                            System.err.println("Could not recoganize gene: "+text);
                            continue;
                        }
                        geneSymbol = gene.getHugoGeneSymbolAllCaps();
                        mutation = "mutation";
                        cancer = m.group(2);
                    } else {
                        m = pA.matcher(text);
                        if (m.matches()) {
                            CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(m.group(1));
                            if (gene == null) {
                                System.err.println("Could not recoganize gene: "+text);
                                continue;
                            }
                            geneSymbol = gene.getHugoGeneSymbolAllCaps();
                            mutation = "amplification";
                            cancer = m.group(2);
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
                                geneSymbol = gene.getHugoGeneSymbolAllCaps();
                                mutation = "amplification";
                                cancer = m.group(3);
                            } else {


                                System.out.println(text);
                            }
                        }
                    }
                    
                }
            }
            
            if (geneSymbol!=null) {
                Map<String, Map<String, String>> mapVariantCancerLink = mapGeneVariantCancerLink.get(geneSymbol);
                if (mapVariantCancerLink==null) {
                    mapVariantCancerLink = new HashMap<String, Map<String, String>>();
                    mapGeneVariantCancerLink.put(geneSymbol, mapVariantCancerLink);
                }
                
                Map<String, String> mapCancerLink = mapVariantCancerLink.get(mutation);
                if (mapCancerLink==null) {
                    mapCancerLink = new HashMap<String,String>();
                    mapVariantCancerLink.put(mutation, mapCancerLink);
                }
                
                mapCancerLink.put(cancer, href);
            }
        }
        
        return mapGeneVariantCancerLink;
    }
    
    private static Map<String,String> getLinks(String str) {
        Map<String,String> links = new HashMap<String,String>();
        Pattern p = Pattern.compile("<a href=\"([^\"]+)\">([^<]+)</a>");
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

