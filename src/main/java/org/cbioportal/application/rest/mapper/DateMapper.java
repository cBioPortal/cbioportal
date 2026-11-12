package org.cbioportal.application.rest.mapper;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.mapstruct.Named;

/**
 * Formats dates for REST responses in UTC, independent of the server's default time zone. This
 * matches the Jackson {@code @JsonFormat} behavior the API used before responses were mapped to
 * DTOs.
 */
public class DateMapper {

  private static final DateTimeFormatter UTC_DATE_TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

  @Named("utcDateTime")
  public String toUtcDateTime(Date date) {
    return date == null ? null : UTC_DATE_TIME.format(date.toInstant());
  }
}
