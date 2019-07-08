package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.DataAccessToken;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.concurrent.TimeUnit;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class DataAccessTokenMyBatisRepositoryTest {

    @Autowired
    private DataAccessTokenMyBatisRepository dataAccessTokenMyBatisRepository;

    @Test
    public void getAllDataAccessTokensForUsername() {
        List<DataAccessToken> dataAccessTokensForMockEmail = dataAccessTokenMyBatisRepository.getAllDataAccessTokensForUsername("mockemail@email.com");
        Assert.assertEquals(1, dataAccessTokensForMockEmail.size());
        List<DataAccessToken> dataAccessTokensForMockEmail3= dataAccessTokenMyBatisRepository.getAllDataAccessTokensForUsername("mockemail3@email.com");
        Assert.assertEquals(3, dataAccessTokensForMockEmail3.size());
        for (DataAccessToken dataAccessToken : dataAccessTokensForMockEmail3) {
            Assert.assertEquals("mockemail3@email.com", dataAccessToken.getUsername());
        }
    }
    
    @Test
    public void getDataAccessToken() {
        DataAccessToken dataAccessToken = dataAccessTokenMyBatisRepository.getDataAccessToken("6c9a641e-9719-fake-data-f17e089b37e8");
        Assert.assertEquals("6c9a641e-9719-fake-data-f17e089b37e8", dataAccessToken.getToken());
        Assert.assertEquals("mockemail2@email.com", dataAccessToken.getUsername());        
    }
    
    @Test
    @Transactional
    public void addDataAccessToken() {
        String uuid = UUID.randomUUID().toString();
        Calendar calendar = Calendar.getInstance();
        Date creationDate = calendar.getTime();
        calendar.add(Calendar.SECOND, 1000);
        Date expirationDate = calendar.getTime();
        
        DataAccessToken dataAccessToken = new DataAccessToken(uuid, "mockemail2@email.com", expirationDate, creationDate);
        dataAccessTokenMyBatisRepository.addDataAccessToken(dataAccessToken);
        
        DataAccessToken newDataAccessToken = dataAccessTokenMyBatisRepository.getDataAccessToken(uuid);
        Assert.assertEquals(uuid, newDataAccessToken.getToken());
        Assert.assertEquals("mockemail2@email.com", newDataAccessToken.getUsername());
        Assert.assertEquals(creationDate, newDataAccessToken.getCreation());
        Assert.assertEquals(expirationDate, newDataAccessToken.getExpiration());
    }
    
    @Test
    @Transactional
    public void removeDataAccessToken() {
        dataAccessTokenMyBatisRepository.removeDataAccessToken("6c9a641e-9719-fake-data-f17e089b37e8");
        List<DataAccessToken> dataAccessTokensForMockEmail2 = dataAccessTokenMyBatisRepository.getAllDataAccessTokensForUsername("mockemail2@email.com");
        Assert.assertEquals(0, dataAccessTokensForMockEmail2.size());
    }

    @Test
    @Transactional
    public void removeAllDataAccessTokensForUsername() {
        List<DataAccessToken> dataAccessTokensForMockEmail4 = dataAccessTokenMyBatisRepository.getAllDataAccessTokensForUsername("mockemail4@email.com");
        Assert.assertEquals(3, dataAccessTokensForMockEmail4.size());
        dataAccessTokenMyBatisRepository.removeAllDataAccessTokensForUsername("mockemail4@email.com");
        List<DataAccessToken> dataAccessTokensForMockEmail4AfterDeletion = dataAccessTokenMyBatisRepository.getAllDataAccessTokensForUsername("mockemail4@email.com");
        Assert.assertEquals(0, dataAccessTokensForMockEmail4AfterDeletion.size());
    }
}
