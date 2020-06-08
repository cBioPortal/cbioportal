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

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.CanonicalGene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang.StringUtils;

/**
 * Data Access Object for the Genetic Alteration Table.
 *
 * @author Ethan Cerami.
 */
public class DaoGeneticAlteration {
    private static final String DELIM = ",";
    public static final String NAN = "NaN";
    private static DaoGeneticAlteration daoGeneticAlteration = null;

    /**
     * Private Constructor (Singleton pattern).
     */
    private DaoGeneticAlteration() {
    }

    /**
     * Gets Instance of Dao Object. (Singleton pattern).
     *
     * @return DaoGeneticAlteration Object.
     * @throws DaoException Dao Initialization Error.
     */
    public static DaoGeneticAlteration getInstance() throws DaoException {
        if (daoGeneticAlteration == null) {
            daoGeneticAlteration = new DaoGeneticAlteration();
            
        }

        return daoGeneticAlteration;
    }

    public static interface AlterationProcesser {
        ObjectNode process(
            long entrezGeneId,
            String[] values,
            ArrayList<Integer> orderedSampleList
        );
    }

    /**
     * Adds a Row of Genetic Alterations associated with a Genetic Profile ID and Entrez Gene ID.
     * @param geneticProfileId Genetic Profile ID.
     * @param entrezGeneId Entrez Gene ID.
     * @param values DELIM separated values.
     * @return number of rows successfully added.
     * @throws DaoException Database Error.
     */
    public int addGeneticAlterations(int geneticProfileId, long entrezGeneId, String[] values)
            throws DaoException {
    	return addGeneticAlterationsForGeneticEntity(geneticProfileId, DaoGeneOptimized.getGeneticEntityId(entrezGeneId), values);
    }
    
    public int addGeneticAlterationsForGeneticEntity(int geneticProfileId, int geneticEntityId, String[] values)
            throws DaoException {
    
        StringBuffer valueBuffer = new StringBuffer();
        for (String value:  values) {
            if (value.contains(DELIM)) {
                throw new IllegalArgumentException ("Value cannot contain delim:  " + DELIM
                    + " --> " + value);
            }
            valueBuffer.append(value).append(DELIM);
        }
        
       if (MySQLbulkLoader.isBulkLoad() ) {
          //  write to the temp file maintained by the MySQLbulkLoader
          MySQLbulkLoader.getMySQLbulkLoader("genetic_alteration").insertRecord(Integer.toString( geneticProfileId ),
        		  Integer.toString( geneticEntityId ), valueBuffer.toString());
          // return 1 because normal insert will return 1 if no error occurs
          return 1;
        } 
       
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO genetic_alteration (GENETIC_PROFILE_ID, " +
                            " GENETIC_ENTITY_ID," +
                            " `VALUES`) "
                            + "VALUES (?,?,?)");
            pstmt.setInt(1, geneticProfileId);
            pstmt.setLong(2, geneticEntityId);
            pstmt.setString(3, valueBuffer.toString());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }

    /**
     * Gets the Specified Genetic Alteration.
     *
     * @param geneticProfileId  Genetic Profile ID.
     * @param sampleId            Sample ID.
     * @param entrezGeneId      Entrez Gene ID.
     * @return value or NAN.
     * @throws DaoException Database Error.
     */
    public String getGeneticAlteration(int geneticProfileId, int sampleId,
            long entrezGeneId) throws DaoException {
        HashMap <Integer, String> sampleMap = getGeneticAlterationMap(geneticProfileId, entrezGeneId);
        if (sampleMap.containsKey(sampleId)) {
            return sampleMap.get(sampleId);
        } else {
            return NAN;
        }
    }

    /**
     * Gets a HashMap of Values, keyed by Sample ID.
     * @param geneticProfileId  Genetic Profile ID.
     * @param entrezGeneId      Entrez Gene ID.
     * @return HashMap of values, keyed by Sample ID.
     * @throws DaoException Database Error.
     */
    public HashMap<Integer, String> getGeneticAlterationMap(int geneticProfileId,
            long entrezGeneId) throws DaoException {
        HashMap<Long,HashMap<Integer, String>> map = getGeneticAlterationMap(geneticProfileId, Collections.singleton(entrezGeneId));
        if (map.isEmpty()) {
            return new HashMap<Integer, String>();
        }
        
        return map.get(entrezGeneId);
    }

    /**
     * Returns the map of entrezGeneId as key and map with all
     * respective CaseId and Values as value. 
     * 
     * @param geneticProfileId  Genetic Profile ID.
     * @param entrezGeneIds      Entrez Gene IDs.
     * @return Map<Entrez, Map<CaseId, Value>>.
     * @throws DaoException Database Error.
     */
    public HashMap<Long,HashMap<Integer, String>> getGeneticAlterationMap(int geneticProfileId, Collection<Long> entrezGeneIds) throws DaoException {
    	Collection<Integer> geneticEntityIds = null;
    	if (entrezGeneIds != null) {
    		//translate entrezGeneIds to corresponding geneticEntityIds:
        	geneticEntityIds = new ArrayList<Integer>();
	    	for (Long entrezGeneId : entrezGeneIds) {
	    		geneticEntityIds.add(DaoGeneOptimized.getGeneticEntityId(entrezGeneId));
	    	}
    	}
    	HashMap<Integer, HashMap<Integer, String>> intermediateMap = getGeneticAlterationMapForEntityIds(geneticProfileId, geneticEntityIds);
    	//translate back to entrez, since intermediateMap is keyed by geneticEntityIds:
    	HashMap<Long, HashMap<Integer, String>> resultMap = new HashMap<Long, HashMap<Integer, String>>();
    	Iterator<Entry<Integer, HashMap<Integer, String>>> mapIterator = intermediateMap.entrySet().iterator();
    	while (mapIterator.hasNext()) {
    		Entry<Integer, HashMap<Integer, String>> mapEntry = mapIterator.next();
    		resultMap.put(DaoGeneOptimized.getEntrezGeneId(mapEntry.getKey()), mapEntry.getValue());
    	}    	
    	return resultMap;
    }
    
