/*
 * Copyright (c) 2017 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.util.GenePanelUtil;
import org.mskcc.cbio.portal.util.GenePanelUtil.Pair;

import static org.mskcc.cbio.portal.util.GenePanelUtil.getAddRemove;

/**
 * @author heinsz
 */
public class DaoGenePanel {

    private static final String GENE_PANEL_SEQUENCE = "seq_gene_panel";
    private static final Map<String, GenePanel> genePanelMap = initMap();

    /**
     * @param stableId Name of the gene panel, e.g. TESTPANEL1
     * @return Gets a gene panel based on its stable id in the database.
     */
    public static GenePanel getGenePanelByStableId(String stableId) {
        return genePanelMap.get(stableId);
    }

    /**
     * @return All available gene panels
     */
    public static Collection<GenePanel> getAllGenePanels() {
        return genePanelMap.values();
    }

    private static Map<String, GenePanel> initMap() {
        Map<String, GenePanel> genePanelMap = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGenePanel.class);
            pstmt = con.prepareStatement("SELECT * FROM gene_panel");
            rs = pstmt.executeQuery();
            genePanelMap = extractGenePanelMap(rs);
        } catch (SQLException | DaoException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGenePanel.class, con, pstmt, rs);
        }
        return genePanelMap;
    }

    private static Map<String, GenePanel> extractGenePanelMap(ResultSet rs) throws DaoException {
        Map<String, GenePanel> genePanelMap = new HashMap<>();
        try {
            while (rs.next()) {
                GenePanel gp = new GenePanel();
                gp.setInternalId(rs.getInt("internal_id"));
                gp.setStableId(rs.getString("stable_id"));
                gp.setDescription(rs.getString("description"));
                gp.setGenes(extractGenePanelGenes(gp.getInternalId()));
                genePanelMap.put(gp.getStableId(), gp);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return genePanelMap;
    }

    private static Set<CanonicalGene> extractGenePanelGenes(Integer genePanelId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashSet<CanonicalGene> toReturn = new HashSet<>();
        try {
            con = JdbcUtil.getDbConnection(DaoGenePanel.class);
            pstmt = con.prepareStatement("SELECT * FROM gene_panel_list where internal_id = ?");
            pstmt.setInt(1, genePanelId);
            rs = pstmt.executeQuery();
            DaoGeneOptimized daoGeneOpt = DaoGeneOptimized.getInstance();
            while (rs.next()) {
                CanonicalGene gene = daoGeneOpt.getGene(rs.getLong(2));
                toReturn.add(gene);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenePanel.class, con, pstmt, rs);
        }
        return toReturn;
    }

    /**
     * Adds a new gene panel to the database.
     *
     * @param stableId       Name of the gene panel, e.g. TESTPANEL1
     * @param description    Description for the gene panel
     * @param canonicalGenes Genes to be added to the database
     * @throws DaoException
     */
    public static void addGenePanel(String stableId, String description, Set<CanonicalGene> canonicalGenes)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        if (stableId == null) {
            throw new DaoException("Gene Panel stable ID cannot be null.");
        }
        if (canonicalGenes == null || canonicalGenes.isEmpty()) {
            throw new DaoException("Gene Panel gene list cannot be null or empty.");
        }

        try {
            con = JdbcUtil.getDbConnection(DaoGenePanel.class);
            long genePanelId = ClickHouseAutoIncrement.nextId(GENE_PANEL_SEQUENCE);
            pstmt = con.prepareStatement("INSERT INTO gene_panel (`internal_id`, `stable_id`, `description`) VALUES (?,?,?)");
            pstmt.setLong(1, genePanelId);
            pstmt.setString(2, stableId);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            addGenePanelGeneList((int) genePanelId, canonicalGenes);
            GenePanel gp = new GenePanel();
            gp.setInternalId((int) genePanelId);
            gp.setStableId(stableId);
            gp.setDescription(description);
            gp.setGenes(canonicalGenes);
            genePanelMap.put(stableId, gp);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenePanel.class, con, pstmt, null);
        }
    }

    private static void addGenePanelGeneList(Integer internalId, Set<CanonicalGene> canonicalGenes)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGenePanel.class);
            for (CanonicalGene canonicalGene : canonicalGenes) {
                pstmt = con.prepareStatement("INSERT INTO gene_panel_list (`internal_id`, `gene_id`) VALUES (?,?)");
                pstmt.setInt(1, internalId);
                pstmt.setLong(2, canonicalGene.getEntrezGeneId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenePanel.class, con, pstmt, null);
        }
    }

    /**
     * Deletes an existing gene panel in the database.
     *
     * @param genePanel
     * @throws DaoException
     */
    public static void deleteGenePanel(GenePanel genePanel) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGenePanel.class);
            pstmt = con.prepareStatement("DELETE from gene_panel WHERE internal_id = ?");
            pstmt.setInt(1, genePanel.getInternalId());
            pstmt.executeUpdate();
            genePanelMap.remove(genePanel.getStableId());
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenePanel.class, con, pstmt, null);
        }
    }

    /**
     * Updates an existing gene panel to the incoming set of genes.
     *
     * @param genePanel
     * @param incoming
     * @throws DaoException
     */
    public static void updateGenePanel(GenePanel genePanel, Set<CanonicalGene> incoming) throws DaoException {
        String stableId = genePanel.getStableId();
        Integer internalId = genePanel.getInternalId();

        if (stableId == null) {
            throw new DaoException("Gene Panel stable ID cannot be null.");
        }

        updateGenePanelGeneList(internalId, incoming);
    }

    /**
     * Prints the genes to be added and removed from a gene panel in the database.
     *
     * @param genePanel
     * @param incoming
     * @throws DaoException
     */
    public static void updatePreview(GenePanel genePanel, Set<CanonicalGene> incoming) throws DaoException {
        Pair pair = getAddRemove(incoming, extractGenePanelGenes(genePanel.getInternalId()));
        String add = pair.add.stream()
            .map(CanonicalGene::toString)
            .collect(Collectors.joining(", "));
        String remove = pair.remove.stream()
            .map(CanonicalGene::toString)
            .collect(Collectors.joining(", "));

        System.out.printf("ADD: %s\n", add);
        System.out.printf("REMOVE: %s\n", remove);
    }

    private static boolean genePanelExists(Integer internalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, GenePanel> genePanelMap;

        try {
            con = JdbcUtil.getDbConnection(DaoGenePanel.class);
            pstmt = con.prepareStatement("SELECT * FROM gene_panel WHERE internal_id = ?");
            pstmt.setInt(1, internalId);
            rs = pstmt.executeQuery();
            genePanelMap = extractGenePanelMap(rs);
            return !genePanelMap.isEmpty();
        } catch (SQLException e) {
            return false;
        } finally {
            JdbcUtil.closeAll(DaoGenePanel.class, con, pstmt, rs);
        }
    }

    /**
     * Inserts and deletes genes from a gene panel represented by an internalId in
     * the database.
     */
    private static void updateGenePanelGeneList(Integer internalId, Set<CanonicalGene> incoming) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            if (!genePanelExists(internalId)) {
                System.out.println("Gene panel does not exist in gene_panel, exiting");
                return;
            }

            con = JdbcUtil.getDbConnection(DaoGenePanel.class);
            Pair pair = getAddRemove(incoming, extractGenePanelGenes(internalId));
            Set<CanonicalGene> toAdd = pair.add;
            Set<CanonicalGene> toRemove = pair.remove;

            // Add and remove genes from specified gene panel
            for (CanonicalGene canonicalGene : toAdd) {
                pstmt = con.prepareStatement("INSERT INTO gene_panel_list (`internal_id`, `gene_id`) VALUES (?,?)");
                pstmt.setInt(1, internalId);
                pstmt.setLong(2, canonicalGene.getEntrezGeneId());
                pstmt.executeUpdate();
            }
            for (CanonicalGene canonicalGene : toRemove) {
                pstmt = con.prepareStatement("DELETE FROM gene_panel_list WHERE internal_id=? AND gene_id=?");
                pstmt.setInt(1, internalId);
                pstmt.setLong(2, canonicalGene.getEntrezGeneId());
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenePanel.class, con, pstmt, rs);
        }
    }
}
