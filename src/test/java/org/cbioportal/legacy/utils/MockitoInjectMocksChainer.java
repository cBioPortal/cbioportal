package org.cbioportal.legacy.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fills in null dependency fields on a test class's {@code @Spy}/{@code @InjectMocks} objects by
 * cross-wiring them against every other mock/spy field declared on the same test instance, matching
 * by declared field type (disambiguating same-type matches by field name).
 *
 * <p>Some of this project's tests declare chains of {@code @Spy @InjectMocks} fields, where one
 * such object is itself a dependency of another (e.g. a {@code StudyViewFilterUtil} spy injected
 * into a {@code StudyViewFilterApplier} under test). Mockito's own {@code @InjectMocks} field
 * injection does not reliably wire these chains on this project's current mockito/spring-test
 * versions, leaving such fields {@code null}. Call {@link #chain(Object)} right after {@code
 * MockitoAnnotations.openMocks(this)} to patch them up.
 */
public final class MockitoInjectMocksChainer {

  private MockitoInjectMocksChainer() {}

  public static void chain(Object testInstance) {
    Map<Object, String> pool = new IdentityHashMap<>();
    for (Field field : allInstanceFields(testInstance.getClass())) {
      Object value = get(field, testInstance);
      if (value != null) {
        pool.put(value, field.getName());
      }
    }

    for (int pass = 0; pass < 2; pass++) {
      for (Object candidate : new ArrayList<>(pool.keySet())) {
        for (Field targetField : allInstanceFields(candidate.getClass())) {
          if (get(targetField, candidate) != null) {
            continue;
          }
          Object match = findMatch(pool, targetField, candidate);
          if (match != null) {
            set(targetField, candidate, match);
          }
        }
      }
    }
  }

  private static Object findMatch(Map<Object, String> pool, Field targetField, Object owner) {
    Class<?> type = targetField.getType();
    Object byName = null;
    List<Object> byType = new ArrayList<>();
    for (Map.Entry<Object, String> entry : pool.entrySet()) {
      Object candidate = entry.getKey();
      if (candidate == owner || !type.isInstance(candidate)) {
        continue;
      }
      byType.add(candidate);
      if (entry.getValue().equals(targetField.getName())) {
        byName = candidate;
      }
    }
    if (byName != null) {
      return byName;
    }
    return byType.size() == 1 ? byType.get(0) : null;
  }

  private static List<Field> allInstanceFields(Class<?> type) {
    List<Field> fields = new ArrayList<>();
    for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
      for (Field field : c.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())) {
          fields.add(field);
        }
      }
    }
    return fields;
  }

  private static Object get(Field field, Object target) {
    field.setAccessible(true);
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void set(Field field, Object target, Object value) {
    field.setAccessible(true);
    try {
      field.set(target, value);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
}
