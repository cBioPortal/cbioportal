package org.cbioportal.test.integration.security.util;

import org.cbioportal.test.integration.security.AbstractContainerTest;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.Assert;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class Util {

    public static boolean isHostMappingPresent(String host, String ipAddress) throws IOException {
        String hostsFilePath = "/etc/hosts";
        BufferedReader reader = new BufferedReader(new FileReader(hostsFilePath));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (line.contains(host) && line.contains(ipAddress)) {
                    return true;
                }
            }
        } finally {
            reader.close();
        }

        return false;
    }

    public static void testLogin(String cbioUrl, BrowserWebDriverContainer chromedriverContainer) {
        RemoteWebDriver driver = chromedriverContainer.getWebDriver();
        performLogin(cbioUrl, driver);
        WebElement loggedInButton = driver.findElement(By.id("dat-dropdown"));
        Assertions.assertEquals("Logged in as testuser@thehyve.nl", loggedInButton.getText());
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//span[.='Breast Invasive Carcinoma (TCGA,Nature 2012)']")));
        Assertions.assertDoesNotThrow(
            () -> driver.findElement(By.xpath("//span[.='Breast Invasive Carcinoma (TCGA,Nature 2012)']")),
            "Study could not be found on the landing page. Permissions are not correctly passed from IDP to client.");
    }
    
    public static void testLoginAndVerifyStudyNotPresent(String cbioUrl, BrowserWebDriverContainer chromedriverContainer) {
        RemoteWebDriver driver = chromedriverContainer.getWebDriver();
        performLogin(cbioUrl, driver);
        WebElement loggedInButton = driver.findElement(By.id("dat-dropdown"));
        Assertions.assertEquals("Logged in as testuser@thehyve.nl", loggedInButton.getText());
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//span[.='Breast Invasive Carcinoma (TCGA,Nature 2012)']")));
        Assertions.assertThrows(
            NoSuchElementException.class,
            () -> driver.findElement(By.xpath("//span[.='Adrenocortical Carcinoma (TCGA, Provisional)']")),
            "Study could not be found on the landing page. Permissions are not correctly passed from IDP to client."); 
    }
    
    public static void testDownloadOfflineToken(String cbioUrl, BrowserWebDriverContainer chromedriverContainer) throws Exception {
        RemoteWebDriver driver = chromedriverContainer.getWebDriver();
        performLogin(cbioUrl, driver);
        Assertions.assertDoesNotThrow(
            () -> driver.findElement(By.id("dat-dropdown")).click(),
            "Logged-in menu could not be found on the page.");
        driver.findElement(By.linkText("Data Access Token")).click();
        driver.findElement(By.xpath("//button[text()='Download Token']")).click();

        await().atMost(Duration.ofSeconds(5)).until(downloadedFile(chromedriverContainer));

        Assertions.assertTrue(downloadedFile(chromedriverContainer  ).call());
    }
    
    public static void testLogout(String cbioUrl, BrowserWebDriverContainer chromedriverContainer) {
        RemoteWebDriver driver = chromedriverContainer.getWebDriver();
        performLogin(cbioUrl, driver);
        Assertions.assertDoesNotThrow(
            () -> driver.findElement(By.id("dat-dropdown")).click(),
            "Logout menu could not be found on the page.");
        driver.findElement(By.linkText("Sign out")).click();
        driver.get(cbioUrl + "/logout");
        Assertions.assertDoesNotThrow(
            () -> driver.findElement(By.id("username")),
            "IDP login screen not visible on the page. Logout did not work correctly."
        );
    }
    
    public static void testLoginAgain(String cbioUrl, BrowserWebDriverContainer chromedriverContainer) {
        RemoteWebDriver driver = chromedriverContainer.getWebDriver();
        performLogin(cbioUrl, driver);
        Assertions.assertDoesNotThrow(
            () -> driver.findElement(By.id("dat-dropdown")).click(),
            "Logout menu could not be found on the page.");
        driver.get(cbioUrl + "/logout");
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        Assertions.assertDoesNotThrow(
            () -> driver.findElement(By.id("username")),
            "IDP login screen not visible on the page. Logout did not work correctly."
        );
        performLogin(cbioUrl, driver);
        Assertions.assertDoesNotThrow(
            () -> driver.findElement(By.id("dat-dropdown")),
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
        // wait for the page to load
//        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-test='cancerTypeListContainer']")));
    }

    private static boolean containerFileExists(
        @Nonnull final GenericContainer container, @Nonnull String path)
        throws IOException, InterruptedException {
        Assert.notNull(container, "Containers is null");
        Assert.isTrue(!path.isEmpty(), "Path string is empty");
        Container.ExecResult r = container.execInContainer("/bin/sh", "-c",
            "if [ -f " + path
                + " ] ; then echo '0' ; else (>&2 echo '1') ; fi");
        boolean fileNotFound = r.getStderr().contains("1");
        container.execInContainer("rm -f " + path);
        return !fileNotFound;
    }

    private static Callable<Boolean> downloadedFile(GenericContainer chromedriverContainer) {
        return () -> containerFileExists(chromedriverContainer,
            String.format("%s/cbioportal_data_access_token.txt",
                AbstractContainerTest.DOWNLOAD_FOLDER));
    }

}
