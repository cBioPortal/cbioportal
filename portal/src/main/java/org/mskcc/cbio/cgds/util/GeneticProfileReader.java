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

package org.mskcc.cbio.cgds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;
import org.mskcc.cbio.cgds.model.GeneticProfile;

/**
 * Prepare a GeneticProfile for having its data loaded.
 * 
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class GeneticProfileReader {

   /**
    * Load a GeneticProfile. Get a stableID from a description file. If the same
    * GeneticProfile already exists in the dbms use it, otherwise create a new
    * GeneticProfile dbms record, defining all parameters from the file.
    * 
    * @author Ethan Cerami
    * @author Arthur Goldberg goldberg@cbio.mskcc.org
    * 
    * @param file
    *           A handle to a description of the genetic profile, i.e., a
    *           'description' or 'meta' file.
    * @return an instantiated GeneticProfile record
    * @throws IOException
    *            if the description file cannot be read
    * @throws DaoException
    */
   public static GeneticProfile loadGeneticProfile(File file /*, int updateAction*/ ) throws IOException, DaoException {
      GeneticProfile geneticProfile = loadGeneticProfileFromMeta(file);
      DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
      GeneticProfile existingGeneticProfile = daoGeneticProfile.getGeneticProfileByStableId(geneticProfile
               .getStableId());

      if (existingGeneticProfile != null) {
         // the dbms already contains a GeneticProfile with the file's stable_id
         System.out.println("Warning: Possible Error: Existing Profile Found with Stable ID:  "
                  + existingGeneticProfile.getStableId());
         // target line isn't stored in the dbms
         existingGeneticProfile.setTargetLine(geneticProfile.getTargetLine()); 

         System.out.println("Action:  Clobbering all old data");
         System.out.println("Deleting all matching records in table:  gene_in_profile");
         System.out.println("Deleting all matching records in table:  genetic_alteration");
         DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
         daoGeneticAlteration.deleteAllRecordsInGeneticProfile(existingGeneticProfile.getGeneticProfileId());

         System.out.println("Deleting all matching records in table:  micro_rna_alteration");
         DaoMicroRnaAlteration daoMicroRnaAlteration = DaoMicroRnaAlteration.getInstance();
         daoMicroRnaAlteration.deleteAllRecordsInGeneticProfile(existingGeneticProfile.getGeneticProfileId());

         System.out.println("Deleting all matching records in table:  mutation");
         DaoMutation daoMutation = DaoMutation.getInstance();
         daoMutation.deleteAllRecordsInGeneticProfile(existingGeneticProfile.getGeneticProfileId());

         System.out.println("Deleting all matching cases in table:  genetic_profile_cases");
         DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
         daoGeneticProfileCases.deleteAllCasesInGeneticProfile(existingGeneticProfile.getGeneticProfileId());
         daoGeneticAlteration.deleteAllRecordsInGeneticProfile(existingGeneticProfile.getGeneticProfileId());
         return existingGeneticProfile;
      } else {
         // add new profile
         daoGeneticProfile.addGeneticProfile(geneticProfile);
         // load it into a GeneticProfile
         GeneticProfile newGeneticProfile = daoGeneticProfile.getGeneticProfileByStableId(geneticProfile.getStableId());
         newGeneticProfile.setTargetLine(geneticProfile.getTargetLine());
         return newGeneticProfile;
      }
   }

   /**
    * Load a GeneticProfile from a description file.
    * 
    * @author Ethan Cerami
    * @author Arthur Goldberg goldberg@cbio.mskcc.org
    * 
    * @param file
    *           A handle to a description of the genetic profile, i.e., a
    *           'description' or 'meta' file.
    * @return an instantiated GeneticProfile
    * @throws IOException
    *            if the description file cannot be read
    * @throws DaoException
    */
   public static GeneticProfile loadGeneticProfileFromMeta(File file) throws IOException, DaoException {
      Properties properties = new Properties();
      properties.load(new FileInputStream(file));
      
      // when loading cancer studies and their profiles from separate files, 
      // use the cancer_study_identifier as a unique id for each study.
      // this was called the "cancer_type_id" previously.
      // eventually, it won't be needed when studies are loaded by a connected client that
      // knows its study_id in its state
      
      String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");
      if (cancerStudyIdentifier == null) {
         throw new IllegalArgumentException("cancer_study_identifier is not specified.");
      }
      CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
      if (cancerStudy == null) {
         throw new IllegalArgumentException("cancer study identified by cancer_study_identifier "
                  + cancerStudyIdentifier + " not found in dbms.");
      }
      
      String stableId = properties.getProperty("stable_id");
      if (stableId == null) {
         throw new IllegalArgumentException("stable_id is not specified.");
      }

      String profileName = properties.getProperty("profile_name");
      String profileDescription = properties.getProperty("profile_description");
      String geneticAlterationTypeString = properties.getProperty("genetic_alteration_type");
      if (profileName == null) {
         throw new IllegalArgumentException("profile_name is not specified.");
      } else if (profileDescription == null) {
         throw new IllegalArgumentException("profile_description is not specified.");
      } else if (geneticAlterationTypeString == null) {
         throw new IllegalArgumentException("genetic_alteration_type is not specified.");
      }

      boolean showProfileInAnalysisTab = true;
      String showProfileInAnalysisTabStr = properties.getProperty("show_profile_in_analysis_tab");
      if (showProfileInAnalysisTabStr != null && showProfileInAnalysisTabStr.equalsIgnoreCase("FALSE")) {
         showProfileInAnalysisTab = false;
      }

      profileDescription = profileDescription.replaceAll("\t", " ");
      GeneticAlterationType alterationType = GeneticAlterationType.getType(geneticAlterationTypeString);

      GeneticProfile geneticProfile = new GeneticProfile();
      geneticProfile.setCancerStudyId(cancerStudy.getInternalId());
      geneticProfile.setStableId(stableId);
      geneticProfile.setProfileName(profileName);
      geneticProfile.setProfileDescription(profileDescription);
      geneticProfile.setGeneticAlterationType(alterationType);
      geneticProfile.setShowProfileInAnalysisTab(showProfileInAnalysisTab);
      geneticProfile.setTargetLine(properties.getProperty("target_line"));
      return geneticProfile;
   }
}