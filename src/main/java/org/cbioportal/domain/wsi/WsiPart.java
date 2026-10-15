package org.cbioportal.domain.wsi;

import java.util.List;

public record WsiPart(
    String partNumber,
    String partType,
    String partDescription,
    String subspecialty,
    List<WsiBlock> blocks) {}
