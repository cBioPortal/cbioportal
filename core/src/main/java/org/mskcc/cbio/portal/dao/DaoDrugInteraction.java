/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Drug;
import org.mskcc.cbio.portal.model.DrugInteraction;

public class DaoDrugInteraction {
    private static DaoDrugInteraction daoDrugInteraction;
    private static final String NA = "NA";

    private static final Log log = LogFactory.getLog(DaoDrugInteraction.class);

    private DaoDrugInteraction() {
    }

    public static DaoDrugInteraction getInstance() throws DaoException {
        if (daoDrugInteraction == null) {
            daoDrugInteraction = new DaoDrugInteraction();
        }

        return daoDrugInteraction;
    }

    public int addDrugInteraction(Drug drug,
                                  CanonicalGene targetGene,
                                  String interactionType,
                                  String dataSource,
                                  String experimentTypes,
                                  String pmids) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        if (interactionType == null) {
            throw new IllegalArgumentException ("Drug interaction type cannot be null");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException ("Data Source cannot be null");
        }
        if (experimentTypes == null) {
            experimentTypes = NA;
        }
        if (pmids == null) {
            pmids = NA;
        }

        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                MySQLbulkLoader.getMySQLbulkLoader("drug_interaction").insertRecord(
                        drug.getId(),
                        Long.toString(targetGene.getEntrezGeneId()),
                        interactionType,
                        dataSource,
                        experimentTypes,
                        pmids);

