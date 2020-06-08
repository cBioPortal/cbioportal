package org.mskcc.cbio.portal.auth_integration_test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({})
public class TestAuthIntegrationGoogle extends AbstractAuthIntegrationTest {

    private static String cbioUrl;

    @BeforeClass
    public static void setUp() {
        System.out.println("portal starting with googleplus ...");
        cbioportal.withEnv("AUTHENTICATE", "googleplus");
        cbioportal.start();
        System.out.println("portal ready (port is: " + cbioportal.getMappedPort(8080) +")");
        cbioUrl = "http://localhost:" + cbioportal.getMappedPort(8080) + "/";
    }

    @AfterClass
    public static void tearDown() {
        cbioportal.stop();
    }

    @Test
    public void myFirstTest() {
        RemoteWebDriver driver = chrome.getWebDriver();
        driver.get(cbioUrl);
        WebElement button = driver.findElement(By.tagName("button"));
        assertNotNull(button);
        // test should be expanded to test actual login
    }

}
