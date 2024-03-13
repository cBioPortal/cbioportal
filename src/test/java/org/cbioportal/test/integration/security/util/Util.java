package org.cbioportal.test.integration.security.util;

import org.cbioportal.test.integration.security.ContainerConfig;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class Util {

    public static void testLogin(String cbioUrl, ChromeDriver chromedriver) {
        performLogin(cbioUrl, chromedriver);
        WebElement loggedInButton = chromedriver.findElement(By.id("dat-dropdown"));
        Assertions.assertEquals("Logged in as testuser@thehyve.nl", loggedInButton.getText());
        new WebDriverWait(chromedriver, Duration.ofSeconds(20)).until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//span[.='Breast Invasive Carcinoma (TCGA,Nature 2012)']")));
        Assertions.assertDoesNotThrow(
            () -> chromedriver.findElement(By.xpath("//span[.='Breast Invasive Carcinoma (TCGA,Nature 2012)']")),
            "Study could not be found on the landing page. Permissions are not correctly passed from IDP to client.");
    }
    
    public static void testLoginAndVerifyStudyNotPresent(String cbioUrl, ChromeDriver chromeDriver) {
        performLogin(cbioUrl, chromeDriver);
        WebElement loggedInButton = chromeDriver.findElement(By.id("dat-dropdown"));
        Assertions.assertEquals("Logged in as testuser@thehyve.nl", loggedInButton.getText());
        new WebDriverWait(chromeDriver, Duration.ofSeconds(20)).until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//span[.='Breast Invasive Carcinoma (TCGA,Nature 2012)']")));
        Assertions.assertThrows(
            NoSuchElementException.class,
            () -> chromeDriver.findElement(By.xpath("//span[.='Adrenocortical Carcinoma (TCGA, Provisional)']")),
            "Study could not be found on the landing page. Permissions are not correctly passed from IDP to client."); 
    }
    
    public static void testDownloadOfflineToken(String cbioUrl, ChromeDriver chromeDriver) throws Exception {
        performLogin(cbioUrl, chromeDriver);
        Assertions.assertDoesNotThrow(
            () -> chromeDriver.findElement(By.id("dat-dropdown")).click(),
            "Logged-in menu could not be found on the page.");
        chromeDriver.findElement(By.linkText("Data Access Token")).click();
        chromeDriver.findElement(By.xpath("//button[text()='Download Token']")).click();

        var file = new File(String.format("%s/cbioportal_data_access_token.txt",
            ContainerConfig.DOWNLOAD_FOLDER));
        await().atMost(Duration.ofSeconds(5)).until(file::exists);
        Assertions.assertTrue(file.exists());
    }
    
    public static void testOAuthLogout(String cbioUrl, ChromeDriver chromeDriver) {
        performLogin(cbioUrl, chromeDriver);
        Assertions.assertDoesNotThrow(
            () -> chromeDriver.findElement(By.id("dat-dropdown")).click(),
            "Logout menu could not be found on the page.");
        //chromeDriver.findElement(By.linkText("Sign out")).click();
        // TODO: Remove when sync'd with frontend
        chromeDriver.get(cbioUrl + "/logout");
        Assertions.assertDoesNotThrow(
            () -> chromeDriver.findElement(By.id("username")),
            "IDP login screen not visible on the page. Logout did not work correctly.");
    }

    public static void testSamlLogout(String cbioUrl, ChromeDriver chromeDriver) {
        performLogin(cbioUrl, chromeDriver);
        Assertions.assertDoesNotThrow(
            () -> chromeDriver.findElement(By.id("dat-dropdown")).click(),
            "Logout menu could not be found on the page.");
        chromeDriver.findElement(By.linkText("Sign out")).click();
        Assertions.assertDoesNotThrow(
            () -> {
                chromeDriver.findElement(By.xpath("//button[text()='Login CBioPortal']"));
            }
        );
    }
    
    
    
    public static void testLoginAgain(String cbioUrl, ChromeDriver chromeDriver) {
        performLogin(cbioUrl, chromeDriver);
        Assertions.assertDoesNotThrow(
            () -> chromeDriver.findElement(By.id("dat-dropdown")).click(),
            "Logout menu could not be found on the page.");
        chromeDriver.get(cbioUrl + "/logout");
        chromeDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        Assertions.assertDoesNotThrow(
            () -> chromeDriver.findElement(By.id("username")),
            "IDP login screen not visible on the page. Logout did not work correctly."
        );
        performLogin(cbioUrl, chromeDriver);
        Assertions.assertDoesNotThrow(
            () -> chromeDriver.findElement(By.id("dat-dropdown")),
            "Logged-in menu could not be found on the page. Login did not work correctly.");
    }

    private static void performLogin(String url, RemoteWebDriver driver) {
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        try {
            // when the cbioportal logo is visible, skip login
            driver.findElement(By.id("cbioportal-logo"));
        } catch (NoSuchElementException e) {
            WebElement userNameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("kc-login"));
            userNameInput.sendKeys("testuser");
            passwordInput.sendKeys("P@ssword1");
            loginButton.click();
        }
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-test='cancerTypeListContainer']")));
    }

}
