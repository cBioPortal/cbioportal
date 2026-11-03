/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.util.*;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.GenePanel;

/**
 * Genetic Profile Util Class.
 *
 */
public class GeneticProfileUtil {

    /**
     * Gets the GeneticProfile with the Specified GeneticProfile ID.
     * @param profileId GeneticProfile ID.
     * @param profileList List of Genetic Profiles.
     * @return GeneticProfile or null.
     */
    public static GeneticProfile getProfile(String profileId,
            ArrayList<GeneticProfile> profileList) {
        for (GeneticProfile profile : profileList) {
            if (profile.getStableId().equals(profileId)) {
                return profile;
            }
        }
        return null;
    }

    public static int getGenePanelId(String panelId) {
        GenePanel genePanel = DaoGenePanel.getGenePanelByStableId(panelId);
        if (genePanel == null) {
            throw new NoSuchElementException("Gene panel with id " + panelId + " not found.");
        }
        return genePanel.getInternalId();
    }

}
