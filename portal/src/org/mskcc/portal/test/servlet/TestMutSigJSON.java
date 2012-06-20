package org.mskcc.portal.test.servlet;

import junit.framework.TestCase;
import org.junit.Test;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.portal.servlet.MutSigJSON;

import java.util.HashSet;

public class TestMutSigJSON extends TestCase {

    @Test
    public void testMutSigtoMap()
    {
        // make a stupid test
        CanonicalGene canonicalGene = new CanonicalGene(12, "hello", new HashSet<String>());

        // test one of the values of the map.  probably a waste of time
        MutSig mutsig = new MutSig(1, canonicalGene, 1, 502500, 20, "<1E-11", "1E-8");
        assertTrue(!MutSigJSON.MutSigtoMap((mutsig)).isEmpty());
        assertTrue(MutSigJSON.MutSigtoMap(mutsig).get("qval").equals("1E-8"));

        // should now reject because qval is too large
        mutsig = new MutSig(1, canonicalGene, 1, 502500, 20, "12", "12");
        assertFalse(MutSigJSON.MutSigtoMap(mutsig).isEmpty());

        // sometimes the qvals comes in a funny format
        mutsig = new MutSig(1, canonicalGene, 1, 502500, 20, "<1E-8", "<1E-8");
        assertTrue(!MutSigJSON.MutSigtoMap((mutsig)).isEmpty());
        assertTrue(MutSigJSON.MutSigtoMap(mutsig).get("qval").toString().equals("<1E-8"));

        // untested : functionality of DoGet method
        //
        // this is the most important part!
        // to test this we would have to do some work to simulate an httpServletRequest
        // I've been doing this from the browser
    }
}
