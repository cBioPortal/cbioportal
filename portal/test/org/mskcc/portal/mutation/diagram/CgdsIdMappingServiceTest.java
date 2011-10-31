package org.mskcc.portal.mutation.diagram;

/**
 * Unit test for CgdsIdMappingService.
 */
public final class CgdsIdMappingServiceTest extends AbstractIdMappingServiceTest {

    @Override
    protected IdMappingService createIdMappingService() {
        return new CgdsIdMappingService();
    }
}