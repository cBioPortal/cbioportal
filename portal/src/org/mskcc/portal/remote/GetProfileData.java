package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.mskcc.portal.model.GeneticProfile;
import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Gets Profile Data for all specified genes in a specific genetic profile.
 */
public class GetProfileData {
    private ArrayList<String> warningList = new ArrayList<String>();
    private URI uri;
    private String content;

    /**
     * Gets Profile Data for all specified genes in a specific genetic profile.
     *
     * @param profile  GeneticProfile Object.
     * @param geneList ArrayList of official gene symbols.
     * @param caseIds  Space-delimited list of case IDs.
     * @return ProfileData Object.
     * @throws RemoteException Remote / Network IO Error.
     */
    public ProfileData getProfileData(GeneticProfile profile, ArrayList<String> geneList,
                                      String caseIds, XDebug xdebug) throws RemoteException {

        //  Prepare gene list
        StringBuffer geneBuf = new StringBuffer();
        for (String gene : geneList) {
            geneBuf.append(gene + " ");
        }

        //  Connect to remote server
        try {

           //  Prepare query parameters
            NameValuePair[] data = {
                    new NameValuePair(CgdsProtocol.CMD, "getProfileData"),
                    new NameValuePair("genetic_profile_id", profile.getId()),
                    new NameValuePair(CgdsProtocol.GENE_LIST, geneBuf.toString()),
                    new NameValuePair("id_type", "gene_symbol"),
                    new NameValuePair(CgdsProtocol.CASE_LIST, caseIds)
            };

            //  Connect and get response
            CgdsProtocol protocol = new CgdsProtocol(xdebug);
            content = protocol.connect(data, xdebug);
            uri = protocol.getURI();
            String lines[] = content.split("\n");

            //  Determine where real data starts
            int startData = -1;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (!line.startsWith("#")) {
                    startData = i;
                    break;
                } else {
                    if (line.startsWith("# Warning")) {
                        line = line.replaceAll("# Warning:  ", "");
                        warningList.add(line);
                    }
                }
            }

            //  Parse the profile data
            if (lines.length > 3) { // What's the point of this 3?
                // Determine matrix dimensions
                int numRows = lines.length - startData;
                String firstDataLine[] = lines[startData].split("\t");
                int numCols = firstDataLine.length;

                //  Populate n x m matrix
                String matrix[][] = new String[numRows][numCols];
                for (int i = startData; i < lines.length; i++) {
                    String parts[] = lines[i].split("\t");
                    for (int j = 0; j < parts.length; j++) {
                        matrix[i - startData][j] = parts[j];
                    }
                }
                //  Create Profile Data Object.
                ProfileData profileData = new ProfileData(profile, matrix);
                return profileData;
            }
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        return null;
    }

    /**
     * Gets warnings (if triggered).
     *
     * @return ArrayList of Warning Strings.
     */
    public ArrayList<String> getWarnings() {
        return this.warningList;
    }

    /**
     * Gets URI for connecting to remote server.
     *
     * @return URI.
     */
    public URI getURI() {
        return this.uri;
    }

    /**
     * Gets Full Content retrieved from the CGDS.
     *
     * @return Full Content.
     */
    public String getContent() {
        return this.content;
    }
}
