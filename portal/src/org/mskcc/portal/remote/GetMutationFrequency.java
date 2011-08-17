package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.mskcc.portal.model.GeneWithScore;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Gets Mutation Frequencies.
 */
public class GetMutationFrequency {

    /**
     * Gets Mutation Frequency.
     *
     * @throws java.rmi.RemoteException Remote / Network IO Error.
     */
    public static ArrayList <GeneWithScore> getMutationFrequency(XDebug xdebug) throws RemoteException {
        String content;
        ArrayList <GeneWithScore> geneList = new ArrayList <GeneWithScore>();
        //  Connect to remote server
        try {
            //  Prepare query parameters
            NameValuePair[] data = {
                    new NameValuePair(CgdsProtocol.CMD, "getMutationFrequency"),
            };

            //  Connect and get response
            CgdsProtocol protocol = new CgdsProtocol(xdebug);
            content = protocol.connect(data, xdebug);
            URI uri = protocol.getURI();
            String lines[] = content.split("\n");

            //  Determine where real data starts
            int startData = -1;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (!line.startsWith("#")) {
                    startData = i;
                    break;
                }
            }

            //  Parse the profile data
            if (lines.length > 3) {
                for (int i = startData+1; i < lines.length; i++) {
                    String parts[] = lines[i].split("\t");
                    GeneWithScore geneWithScore = new GeneWithScore();
                    geneWithScore.setGene(parts[1]);
                    geneWithScore.setEntrezGeneId(Integer.parseInt(parts[0]));
                    geneWithScore.setScore(Double.parseDouble(parts[2]));
                    geneList.add(geneWithScore);
                }
                return geneList;
            }
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        return null;
    }
}