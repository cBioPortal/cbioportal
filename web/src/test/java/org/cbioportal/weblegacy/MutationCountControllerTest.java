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

package org.cbioportal.weblegacy;

import java.util.LinkedList;
import java.util.List;
import org.cbioportal.model.PositionMutationCount;
import org.cbioportal.service.MutationCountService;
import org.cbioportal.web.config.CustomObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

/**
 *
 * @author abeshoua
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {MutationCountControllerConfig.class, CustomObjectMapper.class})
public class MutationCountControllerTest {
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private MutationCountService mutationCountServiceMock;
	private MockMvc mockMvc;
	
	private PositionMutationCount pmc1;
	
	@Before
	public void setup() {
		Mockito.reset(mutationCountServiceMock);
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		pmc1 = new PositionMutationCount();
		pmc1.setHugoGeneSymbol("BRAF");
		pmc1.setCodonPosition(600);
		pmc1.setMutationCount(523);
	}
	
	@Test
	public void positionMutationCounts() throws Exception {
		List<PositionMutationCount> mockResponse = new LinkedList<>();
		mockResponse.add(pmc1);
		
		Mockito.when(mutationCountServiceMock.getPositionMutationCounts(org.mockito.Matchers.anyList(), org.mockito.Matchers.anyList())).thenReturn(mockResponse);
		
		this.mockMvc.perform(
			MockMvcRequestBuilders.get("/position-mutation-count")
			.accept(MediaType.parseMediaType("application/json; charset=UTF-8"))
			.param("genes", new String[4])
			.param("positions", new String[4]))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
			.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value("BRAF"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].codonPosition").value(600))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationCount").value(523));
	}
	
}
