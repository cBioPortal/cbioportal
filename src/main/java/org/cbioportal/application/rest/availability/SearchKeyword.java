package org.cbioportal.application.rest.availability;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a free-text request parameter that {@link StudyAvailabilityInterceptor} does not inspect,
 * so a search string that happens to equal an unavailable study's identifier is not rejected.
 *
 * <p>Only for parameters whose matches cannot return data of an unavailable study, e.g. the study
 * list search, which always lists every study.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchKeyword {}