    /**
     * Returns the map of geneticEntityIds as key and map with all
     * respective CaseId and Values as value. 
     * 
     * @param geneticProfileId
     * @param geneticEntityIds
     * @return Map<GeneticEntityId, Map<CaseId, Value>>.
     * @throws DaoException
     */
    public HashMap<Integer,HashMap<Integer, String>> getGeneticAlterationMapForEntityIds(int geneticProfileId, Collection<Integer> geneticEntityIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashMap<Integer,HashMap<Integer, String>> map = new HashMap<Integer,HashMap<Integer, String>>();
        ArrayList<Integer> orderedSampleList = DaoGeneticProfileSamples.getOrderedSampleList(geneticProfileId);
        if (orderedSampleList == null || orderedSampleList.size() ==0) {
            throw new IllegalArgumentException ("Could not find any samples for genetic" +
                    " profile ID:  " + geneticProfileId);
        }
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            if (geneticEntityIds == null) {
                pstmt = con.prepareStatement("SELECT * FROM genetic_alteration WHERE"
                        + " GENETIC_PROFILE_ID = " + geneticProfileId);
            } else {
                pstmt = con.prepareStatement("SELECT * FROM genetic_alteration WHERE"
                        + " GENETIC_PROFILE_ID = " + geneticProfileId
                        + " AND GENETIC_ENTITY_ID IN ("+StringUtils.join(geneticEntityIds, ",")+")");
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                HashMap<Integer, String> mapSampleValue = new HashMap<Integer, String>();
                int geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
                String values = rs.getString("VALUES");
                //hm.debug..
                String valueParts[] = values.split(DELIM);
                for (int i=0; i<valueParts.length; i++) {
                    String value = valueParts[i];
                    Integer sampleId = orderedSampleList.get(i);
                    mapSampleValue.put(sampleId, value);
                }
                map.put(geneticEntityId, mapSampleValue);
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }

    /**
     * Process SQL result alteration data
     * @param geneticProfileId  Genetic Profile ID.
     * @param entrezGeneIds      Entrez Gene IDs.
     * @param processor         Implementation of AlterationProcesser Interface
     * @return ArrayList<ObjectNode>
     * @throws DaoException Database Error, MathException
     */
    public static ArrayList<ObjectNode> getProcessedAlterationData(
            int geneticProfileId,               //queried profile internal id (num)
            //Set<Long> entrezGeneIds,            //list of genes in calculation gene pool (all genes or only cancer genes)
            int offSet,                         //OFFSET for LIMIT (to get only one segment of the genes)
            AlterationProcesser processor       //implemented interface
    ) throws DaoException {

        ArrayList<ObjectNode> result = new ArrayList<>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        ArrayList<Integer> orderedSampleList = DaoGeneticProfileSamples.getOrderedSampleList(geneticProfileId);
        if (orderedSampleList == null || orderedSampleList.size() ==0) {
            throw new IllegalArgumentException ("Could not find any samples for genetic" +
                    " profile ID:  " + geneticProfileId);
        }

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);

            pstmt = con.prepareStatement("SELECT * FROM genetic_alteration WHERE"
                    + " GENETIC_PROFILE_ID = " + geneticProfileId
                    + " LIMIT 3000 OFFSET " + offSet);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                long entrezGeneId = DaoGeneOptimized.getEntrezGeneId(rs.getInt("GENETIC_ENTITY_ID"));
                String[] values = rs.getString("VALUES").split(DELIM);
                ObjectNode datum = processor.process(
                        entrezGeneId,
                        values,
                        orderedSampleList);
                if (datum != null) result.add(datum);
            }
            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }

    }

    /**
     * Gets all Genes in a Specific Genetic Profile.
     * @param geneticProfileId  Genetic Profile ID.
     * @return Set of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public Set<CanonicalGene> getGenesInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set <CanonicalGene> geneList = new HashSet <CanonicalGene>();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_alteration WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);

            rs = pstmt.executeQuery();
            while  (rs.next()) {
                Long entrezGeneId = DaoGeneOptimized.getEntrezGeneId(rs.getInt("GENETIC_ENTITY_ID"));
                geneList.add(daoGene.getGene(entrezGeneId));
            }
            return geneList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }

    /**
     * Gets all Genes in a Specific Genetic Profile.
     * @param geneticProfileId  Genetic Profile ID.
     * @return Set of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public static Set<Integer> getEntityIdsInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set<Integer> geneticEntityList = new HashSet<>();

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_alteration WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);

            rs = pstmt.executeQuery();
            while  (rs.next()) {
            	int geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
                geneticEntityList.add(geneticEntityId);
            }
            return geneticEntityList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }

    /**
     * Gets the total number of all genes in a Specific Genetic Profile.
     * @param geneticProfileId  Genetic Profile ID.
     * @return number of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public static int getGenesCountInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM genetic_alteration WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }

    /**
     * Gets total number of records in table.
     * @return number of records.
     * @throws DaoException Database Error.
     */
    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM genetic_alteration");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all Genetic Alteration Records associated with the specified Genetic Profile ID.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @throws DaoException Database Error.
     */
    public void deleteAllRecordsInGeneticProfile(long geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement("DELETE from " +
                    "genetic_alteration WHERE GENETIC_PROFILE_ID=?");
            pstmt.setLong(1, geneticProfileId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all Records in Table.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE genetic_alteration");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }
}
