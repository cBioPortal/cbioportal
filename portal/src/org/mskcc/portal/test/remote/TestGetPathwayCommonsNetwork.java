package org.mskcc.portal.test.remote;

import junit.framework.TestCase;

import java.rmi.RemoteException;
import java.util.Arrays;

import org.mskcc.portal.remote.GetPathwayCommonsNetwork;
import org.mskcc.portal.util.XDebug;
import org.mskcc.portal.network.Network;

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
        Network net = gpcn.getNetwork(Arrays.asList("BRCA1"), new XDebug());
        boolean print = false;
        if (print)
            System.out.println(net);
        
    }
}
