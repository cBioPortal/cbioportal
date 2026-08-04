package org.cbioportal.domain.wsi;

import java.util.List;

public record WsiPart(
    String partNumber,
    String partDesignator,
    String partType,
    String partDescription,
    String subspecialty,
    String pathDxTitle,
    List<WsiBlock> blocks) {}
