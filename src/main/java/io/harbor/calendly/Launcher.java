package io.harbor.calendly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class Launcher extends io.vertx.core.Launcher {

  public static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

  // Main

  private static String host = "127.0.0.1";

  // Constants

  public static void main(String[] args) {
    try {
      host = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      LOG.info("Unable to retrieve local host address and set it as event bus host. " + e.getMessage());
    }

    ObjectMapper mapper = DatabindCodec.mapper();
    JavaTimeModule module = new JavaTimeModule();
    LocalDateTimeDeserializer localDateTimeDeserializer = new
      LocalDateTimeDeserializer(
      new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd[['T']HH:mm:ss]")
        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
        .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
        .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
        .toFormatter().withZone(ZoneId.of("UTC"))
    );
    module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

    LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd[['T']HH:mm:ss]")
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
      .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
      .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
      .toFormatter().withZone(ZoneId.of("UTC")));
    module.addSerializer(localDateTimeSerializer);

    mapper.registerModule(module);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


    System.out.println(mapper.getRegisteredModuleIds());
    try {
      JsonObject ob = new JsonObject().put("time", LocalDateTime.now());
      System.out.println(mapper.writeValueAsString(ob.encode()));
      final String date = mapper.writeValueAsString(LocalDateTime.now());
      System.out.println(date);
      System.out.println(mapper.readValue(date, LocalDateTime.class));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    new Launcher().dispatch(args);
  }

  // Overrides

  @Override
  public void beforeStartingVertx(VertxOptions options) {
    options.getEventBusOptions()/*.setClustered(true)*/
      .setHost(host);
  }
}
