package org.cbioportal.domain.wsi;

import java.util.List;

public record WsiBlock(String blockNumber, String blockLabel, List<WsiSlide> slides) {}
