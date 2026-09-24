package org.cbioportal.application.rest.availability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.aopalliance.intercept.MethodInvocation;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

class StudyAvailabilityInterceptorTest {

  private static final Map<String, String> UNAVAILABLE =
      Map.of(
          "study1", "study1",
          "study1_mutations", "study1",
          "study1_all", "study1");

  @SuppressWarnings("unused")
  static class Endpoints {
    void byProfile(@PathVariable String molecularProfileId) {}

    void byParam(@RequestParam String sampleListId) {}

    void byStudies(@RequestBody List<String> studyIds) {}

    void byFilter(@RequestAttribute("interceptedSampleFilter") SampleFilter filter) {}

    void unannotated(String studyId) {}
  }

  private static Object invoke(String methodName, Object arg) throws Throwable {
    Method method =
        Arrays.stream(Endpoints.class.getDeclaredMethods())
            .filter(m -> m.getName().equals(methodName))
            .findFirst()
            .orElseThrow();
    MethodInvocation invocation = mock(MethodInvocation.class);
    when(invocation.getMethod()).thenReturn(method);
    when(invocation.getArguments()).thenReturn(new Object[] {arg});
    when(invocation.proceed()).thenReturn("proceeded");
    return new StudyAvailabilityInterceptor(() -> UNAVAILABLE).invoke(invocation);
  }

  private static String rejectedStudy(String methodName, Object arg) {
    return assertThrows(StudyUnavailableException.class, () -> invoke(methodName, arg))
        .getStudyId();
  }

  private static SampleFilter filterWithUniqueKey(String sampleId, String studyId) {
    SampleFilter filter = new SampleFilter();
    filter.setUniqueSampleKeys(List.of(Encoder.calculateBase64(sampleId, studyId)));
    return filter;
  }

  @Test
  void reportsOwningStudyOfProfileOrSampleList() {
    assertEquals("study1", rejectedStudy("byProfile", "study1_mutations"));
    assertEquals("study1", rejectedStudy("byParam", "study1_all"));
  }

  @Test
  void rejectsUnavailableStudyInBodyCollection() {
    assertEquals("study1", rejectedStudy("byStudies", List.of("study2", "study1")));
  }

  @Test
  void rejectsUnavailableStudyNestedInInterceptedFilter() {
    SampleIdentifier sampleIdentifier = new SampleIdentifier();
    sampleIdentifier.setSampleId("s1");
    sampleIdentifier.setStudyId("study1");
    SampleFilter filter = new SampleFilter();
    filter.setSampleIdentifiers(List.of(sampleIdentifier));
    assertEquals("study1", rejectedStudy("byFilter", filter));
  }

  @Test
  void rejectsUniqueSampleKeyOfUnavailableStudy() {
    assertEquals("study1", rejectedStudy("byFilter", filterWithUniqueKey("s1", "study1")));
  }

  @Test
  void allowsAvailableStudies() throws Throwable {
    assertEquals("proceeded", invoke("byStudies", List.of("study2")));
    assertEquals("proceeded", invoke("byFilter", filterWithUniqueKey("s1", "study2")));
  }

  @Test
  void ignoresArgumentsNotBoundFromTheRequest() throws Throwable {
    assertEquals("proceeded", invoke("unannotated", "study1"));
  }

  @Test
  void skipsInspectionWhenEveryStudyIsAvailable() throws Throwable {
    MethodInvocation invocation = mock(MethodInvocation.class);
    when(invocation.proceed()).thenReturn("proceeded");
    assertEquals("proceeded", new StudyAvailabilityInterceptor(Map::of).invoke(invocation));
    verify(invocation, never()).getArguments();
  }
}
