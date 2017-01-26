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

package org.cbioportal.persistence.mybatis;

/**
 *
 * @author heinsz
 */

import java.util.*;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.*;

public interface GenePanelMapper {

    List<GenePanelWithSamples> getGenePanelsByProfile(@Param("profileId") String profileId);
    // TODO: All of the below methods are for importing purposes only. They should be
    // removed once a proper import solution is put in place.
    List<GenePanel> getGenePanelByStableId(@Param("stableId") String stableId);
    List<GenePanel> getGenePanels();
    Sample getSampleByStableIdAndStudyId(@Param("stableId") String stableId, @Param("studyId") String studyId);
    GeneticProfile getGeneticProfileByStableId(@Param("stableId") String stableId);
    Gene getGeneByEntrezGeneId(@Param("geneId") Integer geneId);
    Gene getGeneByHugoSymbol(@Param("symbol") String symbol);
    Gene getGeneByAlias(@Param("symbol") String symbol);
    Integer sampleProfileMappingExistsByProfile(@Param("profileId") Integer profileId);
    Integer sampleProfileMappingExistsByPanel(@Param("panelId") Integer panelId);    
    void insertGenePanel(Map<String, Object> map);
    void deleteGenePanel(@Param("internalId") Integer internalId);
    void deleteGenePanelList(@Param("internalId") Integer internalId);
    void deleteSampleProfileMappingByProfile(@Param("profileId") Integer profileId);
    void deleteSampleProfileMappingByPanel(@Param("panelId") Integer panelId);
    void insertGenePanelList(Map<String, Object> map);
    void insertGenePanelListByHugo(Map<String, Object> map);
    void insertGenePanelSampleProfileMap(Map<String, Object> map);
}
