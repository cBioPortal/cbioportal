package org.cbioportal.persistence.mybatis;

import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { GeneTableMyBatisRepository.class, TestConfig.class })
public class GeneTableMyBatisRepositoryTest {

	@Autowired
	private GeneTableMyBatisRepository versionInfoRepository;

	@Test
	public void testGetGenetableVersionByDbVersion() {
		String version = versionInfoRepository.getGenetableVersion();
		assertNotNull(version);
	}

}
