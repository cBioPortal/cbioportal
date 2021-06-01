package org.cbioportal.model;

/*
 QueryElement represents three types of behavior for a SQL statement in MyBatis mappers
 - INACTIVE: exclude Query element from results
 - ACTIVE: include Query element in results
 - PASS: do not apply on Query element
 Note: the specific behavior the represented by the three states 
 of QueryElement is determined by the mapper-xml.
*/
public enum QueryElement {
    INACTIVE, ACTIVE, PASS
}
