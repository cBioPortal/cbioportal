/*
 * Copyright (c) 2015 - 2018 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.weblegacy;

import org.cbioportal.web.config.CacheMapUtilConfig;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.SerializationUtils;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Gene;
import org.cbioportal.model.MolecularProfile;
import org.mskcc.cbio.portal.model.Mutation;
import org.mskcc.cbio.portal.model.MutationEvent;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;
import org.cbioportal.model.Sample.SampleType;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.web.config.CustomObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.model.DBCancerType;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.persistence.CancerTypeMapperLegacy;
import org.mskcc.cbio.portal.persistence.GeneticProfileMapperLegacy;
import org.mskcc.cbio.portal.persistence.MutationMapperLegacy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {ApiControllerConfig.class, CustomObjectMapper.class, CacheMapUtilConfig.class})
public class ApiControllerTest {
    @Autowired
    private CancerTypeMapperLegacy cancerTypeMapperLegacyMock;
    @Autowired
    private GeneticProfileMapperLegacy geneticProfileMapperLegacyMock;
    @Autowired
    private MutationMapperLegacy mutationMapperLegacy;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private static List<Mutation> geneticprofiledataQuery1PersistenceFullMock; //full data from tables
    private static List<Mutation> geneticprofiledataQuery1ServiceMock;
    private static List<DBGeneticProfile> geneticProfileQuery1ServiceMock;

    @Before
    public void setup() {
        Mockito.reset(cancerTypeMapperLegacyMock);
        Mockito.reset(geneticProfileMapperLegacyMock);
        Mockito.reset(mutationMapperLegacy);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Ignore
    public void cancerTypeDataTest1() throws Exception {
        List<DBCancerType> mockResponse = new ArrayList<DBCancerType>();
        DBCancerType item1 = new DBCancerType();
        item1.id = "nmzl";
        item1.name = "Nodal Marginal Zone Lymphoma";
        item1.color = "LimeGreen";
        DBCancerType item2 = new DBCancerType();
        item2.id = "tcca";
        item2.name = "Choriocarcinoma";
        item2.color = "Red";
        mockResponse.add(item1);
        mockResponse.add(item2);
        Mockito.when(cancerTypeMapperLegacyMock.getAllCancerTypes()).thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/cancertypes")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("nmzl"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Nodal Marginal Zone Lymphoma"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].color").value("LimeGreen"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("tcca"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Choriocarcinoma"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].color").value("Red"))
                ;
    }

    @Test
    @Ignore
    public void cancerTypeDataTest2() throws Exception {
        List<DBCancerType> mockResponse = new ArrayList<DBCancerType>();
        DBCancerType item1 = new DBCancerType();
        item1.id = "nmzl";
        item1.name = "Nodal Marginal Zone Lymphoma";
        item1.color = "LimeGreen";
        DBCancerType item2 = new DBCancerType();
        item2.id = "tcca";
        item2.name = "Choriocarcinoma";
        item2.color = "Red";
        mockResponse.add(item1);
        mockResponse.add(item2);
        List<String> args = new ArrayList<String>(2);
        args.add("nmzl");
        args.add("tcca");
        Mockito.when(cancerTypeMapperLegacyMock.getCancerTypes(ArgumentMatchers.anyList())).thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/cancertypes")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("cancer_type_ids","nmzl,tcca")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("nmzl"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Nodal Marginal Zone Lymphoma"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].color").value("LimeGreen"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("tcca"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Choriocarcinoma"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].color").value("Red"))
                ;
    }

    @Ignore
    @Test
    public void geneticProfileDataTest1() throws Exception {
        List<Mutation> mockResponse = getGeneticprofiledataQuery1ServiceMock();
        List<DBGeneticProfile> ctMockResponse = getGeneticProfileQuery1ServiceMock();
        Mockito.when(geneticProfileMapperLegacyMock.getGeneticProfiles(
                        ArgumentMatchers.anyList()
)).thenReturn(ctMockResponse);
        Mockito.when(mutationMapperLegacy.getMutationsDetailed(
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyString()
                    )).thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/geneticprofiledata")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("genetic_profile_ids","brca_tcga_mutations")
                .param("genes","AKT1,TGFBR1")
                .param("sample_ids","TCGA-AC-A23H-01,TCGA-AR-A2LE-01,TCGA-GM-A3NW-01,TCGA-AN-A0XR-01,TCGA-AO-A12D-01,TCGA-BH-A0B5-01")
                .param("sample_list_id","brca_tcga_all")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(6)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample_list_id").value("brca_tcga_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample_list_id").value("brca_tcga_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].sample_list_id").value("brca_tcga_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].sample_list_id").value("brca_tcga_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].sample_list_id").value("brca_tcga_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].sample_list_id").value("brca_tcga_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrez_gene_id").value("207"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugo_gene_symbol").value("AKT1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample_id").value("TCGA-AN-A0XR-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amino_acid_change").value("L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].functional_impact_score").value("M"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link").value("getma.org/?cm=var&var=hg19,14,105246445,A,C&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link_pdb").value("getma.org/pdb.php?prot=AKT1_HUMAN&from=6&to=108&var=L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=AKT1_HUMAN&rb=6&re=108&var=L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].chr").value("14"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start_position").value("105246445"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].end_position").value("105246445"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].protein_start_position").value("52"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].protein_end_position").value("52"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_allele").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_allele").value("C"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrez_gene_id").value("207"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugo_gene_symbol").value("AKT1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample_id").value("TCGA-GM-A3NW-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].amino_acid_change").value("V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].functional_impact_score").value("N"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link").value("getma.org/?cm=var&var=hg19,14,105258971,C,G&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link_pdb").value("getma.org/pdb.php?prot=AKT1_HUMAN&from=1&to=5&var=V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=AKT1_HUMAN&rb=1&re=35&var=V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].chr").value("14"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].start_position").value("105258971"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].end_position").value("105258971"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].protein_start_position").value("4"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].protein_end_position").value("4"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_allele").value("C"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_allele").value("G"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].entrez_gene_id").value("7046"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].hugo_gene_symbol").value("TGFBR1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].sample_id").value("TCGA-AO-A12D-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].amino_acid_change").value("E228V"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].functional_impact_score").value("N"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].xvar_link").value("getma.org/?cm=var&var=hg19,9,101900249,A,T&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].xvar_link_pdb").value("getma.org/pdb.php?prot=TGFR1_HUMAN&from=205&to=492&var=E228V"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=TGFR1_HUMAN&rb=205&re=492&var=E228V"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].chr").value("9"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].start_position").value("101900249"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].end_position").value("101900249"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].protein_start_position").value("228"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].protein_end_position").value("228"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].reference_allele").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].variant_allele").value("T"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].entrez_gene_id").value("7046"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].hugo_gene_symbol").value("TGFBR1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].sample_id").value("TCGA-BH-A0B5-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].amino_acid_change").value("F396L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].functional_impact_score").value("M"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].xvar_link").value("getma.org/?cm=var&var=hg19,9,101908824,C,G&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].xvar_link_pdb").value("getma.org/pdb.php?prot=TGFR1_HUMAN&from=205&to=492&var=F396L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=TGFR1_HUMAN&rb=205&re=492&var=F396L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].chr").value("9"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].start_position").value("101908824"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].end_position").value("101908824"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].protein_start_position").value("396"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].protein_end_position").value("396"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].reference_allele").value("C"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].variant_allele").value("G"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].entrez_gene_id").value("7046"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].hugo_gene_symbol").value("TGFBR1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].sample_id").value("TCGA-AR-A2LE-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].amino_acid_change").value("R80Q"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].functional_impact_score").value("L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].xvar_link").value("getma.org/?cm=var&var=hg19,9,101891278,G,A&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].xvar_link_pdb").value("getma.org/pdb.php?prot=TGFR1_HUMAN&from=34&to=114&var=R80Q"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=TGFR1_HUMAN&rb=34&re=114&var=R80Q"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].chr").value("9"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].start_position").value("101891278"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].end_position").value("101891278"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].protein_start_position").value("80"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].protein_end_position").value("80"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].reference_allele").value("G"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].variant_allele").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].entrez_gene_id").value("7046"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].hugo_gene_symbol").value("TGFBR1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].sample_id").value("TCGA-AC-A23H-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].amino_acid_change").value("D400Y"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].functional_impact_score").value("H"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].xvar_link").value("getma.org/?cm=var&var=hg19,9,101908834,G,T&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].xvar_link_pdb").value("getma.org/pdb.php?prot=TGFR1_HUMAN&from=205&to=492&var=D400Y"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=TGFR1_HUMAN&rb=205&re=492&var=D400Y"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].chr").value("9"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].start_position").value("101908834"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].end_position").value("101908834"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].protein_start_position").value("400"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].protein_end_position").value("400"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].reference_allele").value("G"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].variant_allele").value("T"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_event_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_event_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].mutation_event_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].mutation_event_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[4].mutation_event_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[5].mutation_event_id").doesNotExist())
                ;
    }

    @Ignore
    @Test
    public void geneticProfileDataTest2() throws Exception {
        List<Mutation> mockResponse = getGeneticprofiledataQuery1ServiceMock().subList(0,2);
        List<DBGeneticProfile> ctMockResponse = getGeneticProfileQuery1ServiceMock();
        Mockito.when(geneticProfileMapperLegacyMock.getGeneticProfiles(
                        ArgumentMatchers.anyList()
)).thenReturn(ctMockResponse);
        Mockito.when(mutationMapperLegacy.getMutationsDetailed(
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyString()
                    )).thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/geneticprofiledata")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("genetic_profile_ids","brca_tcga_mutations")
                .param("genes","AKT1")
                .param("sample_ids","TCGA-AN-A0XR-01,TCGA-GM-A3NW-01")
                .param("sample_list_id","brca_tcga_all")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample_list_id").value("brca_tcga_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample_list_id").value("brca_tcga_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrez_gene_id").value("207"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugo_gene_symbol").value("AKT1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample_id").value("TCGA-AN-A0XR-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amino_acid_change").value("L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].functional_impact_score").value("M"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link").value("getma.org/?cm=var&var=hg19,14,105246445,A,C&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link_pdb").value("getma.org/pdb.php?prot=AKT1_HUMAN&from=6&to=108&var=L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=AKT1_HUMAN&rb=6&re=108&var=L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].chr").value("14"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start_position").value("105246445"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].end_position").value("105246445"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].protein_start_position").value("52"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].protein_end_position").value("52"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_allele").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_allele").value("C"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrez_gene_id").value("207"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugo_gene_symbol").value("AKT1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample_id").value("TCGA-GM-A3NW-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].amino_acid_change").value("V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].functional_impact_score").value("N"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link").value("getma.org/?cm=var&var=hg19,14,105258971,C,G&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link_pdb").value("getma.org/pdb.php?prot=AKT1_HUMAN&from=1&to=5&var=V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=AKT1_HUMAN&rb=1&re=35&var=V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].chr").value("14"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].start_position").value("105258971"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].end_position").value("105258971"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].protein_start_position").value("4"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].protein_end_position").value("4"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_allele").value("C"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_allele").value("G"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_event_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_event_id").doesNotExist())
                ;
    }

    @Test
    @Ignore
    public void geneticProfileDataTest3() throws Exception {
        List<Mutation> mockResponse = getGeneticprofiledataQuery1ServiceMock().subList(0,0);
        List<DBGeneticProfile> ctMockResponse = getGeneticProfileQuery1ServiceMock();
        Mockito.when(geneticProfileMapperLegacyMock.getGeneticProfiles(
                        ArgumentMatchers.anyList()
)).thenReturn(ctMockResponse);
        Mockito.when(mutationMapperLegacy.getMutationsDetailed(
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyString()
                    )).thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/geneticprofiledata")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("genetic_profile_ids","brca_tcga_mutations")
                .param("genes","unrecognized_gene_identifier")
                .param("sample_ids","")
                .param("sample_list_id","brca_tcga_all")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)))
                ;
    }

    @Ignore
    @Test
    public void geneticProfileDataTest4() throws Exception {
        List<Mutation> mockResponse = getGeneticprofiledataQuery1ServiceMock().subList(0,2);
        List<DBGeneticProfile> ctMockResponse = getGeneticProfileQuery1ServiceMock();
        Mockito.when(geneticProfileMapperLegacyMock.getGeneticProfiles(
                        ArgumentMatchers.anyList()
)).thenReturn(ctMockResponse);
        Mockito.when(mutationMapperLegacy.getMutationsDetailed(
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyList(),
                        ArgumentMatchers.anyString()
                    )).thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/geneticprofiledata")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("genetic_profile_ids","brca_tcga_mutations")
                .param("genes","AKT1")
                .param("sample_ids","TCGA-AN-A0XR-01,TCGA-GM-A3NW-01")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample_list_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample_list_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genetic_profile_id").value("brca_tcga_mutations"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].study_id").value("brca_tcga"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sequencing_center").value("genome.wustl.edu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_status").value("Somatic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_type").value("Missense_Mutation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].validation_status").value("Untested"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_read_count_tumor").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_read_count_normal").value("-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrez_gene_id").value("207"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugo_gene_symbol").value("AKT1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample_id").value("TCGA-AN-A0XR-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amino_acid_change").value("L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].functional_impact_score").value("M"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link").value("getma.org/?cm=var&var=hg19,14,105246445,A,C&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link_pdb").value("getma.org/pdb.php?prot=AKT1_HUMAN&from=6&to=108&var=L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=AKT1_HUMAN&rb=6&re=108&var=L52R"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].chr").value("14"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start_position").value("105246445"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].end_position").value("105246445"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].protein_start_position").value("52"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].protein_end_position").value("52"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reference_allele").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variant_allele").value("C"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrez_gene_id").value("207"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugo_gene_symbol").value("AKT1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample_id").value("TCGA-GM-A3NW-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].amino_acid_change").value("V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].functional_impact_score").value("N"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link").value("getma.org/?cm=var&var=hg19,14,105258971,C,G&fts=all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link_pdb").value("getma.org/pdb.php?prot=AKT1_HUMAN&from=1&to=5&var=V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].xvar_link_msa").value("getma.org/?cm=msa&ty=f&p=AKT1_HUMAN&rb=1&re=35&var=V4L"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].chr").value("14"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].start_position").value("105258971"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].end_position").value("105258971"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].protein_start_position").value("4"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].protein_end_position").value("4"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].reference_allele").value("C"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variant_allele").value("G"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutation_event_id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutation_event_id").doesNotExist())
                ;
    }

    private List<Mutation> getGeneticprofiledataQuery1PersistenceFullMock() {
        if (geneticprofiledataQuery1PersistenceFullMock != null) {
            return geneticprofiledataQuery1PersistenceFullMock;
        }
        Gene gene_AKT1 = new Gene();
        gene_AKT1.setHugoGeneSymbol("AKT1");
        gene_AKT1.setEntrezGeneId(207);
        gene_AKT1.setType("protein-coding");
        Gene gene_TGFBR1 = new Gene();
        gene_TGFBR1.setHugoGeneSymbol("TGFBR1");
        gene_TGFBR1.setEntrezGeneId(7046);
        gene_TGFBR1.setType("protein-coding");
        TypeOfCancer typeOfCancer_brca = new TypeOfCancer();
        typeOfCancer_brca.setTypeOfCancerId("brca");
        typeOfCancer_brca.setName("Invasive Breast Carcinoma");
        typeOfCancer_brca.setClinicalTrialKeywords("invasive breast carcinoma");
        typeOfCancer_brca.setDedicatedColor("HotPink");
        typeOfCancer_brca.setShortName("BRCA");
        typeOfCancer_brca.setParent("breast");
        CancerStudy cancerStudy_brca_tcga = new CancerStudy();
        cancerStudy_brca_tcga.setCancerStudyId(188);
        cancerStudy_brca_tcga.setCancerStudyIdentifier("brca_tcga");
        cancerStudy_brca_tcga.setTypeOfCancerId("brca");
        cancerStudy_brca_tcga.setTypeOfCancer(typeOfCancer_brca);
        cancerStudy_brca_tcga.setName("Breast Invasive Carcinoma (TCGA, Provisional)");
        cancerStudy_brca_tcga.setShortName("Breast (TCGA)");
        cancerStudy_brca_tcga.setDescription("TCGA Breast Invasive Carcinoma; raw data at the <A HREF=\"https://tcga-data.nci.nih.gov/\">NCI</A>.");
        cancerStudy_brca_tcga.setPublicStudy(true);
        cancerStudy_brca_tcga.setPmid(null);
        cancerStudy_brca_tcga.setCitation(null);
        cancerStudy_brca_tcga.setGroups("PUBLIC");
        cancerStudy_brca_tcga.setStatus(1);
        cancerStudy_brca_tcga.setImportDate(null);
        MolecularProfile geneticProfile_brca_tcga_mutations = new MolecularProfile();
        geneticProfile_brca_tcga_mutations.setMolecularProfileId(1010);
        geneticProfile_brca_tcga_mutations.setStableId("brca_tcga_mutations");
        geneticProfile_brca_tcga_mutations.setCancerStudyId(188);
        geneticProfile_brca_tcga_mutations.setCancerStudy(cancerStudy_brca_tcga);
        geneticProfile_brca_tcga_mutations.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        geneticProfile_brca_tcga_mutations.setDatatype("MAF");
        geneticProfile_brca_tcga_mutations.setName("Mutations");
        geneticProfile_brca_tcga_mutations.setDescription("Mutation data from whole exome sequencing.");
        geneticProfile_brca_tcga_mutations.setShowProfileInAnalysisTab(true);
        MutationEvent mutationEvent_66181 = new MutationEvent();
        mutationEvent_66181.setMutationEventId(66181);
        mutationEvent_66181.setEntrezGeneId(207);
        mutationEvent_66181.setChr("14");
        mutationEvent_66181.setStartPosition(105246445L);
        mutationEvent_66181.setEndPosition(105246445L);
        mutationEvent_66181.setReferenceAllele("A");
        mutationEvent_66181.setTumorSeqAllele("C");
        mutationEvent_66181.setProteinChange("L52R");
        mutationEvent_66181.setMutationType("Missense_Mutation");
        mutationEvent_66181.setFunctionalImpactScore("M");
        mutationEvent_66181.setFisValue(1.955f);
        mutationEvent_66181.setLinkXvar("getma.org/?cm=var&var=hg19,14,105246445,A,C&fts=all");
        mutationEvent_66181.setLinkPdb("getma.org/pdb.php?prot=AKT1_HUMAN&from=6&to=108&var=L52R");
        mutationEvent_66181.setLinkMsa("getma.org/?cm=msa&ty=f&p=AKT1_HUMAN&rb=6&re=108&var=L52R");
        mutationEvent_66181.setNcbiBuild("GRCh37");
        mutationEvent_66181.setStrand("-1");
        mutationEvent_66181.setVariantType("SNP");
        mutationEvent_66181.setDbSnpRs("NA");
        mutationEvent_66181.setDbSnpValStatus("NA");
        mutationEvent_66181.setOncotatorDbsnpRs(null);
        mutationEvent_66181.setOncotatorRefseqMrnaId("NM_001014432.1");
        mutationEvent_66181.setOncotatorCodonChange("cTc/cGc");
        mutationEvent_66181.setOncotatorUniprotEntryName("AKT1_HUMAN");
        mutationEvent_66181.setOncotatorUniprotAccession("P31749");
        mutationEvent_66181.setOncotatorProteinPosStart(52);
        mutationEvent_66181.setOncotatorProteinPosEnd(52);
        mutationEvent_66181.setCanonicalTranscript(true);
        mutationEvent_66181.setKeyword("AKT1 L52 missense");
        MutationEvent mutationEvent_98005 = new MutationEvent();
        mutationEvent_98005.setMutationEventId(98005);
        mutationEvent_98005.setEntrezGeneId(207);
        mutationEvent_98005.setChr("14");
        mutationEvent_98005.setStartPosition(105258971L);
        mutationEvent_98005.setEndPosition(105258971L);
        mutationEvent_98005.setReferenceAllele("C");
        mutationEvent_98005.setTumorSeqAllele("G");
        mutationEvent_98005.setProteinChange("V4L");
        mutationEvent_98005.setMutationType("Missense_Mutation");
        mutationEvent_98005.setFunctionalImpactScore("N");
        mutationEvent_98005.setFisValue(0.28f);
        mutationEvent_98005.setLinkXvar("getma.org/?cm=var&var=hg19,14,105258971,C,G&fts=all");
        mutationEvent_98005.setLinkPdb("getma.org/pdb.php?prot=AKT1_HUMAN&from=1&to=5&var=V4L");
        mutationEvent_98005.setLinkMsa("getma.org/?cm=msa&ty=f&p=AKT1_HUMAN&rb=1&re=35&var=V4L");
        mutationEvent_98005.setNcbiBuild("GRCh37");
        mutationEvent_98005.setStrand("-1");
        mutationEvent_98005.setVariantType("SNP");
        mutationEvent_98005.setDbSnpRs("NA");
        mutationEvent_98005.setDbSnpValStatus("NA");
        mutationEvent_98005.setOncotatorDbsnpRs(null);
        mutationEvent_98005.setOncotatorRefseqMrnaId("NM_001014432.1");
        mutationEvent_98005.setOncotatorCodonChange("Gtg/Ctg");
        mutationEvent_98005.setOncotatorUniprotEntryName("AKT1_HUMAN");
        mutationEvent_98005.setOncotatorUniprotAccession("P31749");
        mutationEvent_98005.setOncotatorProteinPosStart(4);
        mutationEvent_98005.setOncotatorProteinPosEnd(4);
        mutationEvent_98005.setCanonicalTranscript(true);
        mutationEvent_98005.setKeyword("AKT1 V4 missense");
        MutationEvent mutationEvent_57265 = new MutationEvent();
        mutationEvent_57265.setMutationEventId(57265);
        mutationEvent_57265.setEntrezGeneId(7046);
        mutationEvent_57265.setChr("9");
        mutationEvent_57265.setStartPosition(101908834L);
        mutationEvent_57265.setEndPosition(101908834L);
        mutationEvent_57265.setReferenceAllele("G");
        mutationEvent_57265.setTumorSeqAllele("T");
        mutationEvent_57265.setProteinChange("D400Y");
        mutationEvent_57265.setMutationType("Missense_Mutation");
        mutationEvent_57265.setFunctionalImpactScore("H");
        mutationEvent_57265.setFisValue(5.095f);
        mutationEvent_57265.setLinkXvar("getma.org/?cm=var&var=hg19,9,101908834,G,T&fts=all");
        mutationEvent_57265.setLinkPdb("getma.org/pdb.php?prot=TGFR1_HUMAN&from=205&to=492&var=D400Y");
        mutationEvent_57265.setLinkMsa("getma.org/?cm=msa&ty=f&p=TGFR1_HUMAN&rb=205&re=492&var=D400Y");
        mutationEvent_57265.setNcbiBuild("GRCh37");
        mutationEvent_57265.setStrand("1");
        mutationEvent_57265.setVariantType("SNP");
        mutationEvent_57265.setDbSnpRs("NA");
        mutationEvent_57265.setDbSnpValStatus("NA");
        mutationEvent_57265.setOncotatorDbsnpRs(null);
        mutationEvent_57265.setOncotatorRefseqMrnaId("NM_004612.2");
        mutationEvent_57265.setOncotatorCodonChange("Gac/Tac");
        mutationEvent_57265.setOncotatorUniprotEntryName("TGFR1_HUMAN");
        mutationEvent_57265.setOncotatorUniprotAccession("P36897");
        mutationEvent_57265.setOncotatorProteinPosStart(400);
        mutationEvent_57265.setOncotatorProteinPosEnd(400);
        mutationEvent_57265.setCanonicalTranscript(true);
        mutationEvent_57265.setKeyword("TGFBR1 D400 missense");
        MutationEvent mutationEvent_69018 = new MutationEvent();
        mutationEvent_69018.setMutationEventId(69018);
        mutationEvent_69018.setEntrezGeneId(7046);
        mutationEvent_69018.setChr("9");
        mutationEvent_69018.setStartPosition(101900249L);
        mutationEvent_69018.setEndPosition(101900249L);
        mutationEvent_69018.setReferenceAllele("A");
        mutationEvent_69018.setTumorSeqAllele("T");
        mutationEvent_69018.setProteinChange("E228V");
        mutationEvent_69018.setMutationType("Missense_Mutation");
        mutationEvent_69018.setFunctionalImpactScore("N");
        mutationEvent_69018.setFisValue(0.55f);
        mutationEvent_69018.setLinkXvar("getma.org/?cm=var&var=hg19,9,101900249,A,T&fts=all");
        mutationEvent_69018.setLinkPdb("getma.org/pdb.php?prot=TGFR1_HUMAN&from=205&to=492&var=E228V");
        mutationEvent_69018.setLinkMsa("getma.org/?cm=msa&ty=f&p=TGFR1_HUMAN&rb=205&re=492&var=E228V");
        mutationEvent_69018.setNcbiBuild("GRCh37");
        mutationEvent_69018.setStrand("1");
        mutationEvent_69018.setVariantType("SNP");
        mutationEvent_69018.setDbSnpRs("NA");
        mutationEvent_69018.setDbSnpValStatus("NA");
        mutationEvent_69018.setOncotatorDbsnpRs(null);
        mutationEvent_69018.setOncotatorRefseqMrnaId("NM_004612.2");
        mutationEvent_69018.setOncotatorCodonChange("gAa/gTa");
        mutationEvent_69018.setOncotatorUniprotEntryName("TGFR1_HUMAN");
        mutationEvent_69018.setOncotatorUniprotAccession("P36897");
        mutationEvent_69018.setOncotatorProteinPosStart(228);
        mutationEvent_69018.setOncotatorProteinPosEnd(228);
        mutationEvent_69018.setCanonicalTranscript(true);
        mutationEvent_69018.setKeyword("TGFBR1 E228 missense");
        MutationEvent mutationEvent_71879 = new MutationEvent();
        mutationEvent_71879.setMutationEventId(71879);
        mutationEvent_71879.setEntrezGeneId(7046);
        mutationEvent_71879.setChr("9");
        mutationEvent_71879.setStartPosition(101891278L);
        mutationEvent_71879.setEndPosition(101891278L);
        mutationEvent_71879.setReferenceAllele("G");
        mutationEvent_71879.setTumorSeqAllele("A");
        mutationEvent_71879.setProteinChange("R80Q");
        mutationEvent_71879.setMutationType("Missense_Mutation");
        mutationEvent_71879.setFunctionalImpactScore("L");
        mutationEvent_71879.setFisValue(1.01f);
        mutationEvent_71879.setLinkXvar("getma.org/?cm=var&var=hg19,9,101891278,G,A&fts=all");
        mutationEvent_71879.setLinkPdb("getma.org/pdb.php?prot=TGFR1_HUMAN&from=34&to=114&var=R80Q");
        mutationEvent_71879.setLinkMsa("getma.org/?cm=msa&ty=f&p=TGFR1_HUMAN&rb=34&re=114&var=R80Q");
        mutationEvent_71879.setNcbiBuild("GRCh37");
        mutationEvent_71879.setStrand("1");
        mutationEvent_71879.setVariantType("SNP");
        mutationEvent_71879.setDbSnpRs("NA");
        mutationEvent_71879.setDbSnpValStatus("NA");
        mutationEvent_71879.setOncotatorDbsnpRs(null);
        mutationEvent_71879.setOncotatorRefseqMrnaId("NM_004612.2");
        mutationEvent_71879.setOncotatorCodonChange("cGa/cAa");
        mutationEvent_71879.setOncotatorUniprotEntryName("TGFR1_HUMAN");
        mutationEvent_71879.setOncotatorUniprotAccession("P36897");
        mutationEvent_71879.setOncotatorProteinPosStart(80);
        mutationEvent_71879.setOncotatorProteinPosEnd(80);
        mutationEvent_71879.setCanonicalTranscript(true);
        mutationEvent_71879.setKeyword("TGFBR1 R80 missense");
        MutationEvent mutationEvent_74462 = new MutationEvent();
        mutationEvent_74462.setMutationEventId(74462);
        mutationEvent_74462.setEntrezGeneId(7046);
        mutationEvent_74462.setChr("9");
        mutationEvent_74462.setStartPosition(101908824L);
        mutationEvent_74462.setEndPosition(101908824L);
        mutationEvent_74462.setReferenceAllele("C");
        mutationEvent_74462.setTumorSeqAllele("G");
        mutationEvent_74462.setProteinChange("F396L");
        mutationEvent_74462.setMutationType("Missense_Mutation");
        mutationEvent_74462.setFunctionalImpactScore("M");
        mutationEvent_74462.setFisValue(2.075f);
        mutationEvent_74462.setLinkXvar("getma.org/?cm=var&var=hg19,9,101908824,C,G&fts=all");
        mutationEvent_74462.setLinkPdb("getma.org/pdb.php?prot=TGFR1_HUMAN&from=205&to=492&var=F396L");
        mutationEvent_74462.setLinkMsa("getma.org/?cm=msa&ty=f&p=TGFR1_HUMAN&rb=205&re=492&var=F396L");
        mutationEvent_74462.setNcbiBuild("GRCh37");
        mutationEvent_74462.setStrand("1");
        mutationEvent_74462.setVariantType("SNP");
        mutationEvent_74462.setDbSnpRs("NA");
        mutationEvent_74462.setDbSnpValStatus("NA");
        mutationEvent_74462.setOncotatorDbsnpRs(null);
        mutationEvent_74462.setOncotatorRefseqMrnaId("NM_004612.2");
        mutationEvent_74462.setOncotatorCodonChange("ttC/ttG");
        mutationEvent_74462.setOncotatorUniprotEntryName("TGFR1_HUMAN");
        mutationEvent_74462.setOncotatorUniprotAccession("P36897");
        mutationEvent_74462.setOncotatorProteinPosStart(396);
        mutationEvent_74462.setOncotatorProteinPosEnd(396);
        mutationEvent_74462.setCanonicalTranscript(true);
        mutationEvent_74462.setKeyword("TGFBR1 F396 missense");
        Patient patient_59233 = new Patient();
        patient_59233.setInternalId(59233);
        patient_59233.setStableId("TCGA-AC-A23H");
        patient_59233.setCancerStudyId(188);
        patient_59233.setCancerStudy(cancerStudy_brca_tcga);
        Patient patient_59311 = new Patient();
        patient_59311.setInternalId(59311);
        patient_59311.setStableId("TCGA-AN-A0XR");
        patient_59311.setCancerStudyId(188);
        patient_59311.setCancerStudy(cancerStudy_brca_tcga);
        Patient patient_59353 = new Patient();
        patient_59353.setInternalId(59353);
        patient_59353.setStableId("TCGA-AO-A12");
        patient_59353.setCancerStudyId(188);
        patient_59353.setCancerStudy(cancerStudy_brca_tcga);
        Patient patient_59429 = new Patient();
        patient_59429.setInternalId(59429);
        patient_59429.setStableId("TCGA-AR-A2L");
        patient_59429.setCancerStudyId(188);
        patient_59429.setCancerStudy(cancerStudy_brca_tcga);
        Patient patient_59510 = new Patient();
        patient_59510.setInternalId(59510);
        patient_59510.setStableId("TCGA-BH-A0B");
        patient_59510.setCancerStudyId(188);
        patient_59510.setCancerStudy(cancerStudy_brca_tcga);
        Patient patient_59988 = new Patient();
        patient_59988.setInternalId(59988);
        patient_59988.setStableId("TCGA-GM-A3N");
        patient_59988.setCancerStudyId(188);
        patient_59988.setCancerStudy(cancerStudy_brca_tcga);
        Sample sample_59935 = new Sample();
        sample_59935.setInternalId(59935);
        sample_59935.setStableId("TCGA-AC-A23H-01");
        sample_59935.setSampleType(SampleType.PRIMARY_SOLID_TUMOR);
        sample_59935.setPatientId(59233);
        sample_59935.setPatient(patient_59233);
        Sample sample_60046 = new Sample();
        sample_60046.setInternalId(60046);
        sample_60046.setStableId("TCGA-AR-A2LE-01");
        sample_60046.setSampleType(SampleType.PRIMARY_SOLID_TUMOR);
        sample_60046.setPatientId(59429);
        sample_60046.setPatient(patient_59429);
        Sample sample_60123 = new Sample();
        sample_60123.setInternalId(60123);
        sample_60123.setStableId("TCGA-GM-A3NW-01");
        sample_60123.setSampleType(SampleType.PRIMARY_SOLID_TUMOR);
        sample_60123.setPatientId(59988);
        sample_60123.setPatient(patient_59988);
        Sample sample_60627 = new Sample();
        sample_60627.setInternalId(60627);
        sample_60627.setStableId("TCGA-AN-A0XR-01");
        sample_60627.setSampleType(SampleType.PRIMARY_SOLID_TUMOR);
        sample_60627.setPatientId(59311);
        sample_60627.setPatient(patient_59311);
        Sample sample_60693 = new Sample();
        sample_60693.setInternalId(60693);
        sample_60693.setStableId("TCGA-AO-A12D-01");
        sample_60693.setSampleType(SampleType.PRIMARY_SOLID_TUMOR);
        sample_60693.setPatientId(59353);
        sample_60693.setPatient(patient_59353);
        Sample sample_60831 = new Sample();
        sample_60831.setInternalId(60831);
        sample_60831.setStableId("TCGA-BH-A0B5-01");
        sample_60831.setSampleType(SampleType.PRIMARY_SOLID_TUMOR);
        sample_60831.setPatientId(59510);
        sample_60831.setPatient(patient_59510);
        geneticprofiledataQuery1PersistenceFullMock = new ArrayList<Mutation>();
        Mutation mutation1 = new Mutation();
        mutation1.setMutationEventId(66181);
        mutation1.setMutationEvent(mutationEvent_66181);
        mutation1.setGeneticProfileId(1010);
        mutation1.setGeneticProfile(geneticProfile_brca_tcga_mutations);
        mutation1.setSampleId(60627);
        mutation1.setSample(sample_60627);
        mutation1.setEntrezGeneId(207);
        mutation1.setGene(gene_AKT1);
        mutation1.setCenter("genome.wustl.edu");
        mutation1.setSequencer("Illumina GAIIx");
        mutation1.setMutationStatus("Somatic");
        mutation1.setValidationStatus("Untested");
        mutation1.setTumorSeqAllele1("A");
        mutation1.setTumorSeqAllele2("C");
        mutation1.setMatchedNormSampleBarcode("TCGA-AN-A0XR-10");
        mutation1.setMatchNormSeqAllele1("A");
        mutation1.setMatchNormSeqAllele2("A");
        mutation1.setTumorValidationAllele1("NA");
        mutation1.setTumorValidationAllele2("NA");
        mutation1.setMatchNormValidationAllele1("NA");
        mutation1.setMatchNormValidationAllele2("NA");
        mutation1.setVerificationStatus("Unknown");
        mutation1.setSequencingPhase("Phase_IV");
        mutation1.setSequenceSource("WXS");
        mutation1.setValidationMethod("none");
        mutation1.setScore("1");
        mutation1.setBamFile("dbGAP");
        mutation1.setTumorAltCount(-1);
        mutation1.setTumorRefCount(-1);
        mutation1.setNormalAltCount(-1);
        mutation1.setNormalRefCount(-1);
        mutation1.setAminoAcidChange(null);
        geneticprofiledataQuery1PersistenceFullMock.add(mutation1);
        Mutation mutation2 = new Mutation();
        mutation2.setMutationEventId(98005);
        mutation2.setMutationEvent(mutationEvent_98005);
        mutation2.setGeneticProfileId(1010);
        mutation2.setGeneticProfile(geneticProfile_brca_tcga_mutations);
        mutation2.setSampleId(60123);
        mutation2.setSample(sample_60123);
        mutation2.setEntrezGeneId(207);
        mutation2.setGene(gene_AKT1);
        mutation2.setCenter("genome.wustl.edu");
        mutation2.setSequencer("Illumina GAIIx");
        mutation2.setMutationStatus("Somatic");
        mutation2.setValidationStatus("Untested");
        mutation2.setTumorSeqAllele1("C");
        mutation2.setTumorSeqAllele2("G");
        mutation2.setMatchedNormSampleBarcode("TCGA-GM-A3NW-10");
        mutation2.setMatchNormSeqAllele1("C");
        mutation2.setMatchNormSeqAllele2("C");
        mutation2.setTumorValidationAllele1("NA");
        mutation2.setTumorValidationAllele2("NA");
        mutation2.setMatchNormValidationAllele1("NA");
        mutation2.setMatchNormValidationAllele2("NA");
        mutation2.setVerificationStatus("Unknown");
        mutation2.setSequencingPhase("Phase_IV");
        mutation2.setSequenceSource("WXS");
        mutation2.setValidationMethod("none");
        mutation2.setScore("1");
        mutation2.setBamFile("dbGAP");
        mutation1.setTumorAltCount(-1);
        mutation1.setTumorRefCount(-1);
        mutation1.setNormalAltCount(-1);
        mutation1.setNormalRefCount(-1);
        mutation2.setAminoAcidChange(null);
        geneticprofiledataQuery1PersistenceFullMock.add(mutation2);
        Mutation mutation3 = new Mutation();
        mutation3.setMutationEventId(69018);
        mutation3.setMutationEvent(mutationEvent_69018);
        mutation3.setGeneticProfileId(1010);
        mutation3.setGeneticProfile(geneticProfile_brca_tcga_mutations);
        mutation3.setSampleId(60693);
        mutation3.setSample(sample_60693);
        mutation3.setEntrezGeneId(7046);
        mutation3.setGene(gene_TGFBR1);
        mutation3.setCenter("genome.wustl.edu");
        mutation3.setSequencer("Illumina GAIIx");
        mutation3.setMutationStatus("Somatic");
        mutation3.setValidationStatus("Untested");
        mutation3.setTumorSeqAllele1("A");
        mutation3.setTumorSeqAllele2("T");
        mutation3.setMatchedNormSampleBarcode("TCGA-AO-A12D-10");
        mutation3.setMatchNormSeqAllele1("A");
        mutation3.setMatchNormSeqAllele2("A");
        mutation3.setTumorValidationAllele1("NA");
        mutation3.setTumorValidationAllele2("NA");
        mutation3.setMatchNormValidationAllele1("NA");
        mutation3.setMatchNormValidationAllele2("NA");
        mutation3.setVerificationStatus("Unknown");
        mutation3.setSequencingPhase("Phase_IV");
        mutation3.setSequenceSource("WXS");
        mutation3.setValidationMethod("none");
        mutation3.setScore("1");
        mutation3.setBamFile("dbGAP");
        mutation1.setTumorAltCount(-1);
        mutation1.setTumorRefCount(-1);
        mutation1.setNormalAltCount(-1);
        mutation1.setNormalRefCount(-1);
        mutation3.setAminoAcidChange(null);
        geneticprofiledataQuery1PersistenceFullMock.add(mutation3);
        Mutation mutation4 = new Mutation();
        mutation4.setMutationEventId(74462);
        mutation4.setMutationEvent(mutationEvent_74462);
        mutation4.setGeneticProfileId(1010);
        mutation4.setGeneticProfile(geneticProfile_brca_tcga_mutations);
        mutation4.setSampleId(60831);
        mutation4.setSample(sample_60831);
        mutation4.setEntrezGeneId(7046);
        mutation4.setGene(gene_TGFBR1);
        mutation4.setCenter("genome.wustl.edu");
        mutation4.setSequencer("Illumina GAIIx");
        mutation4.setMutationStatus("Somatic");
        mutation4.setValidationStatus("Untested");
        mutation4.setTumorSeqAllele1("C");
        mutation4.setTumorSeqAllele2("G");
        mutation4.setMatchedNormSampleBarcode("TCGA-BH-A0B5-11");
        mutation4.setMatchNormSeqAllele1("C");
        mutation4.setMatchNormSeqAllele2("C");
        mutation4.setTumorValidationAllele1("NA");
        mutation4.setTumorValidationAllele2("NA");
        mutation4.setMatchNormValidationAllele1("NA");
        mutation4.setMatchNormValidationAllele2("NA");
        mutation4.setVerificationStatus("Unknown");
        mutation4.setSequencingPhase("Phase_IV");
        mutation4.setSequenceSource("WXS");
        mutation4.setValidationMethod("none");
        mutation4.setScore("1");
        mutation4.setBamFile("dbGAP");
        mutation1.setTumorAltCount(-1);
        mutation1.setTumorRefCount(-1);
        mutation1.setNormalAltCount(-1);
        mutation1.setNormalRefCount(-1);
        mutation4.setAminoAcidChange(null);
        geneticprofiledataQuery1PersistenceFullMock.add(mutation4);
        Mutation mutation5 = new Mutation();
        mutation5.setMutationEventId(71879);
        mutation5.setMutationEvent(mutationEvent_71879);
        mutation5.setGeneticProfileId(1010);
        mutation5.setGeneticProfile(geneticProfile_brca_tcga_mutations);
        mutation5.setSampleId(60046);
        mutation5.setSample(sample_60046);
        mutation5.setEntrezGeneId(7046);
        mutation5.setGene(gene_TGFBR1);
        mutation5.setCenter("genome.wustl.edu");
        mutation5.setSequencer("Illumina GAIIx");
        mutation5.setMutationStatus("Somatic");
        mutation5.setValidationStatus("Untested");
        mutation5.setTumorSeqAllele1("G");
        mutation5.setTumorSeqAllele2("A");
        mutation5.setMatchedNormSampleBarcode("TCGA-AR-A2LE-10");
        mutation5.setMatchNormSeqAllele1("G");
        mutation5.setMatchNormSeqAllele2("G");
        mutation5.setTumorValidationAllele1("NA");
        mutation5.setTumorValidationAllele2("NA");
        mutation5.setMatchNormValidationAllele1("NA");
        mutation5.setMatchNormValidationAllele2("NA");
        mutation5.setVerificationStatus("Unknown");
        mutation5.setSequencingPhase("Phase_IV");
        mutation5.setSequenceSource("WXS");
        mutation5.setValidationMethod("none");
        mutation5.setScore("1");
        mutation5.setBamFile("dbGAP");
        mutation1.setTumorAltCount(-1);
        mutation1.setTumorRefCount(-1);
        mutation1.setNormalAltCount(-1);
        mutation1.setNormalRefCount(-1);
        mutation5.setAminoAcidChange(null);
        geneticprofiledataQuery1PersistenceFullMock.add(mutation5);
        Mutation mutation6 = new Mutation();
        mutation6.setMutationEventId(57265);
        mutation6.setMutationEvent(mutationEvent_57265);
        mutation6.setGeneticProfileId(1010);
        mutation6.setGeneticProfile(geneticProfile_brca_tcga_mutations);
        mutation6.setSampleId(59935);
        mutation6.setSample(sample_59935);
        mutation6.setEntrezGeneId(7046);
        mutation6.setGene(gene_TGFBR1);
        mutation6.setCenter("genome.wustl.edu");
        mutation6.setSequencer("Illumina GAIIx");
        mutation6.setMutationStatus("Somatic");
        mutation6.setValidationStatus("Untested");
        mutation6.setTumorSeqAllele1("G");
        mutation6.setTumorSeqAllele2("T");
        mutation6.setMatchedNormSampleBarcode("TCGA-AC-A23H-11");
        mutation6.setMatchNormSeqAllele1("G");
        mutation6.setMatchNormSeqAllele2("G");
        mutation6.setTumorValidationAllele1("NA");
        mutation6.setTumorValidationAllele2("NA");
        mutation6.setMatchNormValidationAllele1("NA");
        mutation6.setMatchNormValidationAllele2("NA");
        mutation6.setVerificationStatus("Unknown");
        mutation6.setSequencingPhase("Phase_IV");
        mutation6.setSequenceSource("WXS");
        mutation6.setValidationMethod("none");
        mutation6.setScore("1");
        mutation6.setBamFile("dbGAP");
        mutation1.setTumorAltCount(-1);
        mutation1.setTumorRefCount(-1);
        mutation1.setNormalAltCount(-1);
        mutation1.setNormalRefCount(-1);
        mutation6.setAminoAcidChange(null);
        geneticprofiledataQuery1PersistenceFullMock.add(mutation6);
        return geneticprofiledataQuery1PersistenceFullMock;
    }

    private void applyNullMaskToMutationModel(Mutation mutation) {
        mutation.setMutationEventId(null);
        mutation.setEntrezGeneId(null);
        mutation.setSequencer(null);
        mutation.setTumorSeqAllele1(null);
        mutation.setTumorSeqAllele2(null);
        mutation.setMatchedNormSampleBarcode(null);
        mutation.setMatchNormSeqAllele1(null);
        mutation.setMatchNormSeqAllele2(null);
        mutation.setTumorValidationAllele1(null);
        mutation.setTumorValidationAllele2(null);
        mutation.setMatchNormValidationAllele1(null);
        mutation.setMatchNormValidationAllele2(null);
        mutation.setVerificationStatus(null);
        mutation.setSequencingPhase(null);
        mutation.setSequenceSource(null);
        mutation.setValidationMethod(null);
        mutation.setScore(null);
        mutation.setBamFile(null);
        mutation.setAminoAcidChange(null);
        mutation.getMutationEvent().setMutationEventId(null);
        mutation.getMutationEvent().setEntrezGeneId(null);
        mutation.getMutationEvent().setFisValue(null);
        mutation.getMutationEvent().setNcbiBuild(null);
        mutation.getMutationEvent().setStrand(null);
        mutation.getMutationEvent().setVariantType(null);
        mutation.getMutationEvent().setDbSnpRs(null);
        mutation.getMutationEvent().setDbSnpValStatus(null);
        mutation.getMutationEvent().setOncotatorDbsnpRs(null);
        mutation.getMutationEvent().setOncotatorRefseqMrnaId(null);
        mutation.getMutationEvent().setOncotatorCodonChange(null);
        mutation.getMutationEvent().setOncotatorUniprotEntryName(null);
        mutation.getMutationEvent().setOncotatorUniprotAccession(null);
        mutation.getMutationEvent().setCanonicalTranscript(null);
        mutation.getMutationEvent().setKeyword(null);
        mutation.getGeneticProfile().setMolecularProfileId(null);
        mutation.getGeneticProfile().setStableId(null);
        mutation.getGeneticProfile().setCancerStudy(null);
        mutation.getGeneticProfile().setMolecularAlterationType(null);
        mutation.getGeneticProfile().setDatatype(null);
        mutation.getGeneticProfile().setName(null);
        mutation.getGeneticProfile().setDescription(null);
        mutation.getGeneticProfile().setShowProfileInAnalysisTab(null);
        mutation.setSample(null); //class object
        mutation.getGene().setType(null);
    }

    private List<Mutation> getGeneticprofiledataQuery1ServiceMock() {
        if (geneticprofiledataQuery1ServiceMock != null) {
            return geneticprofiledataQuery1ServiceMock;
        }
        List<Mutation> fullMock = this.getGeneticprofiledataQuery1PersistenceFullMock();
        geneticprofiledataQuery1ServiceMock = new ArrayList<Mutation>(fullMock.size());
        for (Mutation fullMutation : fullMock) {
            Mutation clone = (Mutation)SerializationUtils.clone(fullMutation);
            this.applyNullMaskToMutationModel(clone);
            geneticprofiledataQuery1ServiceMock.add(clone);
        }
        return geneticprofiledataQuery1ServiceMock;
    }

    private List<DBGeneticProfile> getGeneticProfileQuery1ServiceMock() {
        if (geneticProfileQuery1ServiceMock != null) {
            return geneticProfileQuery1ServiceMock;
        }
        geneticProfileQuery1ServiceMock = new ArrayList<DBGeneticProfile>();
        DBGeneticProfile gp1 = new DBGeneticProfile();
        gp1.id = "brca_tcga_mutations";
        gp1.name = "Mutations";
        gp1.description = "Mutation data from whole exome sequencing.";
        gp1.datatype = "MAF";
        gp1.study_id = "188";
        gp1.genetic_alteration_type = "MUTATION_EXTENDED";
        gp1.show_profile_in_analysis_tab = "1";
        geneticProfileQuery1ServiceMock.add(gp1);
        return geneticProfileQuery1ServiceMock;
    }
}
