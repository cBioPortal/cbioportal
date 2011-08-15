package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;

import org.mskcc.portal.model.ProteinArrayInfo;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GetProteinArrayData {

    public static Map<String,ProteinArrayInfo> getProteinArrayInfo(ArrayList<String> geneList,
                                      String type, XDebug xdebug) throws RemoteException {
        //  Prepare query parameters
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair(CgdsProtocol.CMD, "getProteinArrayInfo"));
        if (geneList!=null) {
            list.add(new NameValuePair(CgdsProtocol.GENE_LIST, StringUtils.join(geneList, " ")));
        }
        if (type!=null) {
            list.add(new NameValuePair("protein_array_type", type));
        }
        
        NameValuePair[] data = list.toArray(new NameValuePair[0]);
        
        Map<String,ProteinArrayInfo> ret = new HashMap<String,ProteinArrayInfo>();
            
        //  Connect to remote server
        try {

            //  Connect and get response
            CgdsProtocol protocol = new CgdsProtocol(xdebug);
            String content = protocol.connect(data, xdebug);
            String[] lines = content.split("\n");

            for (int i=2; i<lines.length; i++) {
                String[] strs = lines[i].split("\t");
                ret.put(strs[1],new ProteinArrayInfo(strs[0],strs[1],strs[2],strs[3],strs[4],Boolean.parseBoolean(strs[5])));
            }
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        
        return ret;
    }
    
    /**
     * 
     * @param proteinArrayIds
     * @param caseIds
     * @param xdebug
     * @return Map &lt; arrayId, Map &lt; caseId,Abundance &gt; &gt;
     * @throws RemoteException 
     */
    public static Map<String,Map<String,Double>> getProteinArrayData(Collection<String> proteinArrayIds, Collection<String> caseIds, XDebug xdebug) throws RemoteException {
        //  Prepare query parameters
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair(CgdsProtocol.CMD, "getProteinArrayData"));
        list.add(new NameValuePair("protein_array_id", StringUtils.join(proteinArrayIds, " ")));
        if (caseIds!=null) {
            list.add(new NameValuePair(CgdsProtocol.CASE_LIST, StringUtils.join(caseIds, " ")));
        }
        
        NameValuePair[] data = list.toArray(new NameValuePair[0]);
        
        Map<String,Map<String,Double>> ret = new HashMap<String,Map<String,Double>>();
            
        //  Connect to remote server
        try {

            //  Connect and get response
            CgdsProtocol protocol = new CgdsProtocol(xdebug);
            String content = protocol.connect(data, xdebug);
            String[] lines = content.split("\n");
            
            String[] cases = lines[1].split("\t");

            for (int i=2; i<lines.length; i++) {
                String[] strs = lines[i].split("\t");
                String arrayId = strs[0];
                Map<String,Double> mapCaseAbun = new HashMap<String,Double>();
                ret.put(arrayId, mapCaseAbun);
                for (int j=1; j<strs.length; j++) {
                
                if (!strs[j].equals("NaN"))
                    mapCaseAbun.put(cases[j], Double.valueOf(strs[j]));
                }
            }
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        
        return ret;
    }
}
