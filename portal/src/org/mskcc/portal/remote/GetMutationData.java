package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.mskcc.portal.model.GeneticProfile;
import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.model.ExtendedMutation;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Gets Mutation Data for all specified genes in a specific genetic profile.
 */
public class GetMutationData {
    private ArrayList<String> warningList = new ArrayList<String>();
    private URI uri;
    private String content;

    /**
     * Gets MutationData Data for all specified genes in a specific genetic profile.
     *
     * @param profile  GeneticProfile Object.
     * @param geneList ArrayList of official gene symbols.
     * @param caseIds  Space-delimited list of case IDs.
     * @return ProfileData Object.
     * @throws java.rmi.RemoteException Remote / Network IO Error.
     */
    public ArrayList <ExtendedMutation> getMutationData(GeneticProfile profile,
                ArrayList<String> geneList, String caseIds, XDebug xdebug) throws RemoteException {

        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        //  Prepare gene list
        StringBuffer geneBuf = new StringBuffer();
        for (String gene : geneList) {
            geneBuf.append(gene + " ");
        }

        //  Connect to remote server
        try {
            //  Prepare query parameters
            NameValuePair[] data = {
                    new NameValuePair(CgdsProtocol.CMD, "getMutationData"),
                    new NameValuePair("genetic_profile_id", profile.getId()),
                    new NameValuePair("gene_list", geneBuf.toString()),
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
            if (lines.length > 3) {
                for (int i = startData+1; i < lines.length; i++) {
                    String parts[] = lines[i].split("\t");
                    ExtendedMutation mutation = new ExtendedMutation();

                    String entrezGeneId = parts[0];
                    String geneSymbol = parts[1];
                    String caseId = parts[2];
                    String center = parts[3];
                    String mutationStatus = parts[4];
                    String mutationType = parts[5];
                    String validationStatus = parts[6];
                    String aminoAcidChange = parts[7];
                    String functionalImpactScore = parts[8].trim();
                    String xvarLink = parts[9].trim();
                    String xvarLinkPdb = parts[10].trim();
                    String xvarLinkMsa = parts[11].trim();
                    String chr = parts[12].trim();
                    String startPosition = parts[13].trim();
                    String endPosition = parts[14].trim();

                    mutation.setGeneSymbol(geneSymbol);
                    mutation.setEntrezGeneId(Integer.parseInt(entrezGeneId));
                    mutation.setCaseId(caseId);
                    mutation.setCenter(center);
                    mutation.setMutationStatus(mutationStatus);
                    mutation.setMutationType(mutationType);
                    mutation.setValidationStatus(validationStatus);
                    mutation.setAminoAcidChange(aminoAcidChange);
                    mutation.setFunctionalImpactScore(functionalImpactScore);
                    mutation.setLinkXVar(xvarLink);
                    mutation.setLinkPdb(xvarLinkPdb);
                    mutation.setLinkMsa(xvarLinkMsa);
                    mutation.setChr(chr);
                    try {
                        mutation.setStartPosition(Long.parseLong(startPosition));
                        mutation.setEndPosition(Long.parseLong(endPosition));
                    } catch (NumberFormatException e) {
                    }
                    mutationList.add(mutation);
                }
                return mutationList;
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