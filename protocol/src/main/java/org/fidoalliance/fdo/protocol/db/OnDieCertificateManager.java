package org.fidoalliance.fdo.protocol.db;

import org.fidoalliance.fdo.protocol.entity.OnDieCertificateData;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OnDieCertificateManager {

  protected static List<String> rootCaList = new ArrayList<String>(Arrays.asList(
          "OnDie_CA_RootCA_Certificate.cer",
          "OnDie_CA_DEBUG_RootCA_Certificate.cer"));

  public boolean isOnDieRootCA(String certName) {
    for (String rootName : rootCaList) {
      if (rootName.equalsIgnoreCase(certName)) {
        return true;
      }
    }
    return false;
  }

  public byte[] getCertificate(String name) throws IOException {

    URL url = new URL(name);
    if (url == null) {
      throw new IllegalArgumentException("OnDieCache: illegal crl reference: " + name);
    }
    Path path = Paths.get(url.getFile());
    Path fileName = path.getFileName();
    if (fileName == null) {
      throw new IllegalArgumentException("OnDieCache: illegal crl reference: " + name);
    }

    // Read cert/crl from data store
    Session session = HibernateUtil.getSessionFactory().openSession();
    try {
      Transaction trans = session.beginTransaction();
      OnDieCertificateData certStore = session.get(OnDieCertificateData.class, fileName.toString());
      Blob blob = certStore.getData();
      trans.commit();
      if (blob != null) {
        try {
          byte[] data = blob.getBytes(Long.valueOf(1),
                  Long.valueOf(blob.length()).intValue());
          return data;
        } catch (SQLException e) {
          throw new IOException(e);
        }
      }
    } catch (Exception ex) {
      throw new IOException(ex.getMessage());
    } finally {
      session.close();
    }
    return null;
  }

  public List<String> getCertList() {
    List<String> list;

    Session session = HibernateUtil.getSessionFactory().openSession();
    String hql = "SELECT C.id FROM OnDieCertificateData C ORDER BY C.id";
    Query query = session.createQuery(hql);
    list = query.list();
    return list;
  }

  /***
   * Loads the certificate/crl table from the cert/crl files contained
   * in the specified zip file
   * Current default location for zipfile is "https://tsci.intel.com/content/csme.zip".
   * and for the debug version: "https://pre-tsci.intel.com/content/csme.zip".
   *
   */
  public void LoadFromZipFileURL(URL zipFileUrl) throws IOException {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction trans = session.beginTransaction();
    Query query = session.createQuery("delete OnDieCertificateData");
    query.executeUpdate();

    try {
      URLConnection urlConn = zipFileUrl.openConnection();
      ZipInputStream zipInput = new ZipInputStream(urlConn.getInputStream());
      ZipEntry zipEntry = zipInput.getNextEntry();
      while (zipEntry != null) {
        if (zipEntry.getName().startsWith("content/OnDieCA")
                && (zipEntry.getName().endsWith(".crl")
                || zipEntry.getName().endsWith(".cer"))) {

          String artifactId = Paths.get(zipEntry.getName()).getFileName().toString();

          OnDieCertificateData certStoreEntry = session.get(OnDieCertificateData.class, artifactId);
          if (certStoreEntry == null) {
            certStoreEntry = new OnDieCertificateData();
            certStoreEntry.setId(artifactId);
          }

          Blob blob = session.getLobHelper().createBlob(zipInput.readAllBytes());
          certStoreEntry.setData(blob);
          session.persist(certStoreEntry);
        }
        zipEntry = zipInput.getNextEntry();
      }
      trans.commit();
    } finally {
      session.close();
    }
  }

}