/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.repository;

import java.util.*;
/**
 *
 * @author heinsz
 */

import org.cbioportal.model.*;
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.model.GenePanelWithSamples;

public interface GenePanelRepositoryLegacy {
    List<GenePanelWithSamples> getGenePanelsByProfile(String profileId);
    // TODO: All of the below methods are for importing purposes only. They should be
    // removed once a proper import solution is put in place.
    List<GenePanel> getGenePanelByStableId(String stableId);
    List<GenePanel> getGenePanels();
    MolecularProfile getGeneticProfileByStableId(String stableId);
    Gene getGeneByEntrezGeneId(Integer geneId);
    Gene getGeneByHugoSymbol(String symbol);
    Gene getGeneByAlias(String symbol);
    boolean sampleProfileMappingExistsByProfile(Integer profileId);
    boolean sampleProfileMappingExistsByPanel(Integer panelId);
    void insertGenePanel(Map<String, Object> map);
    void deleteGenePanel(Integer internalId);
    void deleteGenePanelList(Integer internalId);
    void deleteSampleProfileMappingByProfile(Integer profileId);
    void deleteSampleProfileMappingByPanel(Integer panelId);
    void insertGenePanelList(Map<String, Object> map);
    void insertGenePanelListByHugo(Map<String, Object> map);
}
