/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mskcc.cbio.portal.model.CanonicalGene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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
     */
    public static DaoGeneticAlteration getInstance() {
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
        
       if (ClickHouseBulkLoader.isBulkLoad() ) {
          //  write to the temp file maintained by the ClickHouseBulkLoader
          ClickHouseBulkLoader.getClickHouseBulkLoader("genetic_alteration").insertRecord(Integer.toString( geneticProfileId ),
        		  Integer.toString( geneticEntityId ), valueBuffer.toString());
          // return 1 because normal insert will return 1 if no error occurs
          return 1;
        } 
       
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement(
                    "INSERT INTO genetic_alteration (genetic_profile_id, genetic_entity_id, `values`) "
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
        ArrayList<Integer> orderedSampleList = DaoGeneticProfileSamples.getOrderedSampleList(geneticProfileId);
        if (orderedSampleList == null || orderedSampleList.size() ==0) {
            throw new IllegalArgumentException ("Could not find any samples for genetic" +
                    " profile ID:  " + geneticProfileId);
        }
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            final Connection queryCon = con;
            Collection<Integer> ids = (geneticEntityIds == null || geneticEntityIds.isEmpty()) ? null : geneticEntityIds;
            return ClickHouseBulkUploader.upload(ids, stagingTable -> {
                String inClause = stagingTable == null ? "" : " AND genetic_entity_id IN (SELECT id FROM " + stagingTable + ")";
                HashMap<Integer, HashMap<Integer, String>> result = new HashMap<>();
                try (PreparedStatement stmt = queryCon.prepareStatement(
                        "SELECT * FROM genetic_alteration WHERE genetic_profile_id = ?" + inClause)) {
                    stmt.setInt(1, geneticProfileId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            HashMap<Integer, String> mapSampleValue = new HashMap<>();
                            int geneticEntityId = rs.getInt("genetic_entity_id");
                            String values = rs.getString("values");
                            String[] valueParts = values.split(DELIM, -1);
                            int valuesLength = valueParts.length;
                            if (valuesLength - orderedSampleList.size() == 1 && valueParts[valuesLength - 1].isEmpty()) {
                                valuesLength -= 1;
                            }
                            if (valuesLength != orderedSampleList.size()) {
                                throw new IllegalStateException(
                                        "Data inconsistency detected: The length of the values for genetic profile with Id = "
                                                + geneticProfileId + " and genetic entity with id = " + geneticEntityId
                                                + " (" + valuesLength + " elements) does not match the expected length of the sample list ("
                                                + orderedSampleList.size() + " elements).");
                            }
                            for (int i = 0; i < orderedSampleList.size(); i++) {
                                mapSampleValue.put(orderedSampleList.get(i), valueParts[i]);
                            }
                            result.put(geneticEntityId, mapSampleValue);
                        }
                    }
                }
                return result;
            });
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, null, null);
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

            pstmt = con.prepareStatement("SELECT * FROM genetic_alteration WHERE genetic_profile_id = ? LIMIT 3000 OFFSET " + offSet);
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long entrezGeneId = DaoGeneOptimized.getEntrezGeneId(rs.getInt("genetic_entity_id"));
                String valuesString = rs.getString("values");
                if (valuesString.endsWith(DELIM)) {
                    valuesString = valuesString.substring(0, valuesString.length() - DELIM.length());
                }
                String[] values = valuesString.split(DELIM, -1);
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
                    ("SELECT * FROM genetic_alteration WHERE genetic_profile_id = ?");
            pstmt.setInt(1, geneticProfileId);

            rs = pstmt.executeQuery();
            while  (rs.next()) {
                Long entrezGeneId = DaoGeneOptimized.getEntrezGeneId(rs.getInt("genetic_entity_id"));
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
                    ("SELECT * FROM genetic_alteration WHERE genetic_profile_id = ?");
            pstmt.setInt(1, geneticProfileId);

            rs = pstmt.executeQuery();
            while  (rs.next()) {
            	int geneticEntityId = rs.getInt("genetic_entity_id");
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
                    ("SELECT count(*) FROM genetic_alteration WHERE genetic_profile_id = ?");
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
                    ("SELECT count(*) FROM genetic_alteration");
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
            pstmt = con.prepareStatement("DELETE from genetic_alteration WHERE genetic_profile_id=?");
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
