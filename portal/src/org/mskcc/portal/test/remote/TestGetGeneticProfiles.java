package org.mskcc.portal.test.remote;

import junit.framework.TestCase;
import org.mskcc.portal.model.GeneticProfile;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.util.XDebug;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * JUnit test for the GetGeneticProfiles Class.
 */
public class TestGetGeneticProfiles extends TestCase {

    /**
     * Tests the GetGeneticProfiles() method.
     *
     * @throws RemoteException Remote IO Error.
     */
    public void testGetGeneticProfiles() throws RemoteException {
        ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles("gbm",
                new XDebug());
        assertEquals(5, profileList.size());
    }
}
