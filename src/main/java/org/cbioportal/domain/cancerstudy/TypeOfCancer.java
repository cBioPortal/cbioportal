package org.cbioportal.domain.cancerstudy;

import java.io.Serializable;

public record TypeOfCancer(
    String id, String name, String dedicatedColor, String shortName, String parent)
    implements Serializable {}
