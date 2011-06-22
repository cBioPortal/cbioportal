package org.mskcc.portal.test.remote;

import junit.framework.TestCase;

import java.rmi.RemoteException;
import java.util.Arrays;

import org.mskcc.portal.remote.GetPathwayCommonsNetwork;
import org.mskcc.portal.util.XDebug;

/**
 * JUnit test for get cancer types.
 */
public class TestGetPathwayCommonsNetwork extends TestCase {

    /**
     * Tests the getCancerTypes method.
     *
     * @throws RemoteException Remote IO Error.
     */
    public void testGetPathwayCommonsNetwork() throws RemoteException {
        GetPathwayCommonsNetwork gpcn = new GetPathwayCommonsNetwork();
        String net = gpcn.getNetwork(Arrays.asList("BRCA1"), new XDebug());
        System.out.println(net);
        
    }
}
