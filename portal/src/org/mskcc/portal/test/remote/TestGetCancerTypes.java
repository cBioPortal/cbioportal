package org.mskcc.portal.test.remote;

import junit.framework.TestCase;
import org.mskcc.portal.model.CancerType;
import org.mskcc.portal.remote.GetCancerTypes;
import org.mskcc.portal.util.XDebug;

import java.rmi.RemoteException;
import java.util.ArrayList;

// TODO: Later: ACCESS CONTROL: change to cancer study, etc.
/**
 * JUnit test for get cancer types.
 */
public class TestGetCancerTypes extends TestCase {

    /**
     * Tests the getCancerTypes method.
     *
     * @throws RemoteException Remote IO Error.
     */
    public void testGetCancerTypes() throws RemoteException {
        ArrayList<CancerType> cancerTypesList = GetCancerTypes.getCancerTypes(new XDebug());
        assertEquals(4, cancerTypesList.size());
        CancerType cancerType = cancerTypesList.get(0);
        assertTrue(cancerType.getCancerName().startsWith("Prostate Cancer"));
    }
}
