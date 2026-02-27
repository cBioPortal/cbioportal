package org.cbioportal.domain.cancerstudy;

import java.io.Serializable;

public record TypeOfCancer(
    String id,
    String name,
    String dedicatedColor,
    String shortName,
    String parent,
    String version,
    String status,
    String history,
    String uri)
    implements Serializable {}
