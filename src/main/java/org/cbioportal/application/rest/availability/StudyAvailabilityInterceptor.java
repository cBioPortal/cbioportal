package org.cbioportal.application.rest.availability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.cbioportal.legacy.utils.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Rejects a controller call when any request input (path variable, request param, body, or
 * intercepted filter attribute) names an identifier owned by an unavailable study. Independent of
 * user authorization, so it behaves the same on every portal.
 *
 * <p>Arguments are inspected only while some study is unavailable; otherwise the call proceeds
 * without looking at them.
 */
public class StudyAvailabilityInterceptor implements MethodInterceptor {

  private static final Logger log = LoggerFactory.getLogger(StudyAvailabilityInterceptor.class);

  private static final Pattern BASE64 = Pattern.compile("^[A-Za-z0-9+/]+={0,2}$");

  private final Supplier<Map<String, String>> unavailableStudyIdByIdentifier;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * @param unavailableStudyIdByIdentifier identifier → owning study id, for unavailable studies
   */
  public StudyAvailabilityInterceptor(
      Supplier<Map<String, String>> unavailableStudyIdByIdentifier) {
    this.unavailableStudyIdByIdentifier = unavailableStudyIdByIdentifier;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Map<String, String> unavailable = unavailableStudyIdByIdentifier.get();
    if (!unavailable.isEmpty()) {
      Method method = invocation.getMethod();
      Object[] args = invocation.getArguments();
      for (int i = 0; i < args.length; i++) {
        if (args[i] != null && isIdentifyingInput(new MethodParameter(method, i))) {
          String studyId = findUnavailableStudy(args[i], unavailable);
          if (studyId != null) {
            throw new StudyUnavailableException(studyId);
          }
        }
      }
    }
    return invocation.proceed();
  }

  /**
   * Whether the parameter is bound from the request. Excludes framework arguments such as the
   * servlet request or the authentication.
   */
  private static boolean isIdentifyingInput(MethodParameter parameter) {
    if (parameter.hasParameterAnnotation(SearchKeyword.class)) {
      return false;
    }
    return parameter.hasParameterAnnotation(RequestParam.class)
        || parameter.hasParameterAnnotation(PathVariable.class)
        || parameter.hasParameterAnnotation(RequestBody.class)
        || parameter.hasParameterAnnotation(RequestAttribute.class);
  }

  /** Returns the unavailable study named anywhere in {@code arg}, or {@code null} if none. */
  private String findUnavailableStudy(Object arg, Map<String, String> unavailable) {
    if (arg instanceof String text) {
      return match(text, unavailable);
    }
    if (arg instanceof Number || arg instanceof Boolean || arg instanceof Enum<?>) {
      return null;
    }
    JsonNode root;
    try {
      root = objectMapper.valueToTree(arg);
    } catch (IllegalArgumentException e) {
      log.debug("Cannot inspect {} for study availability: {}", arg.getClass(), e.toString());
      return null;
    }
    Deque<JsonNode> pending = new ArrayDeque<>();
    pending.push(root);
    while (!pending.isEmpty()) {
      JsonNode node = pending.pop();
      if (node.isTextual()) {
        String studyId = match(node.textValue(), unavailable);
        if (studyId != null) {
          return studyId;
        }
      } else if (node.isContainerNode()) {
        node.elements().forEachRemaining(pending::push);
      }
    }
    return null;
  }

  private static String match(String text, Map<String, String> unavailable) {
    String studyId = unavailable.get(text);
    return studyId != null ? studyId : matchUniqueKey(text, unavailable);
  }

  /**
   * Matches unique sample/patient keys, which are {@code base64("<sampleOrPatientId>:<studyId>")}
   * encoded without padding, so their length need not be a multiple of 4.
   */
  private static String matchUniqueKey(String text, Map<String, String> unavailable) {
    if (!BASE64.matcher(text).matches()) {
      return null;
    }
    String decoded;
    try {
      decoded = new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return null;
    }
    String[] parts = decoded.split(Encoder.DELIMITER);
    return parts.length == 2 ? unavailable.get(parts[1]) : null;
  }
}
