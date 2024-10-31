package io.harbor.calendly.utils;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class TimeUtils {

  public static final DateTimeFormatter ONLY_DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
  public final static DateTimeFormatter HUMAN_READABLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  public final static DateTimeFormatter WITH_TIMEZONE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd['T']HH:mm:ss[.SSSVV]").toFormatter();
  private static final Logger LOGGER = LoggerFactory.getLogger(TimeUtils.class);
  private final static DateTimeFormatter DEFAULT_FORMATTER =
    new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd['T'HH:mm:ss[.SS][z][Z]]")
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
      .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
      .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
      .toFormatter().withZone(ZoneId.of("UTC"));

  public final static DateTimeFormatter DEFAULT_FORMATTER_UNI =
    new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd[[ ]['T']HH:mm:ss[.SSS][z][Z]]")
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
      .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
      .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
      .toFormatter().withZone(ZoneId.of("UTC"));

  public static String toUTCTimeString(String timestamp) {
    OffsetDateTime dateTime = toUTCTime(timestamp);
    return dateTime.format(HUMAN_READABLE_FORMATTER);
  }

  public static Timestamp toUTCTimestamp(String timestamp) {
    return Timestamp.valueOf(toUTCTime(timestamp).format(HUMAN_READABLE_FORMATTER));
  }

  public static Timestamp toUTCTimestamp(String timestamp, DateTimeFormatter formatter) {
    return Timestamp.valueOf(toUTCTime(timestamp, formatter).format(formatter));
  }

  public static OffsetDateTime toUTCTime(String timestamp, DateTimeFormatter formatter) {
    ZonedDateTime inDateTime;
    LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);
    inDateTime = ZonedDateTime.ofInstant(localDateTime, ZoneOffset.UTC, ZoneId.of("UTC"));
    ZonedDateTime utcTime = inDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    LOGGER.info(utcTime.format(HUMAN_READABLE_FORMATTER));

    return utcTime.toOffsetDateTime();
  }

  public static OffsetDateTime toUTCTime(String timestamp) {
    ZonedDateTime inDateTime;
    LocalDateTime localDateTime = LocalDateTime.parse(timestamp, DEFAULT_FORMATTER);
    inDateTime = ZonedDateTime.ofInstant(localDateTime, ZoneOffset.UTC, ZoneId.of("UTC"));
    ZonedDateTime utcTime = inDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    LOGGER.info(utcTime.format(HUMAN_READABLE_FORMATTER));

    return utcTime.toOffsetDateTime();
  }

  public static String currentTimeStamp() {
    ZonedDateTime inDateTime = ZonedDateTime.ofInstant(LocalDateTime.now(), ZoneOffset.UTC, ZoneId.of("UTC"));
    ZonedDateTime utcTime = inDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    LOGGER.debug(utcTime.format(HUMAN_READABLE_FORMATTER));

    return utcTime.format(HUMAN_READABLE_FORMATTER);
  }


  public static OffsetDateTime now() {
    ZonedDateTime inDateTime = ZonedDateTime.ofInstant(LocalDateTime.now(), ZoneOffset.UTC, ZoneId.of("UTC"));
    ZonedDateTime utcTime = inDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    LOGGER.debug(utcTime.format(HUMAN_READABLE_FORMATTER));

    return utcTime.toOffsetDateTime();
  }

  public static OffsetDateTime now(String zoneId) {
//    ZonedDateTime inDateTime = ZonedDateTime.ofInstant(LocalDateTime.now(), ZoneId.of("UTC"));
    ZonedDateTime nowAtZone = now().toInstant().atZone(ZoneId.of(zoneId));
    return nowAtZone.toOffsetDateTime().atZoneSimilarLocal(ZoneId.of(zoneId)).toOffsetDateTime();
  }

  public static Long currentTime() {
    return Instant.now().toEpochMilli();
  }

  public static OffsetDateTime toOffsetDateTime(long epochMilli) {
    Instant instant = Instant.ofEpochMilli(epochMilli);
    ZonedDateTime inDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    ZonedDateTime utcTime = inDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    LOGGER.debug(utcTime.format(HUMAN_READABLE_FORMATTER));

    return utcTime.toOffsetDateTime();
  }

  public static ZonedDateTime toZonedDateTime(String timestamp) {
    ZonedDateTime inDateTime;
    try {
      LocalDateTime localDateTime = LocalDateTime.parse(timestamp, DEFAULT_FORMATTER_UNI);
      inDateTime = ZonedDateTime.ofInstant(localDateTime, ZoneOffset.UTC, ZoneId.of("UTC"));
      ZonedDateTime utcTime = inDateTime.withZoneSameInstant(ZoneId.of("UTC"));
      LOGGER.info(utcTime.format(HUMAN_READABLE_FORMATTER));

      return utcTime;
    } catch (Exception e) {
      LOGGER.error("Unable to parse timestamp " + timestamp, e);
      inDateTime = ZonedDateTime.of(LocalDateTime.parse(timestamp, DEFAULT_FORMATTER), ZoneId.of("UTC"));
      return inDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    }
  }

}
