/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mskcc.cbio.portal.model.DBGeneSet;
import org.springframework.stereotype.Service;

/**
 *
 * @author abeshoua
 */
@Service
public class GeneSetService {
    private Map<Integer, DBGeneSet> geneSetMap;
    public GeneSetService() {
       geneSetMap = new HashMap<>();
       try {
            List<DBGeneSet> geneSetList = this.loadGeneSets();
            for (DBGeneSet gs: geneSetList) {
                geneSetMap.put(gs.id, gs);
            }
       } catch (IOException e) {
           //TODO?
       }
    }
       
    private List<DBGeneSet> loadGeneSets() throws IOException {
       InputStream in = this.getClass().getResourceAsStream("/gene_sets.txt");
       ArrayList<DBGeneSet> geneSetList = new ArrayList<DBGeneSet>();
       DBGeneSet userDefined = new DBGeneSet(0,"User-defined List","");
       //User-defined goes first
       geneSetList.add(userDefined);
       BufferedReader reader = new BufferedReader(new InputStreamReader(in));
       String line = reader.readLine();
       int id=0;
       while (line != null) {
           id++;
           line = line.trim();
           String parts[] = line.split("=");
           DBGeneSet set = new DBGeneSet();
           String genes[] = parts[1].split("\\s");
           set.id = id;
           set.name = parts[0]+" ("+genes.length+" genes)";
           set.gene_list = parts[1];
           geneSetList.add(set);
           line = reader.readLine();
       }
       in.close();
       return geneSetList;
    }
    
    public List<DBGeneSet> getAll(boolean omit_lists) {
        ArrayList<DBGeneSet> ret = new ArrayList<>();
        if (!omit_lists) {
            ret.addAll(geneSetMap.values());
        } else {
            for (DBGeneSet gs: geneSetMap.values()) {
                ret.add(gs.stripList());
            }
        }
        return ret;
    }
    public List<DBGeneSet> byInternalId(List<Integer> ids, boolean omit_lists) {
        ArrayList<DBGeneSet> ret = new ArrayList<>();
        for (Integer i: ids) {
            if (geneSetMap.containsKey(i)) {
                DBGeneSet gs = geneSetMap.get(i);
                if (omit_lists) {
                    ret.add(gs.stripList());
                } else {
                    ret.add(gs);
                }
            }
        }
        return ret;
    }
}
