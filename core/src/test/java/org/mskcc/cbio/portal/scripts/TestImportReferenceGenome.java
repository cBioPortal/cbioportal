package org.mskcc.cbio.portal.scripts;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.mskcc.cbio.portal.dao.DaoReferenceGenome;
import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * JUnit tests for ImportGeneData class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportReferenceGenome {

    @Test
    /*
     * Checks that ImportGeneData works by calculating the length from three genes
     * in genes_test.txt. The file genes_test.txt contains real data.
     */
    public void testImportReferenceGenome() throws Exception {
        ProgressMonitor.setConsoleMode(false);
        File file = new File("src/test/resources/reference_genomes.txt");
        ImportReferenceGenome.importData(file);
        ReferenceGenome genome = DaoReferenceGenome.getReferenceGenomeByInternalId(1);
        assertEquals("GRCh37", genome.getBuildName());
        assertEquals(1, DaoReferenceGenome.getReferenceGenomeIdByName("GRCh37"));
    }
}