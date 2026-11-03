package org.mskcc.cbio.portal.integrationTest.scripts;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.scripts.DumpPortalInfo;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDumpPortalInfoApproval extends IntegrationTestBase {

    private static final List<String> PORTAL_INFO_FILES = Arrays.asList(
        "cancer-types.json",
        "genes.json",
        "genesaliases.json",
        "genesets.json",
        "genesets_version.json",
        "gene-panels.json"
    );
    private static final Path APPROVED_DIR =
        Paths.get("tests", "test_data", "dumpPortalInfo-approved");
    private static final Path RECEIVED_DIR =
        Paths.get("target", "dumpPortalInfo-received");

    @Test
    public void testDumpPortalInfoApproval() throws Exception {
        resetDirectory(RECEIVED_DIR);

        new DumpPortalInfo(new String[] { RECEIVED_DIR.toString() }).run();

        assertTrue(
            "Approved portal info dir is missing: " + APPROVED_DIR.toAbsolutePath(),
            Files.isDirectory(APPROVED_DIR));
        assertNoUnexpectedFiles(RECEIVED_DIR);
        assertApprovedFilesMatch(RECEIVED_DIR, APPROVED_DIR);
    }

    private static void assertApprovedFilesMatch(Path receivedDir, Path approvedDir) throws IOException, JSONException {
        for (String fileName : PORTAL_INFO_FILES) {
            Path received = receivedDir.resolve(fileName);
            Path approved = approvedDir.resolve(fileName);
            assertTrue("Missing received file: " + received.toAbsolutePath(), Files.isRegularFile(received));
            assertTrue(
                "Missing approved file: " + approved.toAbsolutePath()
                    + ". Copy from " + received.toAbsolutePath() + " after review.",
                Files.isRegularFile(approved));
            assertJsonMatch(fileName, approved, received);
        }
    }

    private static void assertJsonMatch(String fileName, Path approved, Path received) throws JSONException, IOException {
        String message = "Mismatch for " + fileName + ".\nCopy " + received.toAbsolutePath()
                + " to " + approved.toAbsolutePath() + " after review.";
        JSONAssert.assertEquals(message, Files.readString(approved), Files.readString(received), false);
    }

    private static void assertNoUnexpectedFiles(Path receivedDir) throws IOException {
        List<String> unexpectedFiles;
        try (Stream<Path> files = Files.list(receivedDir)) {
            unexpectedFiles = files
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .filter(name -> !PORTAL_INFO_FILES.contains(name))
                .sorted()
                .collect(Collectors.toList());
        }
        assertTrue("Unexpected output files: " + unexpectedFiles, unexpectedFiles.isEmpty());
    }

    private static void resetDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }
        Files.createDirectories(directory);
    }
}
