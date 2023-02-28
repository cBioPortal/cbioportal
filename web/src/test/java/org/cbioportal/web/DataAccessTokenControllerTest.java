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

package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.TokenNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext-web.xml", "/applicationContext-security-test.xml"})
@WebAppConfiguration
public class DataAccessTokenControllerTest {

    public static final String MOCK_USER = "MOCK_USER";
    public static final String MOCK_PASSWORD = "MOCK_PASSWORD";
    public static final String VALID_TOKEN_STRING = "VALID_TOKEN";
    public static final String NONEXISTENT_TOKEN_STRING = "NONEXISTENT_TOKEN";
    public static final String NOT_FOUND_ERROR_MESSAGE = "Specified token cannot be found";
    public static final DataAccessToken MOCK_TOKEN_INFO = new DataAccessToken(VALID_TOKEN_STRING);

    public String receivedArgument = null;

    @Bean
    public DataAccessTokenService tokenService() {
        DataAccessTokenService tokenService = Mockito.mock(DataAccessTokenService.class);
        return tokenService;
    }

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private DataAccessTokenService tokenService;

    @Autowired
    private FilterChainProxy filterChainProxy;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilter(filterChainProxy).build();
    }

    public void resetReceivedArgument() {
        this.receivedArgument = null;
    }

    private HttpSession getSession(String user, String password) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/j_spring_security_check")
            .param("j_username", user)
            .param("j_password", password))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn()
        .getRequest()
        .getSession();
    }
    /* Tests mapping for GET /data-access-tokens/{token}
     * Test for valid token - checks returned response type is 200 success
     */
    @Test
    public void getTokenInfoForValidTokenTest() throws Exception {
        Mockito.when(tokenService.getDataAccessTokenInfo(VALID_TOKEN_STRING)).thenReturn(MOCK_TOKEN_INFO);
        HttpSession session = getSession(MOCK_USER, MOCK_PASSWORD);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/data-access-tokens/" + VALID_TOKEN_STRING)
            .session((MockHttpSession) session)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
    }

    /* Tests mapping for GET /data-access-tokens/{token}
     * Test for nonexistent token - checks returned response type is 404 not found
     * Checks response for correct error message
     */
    @Test
    public void getTokenInfoForNonexistentTokenTest() throws Exception {
        Mockito.doThrow(new TokenNotFoundException()).when(tokenService).getDataAccessTokenInfo(NONEXISTENT_TOKEN_STRING);
        HttpSession session = getSession(MOCK_USER, MOCK_PASSWORD);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/data-access-tokens/" + NONEXISTENT_TOKEN_STRING)
            .session((MockHttpSession) session)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andReturn();
        if (!result.getResponse().getContentAsString().contains(NOT_FOUND_ERROR_MESSAGE)) {
            Assert.fail("Returned response did not contain expected error message. Expected response: '" + NOT_FOUND_ERROR_MESSAGE + "' Returned response: '" + result.getResponse().getContentAsString() + "'");
        }
    }

    /* Tests mapping for DELETE /data-access-tokens/{token}
     * Test that proper service method was called
     */
    @Test
    public void revokeValidTokenTest() throws Exception {
        resetReceivedArgument();
        Answer<Void> tokenServiceRevokeTokenAnswer = new Answer<Void>() {
            public Void answer(InvocationOnMock revokeTokenInvocation) {
                receivedArgument = (String)revokeTokenInvocation.getArguments()[0];
                return null;
            }
        };
        Mockito.doAnswer(tokenServiceRevokeTokenAnswer).when(tokenService).revokeDataAccessToken(ArgumentMatchers.anyString());
        HttpSession session = getSession(MOCK_USER, MOCK_PASSWORD);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/data-access-tokens/" + VALID_TOKEN_STRING)
            .session((MockHttpSession) session)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        if (!receivedArgument.equals(VALID_TOKEN_STRING)) {
            Assert.fail("Unexpected argument passed to service class. Expected argument: " + VALID_TOKEN_STRING + " Received argument: " + receivedArgument);
        }
    }

    /* Tests mapping for GET /data-access-tokens/{token}
     * Test for nonexistent token - checks returned response type is 404 not found
     * Checks response for correct error message
     */
    @Test
    public void revokeNonexistentTokenTest() throws Exception {
        resetReceivedArgument();
        Mockito.doThrow(new TokenNotFoundException()).when(tokenService).revokeDataAccessToken(NONEXISTENT_TOKEN_STRING);;
        HttpSession session = getSession(MOCK_USER, MOCK_PASSWORD);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/data-access-tokens/" + NONEXISTENT_TOKEN_STRING)
            .session((MockHttpSession) session)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andReturn();
        if (!result.getResponse().getContentAsString().contains(NOT_FOUND_ERROR_MESSAGE)) {
            Assert.fail("Returned response did not contain expected error message. Expected response: '" + NOT_FOUND_ERROR_MESSAGE + "' Returned response: '" + result.getResponse().getContentAsString() + "'");
        }
    }

    /* Tests mapping for POST /data-access-tokens
     * Tests for 201 (CREATED) response code
     */
    @Test
    public void createTokenValidUserTest() throws Exception {
        Mockito.when(tokenService.createDataAccessToken(ArgumentMatchers.anyString())).thenReturn(MOCK_TOKEN_INFO);
        HttpSession session = getSession(MOCK_USER, MOCK_PASSWORD);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/data-access-tokens")
            .session((MockHttpSession) session)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andReturn();
    }

    /* Tests mapping for DELETE /data-access-tokens
     * Checks response status code is 200 success
     * Checks that correct username argument is passed to service class
     */
    @Test
    public void revokeAllTokensForUserTest() throws Exception {
        resetReceivedArgument();
        Answer<Void> tokenServiceRevokeAllTokensAnswer = new Answer<Void>() {
            public Void answer(InvocationOnMock revokeAllTokensInvocation) {
                receivedArgument = (String)revokeAllTokensInvocation.getArguments()[0];
                return null;
            }
        };
        Mockito.doAnswer(tokenServiceRevokeAllTokensAnswer).when(tokenService).revokeAllDataAccessTokens(ArgumentMatchers.anyString());
        HttpSession session = getSession(MOCK_USER, MOCK_PASSWORD);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/data-access-tokens")
            .session((MockHttpSession) session)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        if(!receivedArgument.equals(MOCK_USER)) {
            Assert.fail("Unexpected argument passed to service class. Expected argument: " + MOCK_USER + " Received argument: " + receivedArgument);
        }
    }

    /* Tests mapping for GET /data-access-tokens
     * Checks response status code is 200 success
     * Checks that correct username argument is passed to service class
     */
    @Test
    public void getAllTokensForUserTest() throws Exception {
        resetReceivedArgument();
        Answer<Void> tokenServiceGetAllTokensAnswer = new Answer<Void>() {
            public Void answer(InvocationOnMock getAllTokensInvocation) {
                receivedArgument = (String)getAllTokensInvocation.getArguments()[0];
                return null;
            }
        };
        Mockito.doAnswer(tokenServiceGetAllTokensAnswer).when(tokenService).getAllDataAccessTokens(ArgumentMatchers.anyString());
        HttpSession session = getSession(MOCK_USER, MOCK_PASSWORD);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/data-access-tokens")
            .session((MockHttpSession) session)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        if(!receivedArgument.equals(MOCK_USER)) {
            Assert.fail("Unexpected argument passed to service class. Expected argument: " + MOCK_USER + " Received argument: " + receivedArgument);
        }
    }
}