                return 1;
            } else {
                con = JdbcUtil.getDbConnection(DaoDrugInteraction.class);
                pstmt = con.prepareStatement
                        ("INSERT INTO drug_interaction (`DRUG`,`TARGET`, `INTERACTION_TYPE`," +
                                "`DATA_SOURCE`, `EXPERIMENT_TYPES`, `PMIDS`)"
                                + "VALUES (?,?,?,?,?,?)");
                pstmt.setString(1, drug.getId());
                pstmt.setLong(2, targetGene.getEntrezGeneId());
                pstmt.setString(3, interactionType);
                pstmt.setString(4, dataSource);
                pstmt.setString(5, experimentTypes);
                pstmt.setString(6, pmids);

                return pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrugInteraction.class, con, pstmt, rs);
        }
    }

    public ArrayList<DrugInteraction> getInteractions(long entrezGeneId) throws DaoException {
        return getInteractions(Collections.singleton(entrezGeneId));
    }

    public ArrayList<DrugInteraction> getInteractions(CanonicalGene gene) throws DaoException {
        return getInteractions(Collections.singleton(gene));
    }

    public ArrayList<DrugInteraction> getInteractions(Collection<?> genes) throws DaoException {
        ArrayList<DrugInteraction> interactionList = new ArrayList<DrugInteraction>();
        if (genes.isEmpty())
            return interactionList;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoDrugInteraction.class);
            Set<Long> entrezGeneIds = new HashSet<Long>();

            for (Object gene : genes) {
                if(gene instanceof CanonicalGene)
                    entrezGeneIds.add(((CanonicalGene) gene).getEntrezGeneId());
                else if(gene instanceof Long)
                    entrezGeneIds.add((Long) gene);
                else
                    entrezGeneIds.add(Long.parseLong(gene.toString()));
            }

            String idStr = "(" + StringUtils.join(entrezGeneIds, ",") + ")";

            pstmt = con.prepareStatement("SELECT * FROM drug_interaction WHERE TARGET IN " + idStr);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DrugInteraction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }

            return interactionList;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrugInteraction.class, con, pstmt, rs);
        }
    }
    
    public Map<Long, List<String>> getDrugs(Set<Long> entrezGeneIds, boolean fdaOnly, boolean cancerSpecific) throws DaoException {
        if (entrezGeneIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoDrugInteraction.class);
            
            String sql;
            if (fdaOnly || cancerSpecific) {
                sql = "SELECT DRUG,TARGET FROM drug_interaction, drug"
                    + " WHERE TARGET IN (" + StringUtils.join(entrezGeneIds, ",") + ")"
                    + " AND drug_interaction.DRUG=drug.DRUG_ID";
                if (fdaOnly) {
                    sql += " AND DRUG_APPROVED=1";
                }
                 
                if (cancerSpecific) {
                    sql += " AND DRUG_CANCERDRUG=1";
                }
            } else {
                sql = "SELECT DRUG,TARGET FROM drug_interaction"
                    + " WHERE TARGET IN (" 
                    + StringUtils.join(entrezGeneIds, ",") + ")";
            }

            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            Map<Long, List<String>> map = new HashMap<Long, List<String>>();
            while (rs.next()) {
                long entrez = rs.getLong("TARGET");
                List<String> drugs = map.get(entrez);
                if (drugs==null) {
                    drugs = new ArrayList<String>();
                    map.put(entrez, drugs);
                }
                
                drugs.add(rs.getString("DRUG"));
            }

            return map;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrugInteraction.class, con, pstmt, rs);
        }
    }

    public ArrayList<DrugInteraction> getTargets(Drug drug) throws DaoException {
        return getTargets(Collections.singleton(drug));
    }

    public ArrayList<DrugInteraction> getTargets(Collection<Drug> drugs) throws DaoException {
        ArrayList<DrugInteraction> interactionList = new ArrayList<DrugInteraction>();
        if (drugs.isEmpty())
            return interactionList;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoDrugInteraction.class);
            Set<String> drugIDs = new HashSet<String>();
            for (Drug drug : drugs)
                drugIDs.add("'" + drug.getId() + "'");

            String idStr = "(" + StringUtils.join(drugIDs, ",") + ")";

            pstmt = con.prepareStatement("SELECT * FROM drug_interaction WHERE DRUG IN " + idStr);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DrugInteraction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }

            return interactionList;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrugInteraction.class, con, pstmt, rs);
        }
    }


    public ArrayList<DrugInteraction> getAllInteractions() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<DrugInteraction> interactionList = new ArrayList <DrugInteraction>();

        try {
            con = JdbcUtil.getDbConnection(DaoDrugInteraction.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug_interaction");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DrugInteraction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }

            return interactionList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrugInteraction.class, con, pstmt, rs);
        }
    }

    private DrugInteraction extractInteraction(ResultSet rs) throws SQLException {
        DrugInteraction interaction = new DrugInteraction();
        interaction.setDrug(rs.getString("DRUG"));
        interaction.setTargetGene(rs.getLong("TARGET"));
        interaction.setInteractionType(rs.getString("INTERACTION_TYPE"));
        interaction.setDataSource(rs.getString("DATA_SOURCE"));
        interaction.setExperimentTypes(rs.getString("EXPERIMENT_TYPES"));
        interaction.setPubMedIDs(rs.getString("PMIDS"));
        return interaction;
    }

    /**
     * Gets the Number of Interaction Records in the Database.
     *
     * @return number of gene records.
     */
    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoDrugInteraction.class);
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM drug_interaction");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrugInteraction.class, con, pstmt, rs);
        }
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoDrugInteraction.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE drug_interaction");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrugInteraction.class, con, pstmt, rs);
        }
    }
    
    // Temporary way of handling cases such as akt inhibitor for pten loss
    private static final String DRUG_TARGET_FILE = "/drug_target_annotation.txt";
    private static  Map<Long,Map<String,Set<Long>>> drugTargetAnnotation = null; // map <entrez of gene of event, map <event, target genes> >
    
    private static synchronized Map<Long,Map<String,Set<Long>>> getDrugTargetAnnotation() {
        if (drugTargetAnnotation==null) {
            drugTargetAnnotation = new HashMap<Long,Map<String,Set<Long>>>();
            try {
                DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(DaoDrugInteraction.class.getResourceAsStream(DRUG_TARGET_FILE)));
                for (String line=in.readLine(); line!=null; line=in.readLine()) {
                    if (line.startsWith("#")) {
                        continue;
                    }

                    String[] parts = line.split("\t");
                    String[] genesOfEvents = parts[0].split(",");
                    String[] events = parts[1].split(",");
                    String[] targetGenes = parts[2].split(",");
                    Set<Long> targetEntrez = new HashSet<Long>(targetGenes.length);
                    for (String target : targetGenes) {
                        CanonicalGene gene = daoGeneOptimized.getGene(target);
                        if(gene == null)
                            log.warn("Could not find gene: " + target);
                        else
                            targetEntrez.add(gene.getEntrezGeneId());
                    }

                    for (String gene : genesOfEvents) {
                        long entrez = daoGeneOptimized.getGene(gene).getEntrezGeneId();
                        Map<String,Set<Long>> mapEventTargets = drugTargetAnnotation.get(entrez);
                        if (mapEventTargets==null) {
                            mapEventTargets = new HashMap<String,Set<Long>>();
                            drugTargetAnnotation.put(entrez, mapEventTargets);
                        }

                        for (String event : events) {
                            mapEventTargets.put(event, targetEntrez);
                        }
                    }
                }
                in.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return drugTargetAnnotation;
    }
    
    public Set<Long> getMoreTargets(long geneOfEvent, String event) {
        Map<String,Set<Long>> mapEventTargets = getDrugTargetAnnotation().get(geneOfEvent);
        if (mapEventTargets==null) {
            return Collections.emptySet();
        }
        
        Set<Long> set = mapEventTargets.get(event);
        if (set==null) {
            return Collections.emptySet();
        }
        
        return set;
    }
    // end of Temporary way of handling cases such as akt inhibitor for pten loss
    
}
