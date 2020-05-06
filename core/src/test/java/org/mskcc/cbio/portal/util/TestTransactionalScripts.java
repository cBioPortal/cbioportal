package org.mskcc.cbio.portal.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/demoScript.xml", "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestTransactionalScripts {

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
