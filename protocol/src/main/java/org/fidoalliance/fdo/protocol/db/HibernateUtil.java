package org.fidoalliance.fdo.protocol.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.fidoalliance.fdo.protocol.Config;
import org.fidoalliance.fdo.protocol.DatabaseServer;
import org.fidoalliance.fdo.protocol.LoggerService;
import org.fidoalliance.fdo.protocol.Mapper;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

  private static LoggerService logger;

  private static class RootConfig {

    @JsonProperty("hibernate-properties")
    private Map<String, String> hibernateProperties = new HashMap<>();

  }

  private static final SessionFactory sessionFactory = buildSessionFactory();

  private static SessionFactory buildSessionFactory() {
    try {

      final File file = Path.of(Config.getPath(),"hibernate.cfg.xml").toFile();
      final Configuration cfg = new Configuration();
      logger = new LoggerService(HibernateUtil.class);
      cfg.configure(file);

      final Map<String, String> map = Config.getConfig(RootConfig.class).hibernateProperties;
      for (Map.Entry<String, String> entry : map.entrySet()) {
        cfg.setProperty(entry.getKey(),Config.resolve(entry.getValue()));
      }

      final DatabaseServer server = Config.getWorker(DatabaseServer.class);
      server.start();

      final SessionFactory factory = cfg.buildSessionFactory();


      return factory;

    }
    catch (Throwable e) {
      // Make sure you log the exception, as it might be swallowed
      logger.error("database session factory setup failed");
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * Gets the configured Hibernate Session factory.
   * @return The sessionFactory.
   */
  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * Unwraps a Blob to bytes.
   * @param blob The blob to unwrap.
   * @return The bytes represented by the blob.
   * @throws IOException An error occurred.
   */
  public static byte[] unwrap(Blob blob) throws IOException {
    try {
      int length = Long.valueOf(blob.length()).intValue();
      return blob.getBytes(Long.valueOf(1),length);
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  public static <T> T unwrap(final Blob blob, final Class<T> clazz) throws IOException {
    try (InputStream input = blob.getBinaryStream()) {
      return Mapper.INSTANCE.readValue(input,clazz);
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }


  public static void shutdown() {
    //sessionFactory.ge
    // Close caches and connection pools
    getSessionFactory().close();
  }
}