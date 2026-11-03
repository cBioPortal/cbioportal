package org.mskcc.cbio.portal.integrationTest.util;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.util.TransactionalScripts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/demoScript.xml", "classpath:/applicationContext-dao.xml" })
public class TestTransactionalScripts extends IntegrationTestBase {

	@Autowired
    private ApplicationContext applicationContext;

	@Before
	public void setUp() {
		DaoCancerStudy.reCacheAll();
	}

	/**
	 * A basic test of using the context to drive a set of scripts.
	 */
	@Test
	public void testScripts() {
		TransactionalScripts scripts = applicationContext.getBean(TransactionalScripts.class);
		scripts.run();
	}

}
