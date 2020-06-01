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

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * JUnit test for WebService class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestWebService {
	
   private CancerStudy publicCancerStudy;
   private CancerStudy privateCancerStudy1;
   private CancerStudy privateCancerStudy2;
   private User user1;
   private User user2;
   private String cleartextPwd;
   private GeneticProfile privateGeneticProfile;
   private GeneticProfile publicGeneticProfile;
   
   private AccessControl oldAccessControl;

   @Before
   public void setUp() throws DaoException {
      oldAccessControl = SpringUtil.getAccessControl();

      // This is truly awful, but basically mocks the access control. The API for
      // isAccessibleCancerStudy is a bit silly, as it's intended to be boolean
      // but is really a list. So we mock with a non-empty list with a null in it.

      AccessControl control = createMock(AccessControl.class);
      List<CancerStudy> mockTrue = new ArrayList<CancerStudy>();
      mockTrue.add(null);
      UserDetails mockUserDetails = createMock(UserDetails.class);
      expect(control.isAccessibleCancerStudy(isA(String.class))).andStubReturn(mockTrue);
      expect(control.getUserDetails()).andStubReturn(mockUserDetails);
      replay(control);
      new SpringUtil().setAccessControl(control);
      
      DaoCancerStudy.reCacheAll();
   }
   
   @After
   public void tearDown() {
	   new SpringUtil().setAccessControl(oldAccessControl);
   }
   
   @Test
   public void testWebServiceGetTypesOfCancer() throws Exception {
      
      WebService webService = new WebService();

      // null request
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      NullHttpServletResponse aNullHttpServletResponse = new NullHttpServletResponse();
      webService.processClient( aNullHttpServletRequest, aNullHttpServletResponse );
      assertTrue( aNullHttpServletResponse.getOutput().contains("Error: you must specify a cmd parameter.\n") );
      
      checkRequest( mkStringArray( WebService.CMD, "getTypesOfCancer" ), 
               mkStringArray( "type_of_cancer_id\tname", "acbc\tAdenoid Cystic Breast Cancer" ) );
   }
   
   
   @Test
   public void testWebServiceBadCommand() throws Exception {

      // bad command
      checkRequest( mkStringArray( WebService.CMD, "badCommand" ), "Error: 'badCommand' not a valid command." );
      
      /* TBD: Recoded when we provide granualar access
      // public studies
      String[] publicStudies = mkStringArray( "cancer_study_id\tname\tdescription", 
               studyLine( publicCancerStudy ) );
      checkRequest( mkStringArray( WebService.CMD, "getCancerStudies" ), publicStudies );
      */
   }
      
   @Test
   public void testWebServiceGetGeneticProfilesNoStudy() throws Exception {

      // no cancer_study_id for "getGeneticProfiles" 
      checkRequest( mkStringArray( WebService.CMD, "getGeneticProfiles" ), 
            mkStringArray( "Error: " + "No cancer study (cancer_study_id), or genetic profile (genetic_profile_id) " +
                              "or case list or (case_list) case set (case_set_id) provided by request. " +
                              "Please reformulate request." ) );
   }
   
   @Test
   public void testWebServiceGetGeneticProfiles() throws Exception {


      checkRequest( mkStringArray( WebService.CMD, "getGeneticProfiles",
              WebService.CANCER_STUDY_ID, CancerStudy.NO_SUCH_STUDY +"" ),
            mkStringArray( "Error: " + "Problem when identifying a cancer study for the request." ) );
   }
   
   @Test
   public void testWebService() throws Exception {

      // getGeneticProfiles for public study
      String[] publicGenePro = mkStringArray(
         "genetic_profile_id\tgenetic_profile_name\tgenetic_profile_description\t" +
            "cancer_study_id\tgenetic_alteration_type\tshow_profile_in_analysis_tab",
         "study_tcga_pub_gistic\tPutative copy-number alterations from GISTIC\tPutative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.\t1\tCOPY_NUMBER_ALTERATION\ttrue");  
      
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.CANCER_STUDY_ID, "study_tcga_pub"),
            publicGenePro );

      /* TBD: Recoded when we provide granualar access
      // with bad key
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.SECRET_KEY, "not_good_key", 
            WebService.CANCER_STUDY_ID, publicCancerStudy.getCancerStudyStableId()),
            publicGenePro );
      
      // missing email address for private study
      String deniedError = "Error: User cannot access the cancer study called 'name'." +
      "Please provide credentials to access private data.";
      checkRequest( mkStringArray( WebService.CMD, "getGeneticProfiles", WebService.CANCER_STUDY_ID, "study1" ),
               mkStringArray( deniedError ) );
      
      // denied for "getGeneticProfiles"; no key 
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.CANCER_STUDY_ID, "study1",
            WebService.EMAIL_ADDRESS, user1.getEmail() 
            ), 
            mkStringArray( deniedError ) );
      
      // denied for "getGeneticProfiles"; bad key 
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.SECRET_KEY, "not_good_key", 
            WebService.CANCER_STUDY_ID, "study1",
            WebService.EMAIL_ADDRESS, user1.getEmail() 
            ), 
            mkStringArray( deniedError ) );

      // getGeneticProfiles works for private study 
      String[] privateGenePro1 = mkStringArray( "genetic_profile_id\tgenetic_profile_name\tgenetic_profile_description"
            + "\tcancer_study_id\tgenetic_alteration_type\tshow_profile_in_analysis_tab",
      "stableIdPrivate\tprofileName\tprofileDescription\t1\tCOPY_NUMBER_ALTERATION\ttrue");  
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.SECRET_KEY, cleartextPwd, 
            WebService.CANCER_STUDY_ID, "study1", 
            WebService.EMAIL_ADDRESS, user1.getEmail() 
            ), 
            mkStringArray( privateGenePro1 ) );
      */
   }
   
   private void checkRequest( String[] requestFields, String... responseLines ) throws IOException, ServletException{
      checkRequest( false, requestFields, responseLines );
   }
   
   /**
    * check a request
    * @param debug            if true, print servlet's output
    * @param requestFields    array embedded with name, value pairs for the httpRequest 
    * @param responseLines    array of expected responses
    * @throws IOException
 * @throws ServletException 
    */
   private void checkRequest( boolean debug, String[] requestFields, String... responseLines ) throws IOException, ServletException{
      WebService webService = new WebService();
      webService.init();
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      NullHttpServletResponse aNullHttpServletResponse = new NullHttpServletResponse();
      for( int i=0; i<requestFields.length; i += 2 ){
         aNullHttpServletRequest.setParameter( requestFields[i], requestFields[i+1] );
      }

      webService.processClient( aNullHttpServletRequest, aNullHttpServletResponse );
      if( debug ){
         System.out.println( "\nResponse says:\n" + aNullHttpServletResponse.getOutput() );
      }
      String[] lines = aNullHttpServletResponse.getOutput().split("\n");
      for( int i=0; i<responseLines.length; i++ ){
          assertEquals( responseLines[i], lines[i]);
      }
   }
   
   private String[] mkStringArray( String... requestFields ){
      return requestFields;
   }
   
   private String studyLine( CancerStudy cancerStudy ){
      return cancerStudy.getCancerStudyStableId() + "\t" + cancerStudy.getName()
              + "\t" + cancerStudy.getDescription();
   }

   @Test
   public void testGetCancerStudyIDNull() throws Exception {
	   
      HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertEquals( 0, studies.size() );
   }
   
   @Test
   public void testGetCancerStudyIDInvalid() throws Exception {
	   
      // example getGeneticProfiles request      
      HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest.setParameter(WebService.CANCER_STUDY_ID, "HI");
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertTrue(studies.isEmpty());
   }

   @Test
   public void testGetCancerStudyIDNumeric() throws Exception {
   	   
      HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest.setParameter(WebService.CANCER_STUDY_ID, "33");
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertTrue(studies.isEmpty());
   }
   
   @Test
   public void testGetCancerStudyIDs() throws Exception {
   	   
      HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
	  aNullHttpServletRequest.setParameter(WebService.CANCER_STUDY_ID, "study_tcga_pub");
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertEquals( 1, studies.size() );
      assertTrue( studies.contains("study_tcga_pub"));
   }
   
   @Test
   public void testGetCancerStudySetIDs() throws Exception {
      // example getProfileData, getMutationData, ... request with existing CASE_SET_ID
      HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest.setParameter(WebService.CASE_SET_ID, "HI");
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertTrue(studies.isEmpty());
   }
    
   @Test
   public void testGetCancerStudySetIDList() throws Exception {
	   
      HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      
      CancerStudy study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
      DaoSampleList daoSampleList = new DaoSampleList();
      SampleList sampleList = new SampleList();
      sampleList.setName("Name0");
      sampleList.setDescription("Description0");
      sampleList.setStableId("stable_0");
      sampleList.setCancerStudyId(study.getInternalId());
      sampleList.setSampleListCategory(SampleListCategory.ALL_CASES_WITH_CNA_DATA);
      ArrayList<String> samples = new ArrayList<String>();
      samples.add("TCGA-1-S1");
      samples.add("TCGA-2-S1");
      sampleList.setSampleList(samples);
      daoSampleList.addSampleList(sampleList);
      
      aNullHttpServletRequest.setParameter(WebService.CASE_SET_ID, "stable_0" );
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertFalse(studies.isEmpty());
   }
   
   @Test
   public void testGetCancerStudySetProfileID() throws Exception {

	  HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
            
      // test situations when patient_set_id not provided, but profile_id is, as by getProfileData
      aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest.setParameter(WebService.GENETIC_PROFILE_ID, "study_tcga_pub_mutations" );
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertEquals( 1, studies.size() );
      assertTrue( studies.contains("study_tcga_pub"));
   }
   
   @Test
   public void testGetCancerStudySetPrivateProfile() throws Exception {

	  HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      
      // test situation when multiple profile_ids provided, as by getProfileData
      aNullHttpServletRequest = new NullHttpServletRequest();
      
      // test situations when patient_set_id not provided, but profile_id is, as by getProfileData
      aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest.setParameter(WebService.GENETIC_PROFILE_ID, "study_tcga_pub_gistic,study_tcga_pub_mutations" );
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertEquals( 1, studies.size() );
      assertTrue( studies.contains("study_tcga_pub"));

      // Removed old test logic for public and private profiles since (a) the tests only check the private
      // profiles, and (b) this requires better test data
      
//      aNullHttpServletRequest.setParameter(
//               WebService.MOLECULAR_PROFILE_ID, privateGeneticProfile.getStableId() + ","
//              + publicGeneticProfile.getStableId() );
//      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
//      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
//              (privateGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
//      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
//              (privateGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
   }

   // TODO Add these originally commented out tests
      // test situation when a case_list is explicitly provided, as in getClinicalData, etc.
//      String c1 = "TCGA-12345";
//      DaoCaseProfile.addSampleProfile( c1, publicGeneticProfile.getGeneticProfileId());
//      aNullHttpServletRequest = new NullHttpServletRequest();
//      aNullHttpServletRequest.setParameter( WebService.CASE_LIST, c1 ); 
//      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
//      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
//              (publicGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
//
//      String c2 = "TCGA-54321";
//      DaoCaseProfile.addSampleProfile( c2, privateGeneticProfile.getGeneticProfileId() );
//      aNullHttpServletRequest = new NullHttpServletRequest();
//      aNullHttpServletRequest.setParameter( WebService.CASE_LIST, c1 + "," + c2 ); 
//      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
//      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
//              (privateGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
//      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
//              (publicGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
   
}
