package org.cbioportal.web.util.appliers;

import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;


/**
 * StudyViewSubFilterApplier is a filter applier for the StudyViewFilterApplier. It takes in a list
 * of SampleIdentifiers and a StudyViewFilter. It returns a list of SampleIdentifiers, filtered using that
 * StudyViewFilter object.
 * 
 * Important: you're going to find it a little difficult to see where this class and its inheritors are used.
 * They're used in the StudyViewFilterApplier, where ALL classes that extend StudyViewSubFilterApplier are pulled
 * from the application context in the init() method.
 * This is important because:
 *     If you make a class that extends this class, it will AUTOMATICALLY be added to the subFilterAppliers
 *     field in StudyViewFilterApplier
 */
// This is an abstract class rather than an interface to make Spring wiring easier.
public abstract class StudyViewSubFilterApplier {
    public abstract List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters);
    
    public abstract boolean shouldApplyFilter(StudyViewFilter studyViewFilter);
}
