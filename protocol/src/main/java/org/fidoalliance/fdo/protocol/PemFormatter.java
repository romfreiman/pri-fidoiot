package org.fidoalliance.fdo.protocol;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import org.bouncycastle.asn1.sec.ECPrivateKeyStructure;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.crypto.prng.EntropySource;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class PemFormatter {


  public static String format(List<Certificate> certs) throws IOException {
    StringBuilder builder = new StringBuilder();
    for (Certificate cert : certs) {
      builder.append(format(cert));
    }
    return builder.toString();
  }

  public static String format(Certificate cert) throws IOException {
    try (StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);) {

      pemWriter.writeObject(new JcaMiscPEMGenerator(cert));

      pemWriter.flush();
      pemWriter.close();
      return writer.toString();
    }
  }


  public static String format(PublicKey publicKey) throws IOException {
    try (StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);) {

      pemWriter.writeObject(new JcaMiscPEMGenerator(publicKey));

      pemWriter.flush();
      pemWriter.close();
      return writer.toString();
    }
  }

  public static String formatKey(PrivateKey key, SecureRandom random, String password)
      throws IOException {

    try (StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);) {

      JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(
          PKCS8Generator.AES_256_CBC);
      encryptorBuilder.setProvider(new BouncyCastleProvider());
      encryptorBuilder.setRandom(random);
      encryptorBuilder.setPassword(password.toCharArray());

      OutputEncryptor oe = null;
      try {
        oe = encryptorBuilder.build();
      } catch (OperatorCreationException e) {
        throw new IOException(e);
      }

      pemWriter.writeObject(new JcaPKCS8Generator((PrivateKey) key, oe));

      pemWriter.flush();
      pemWriter.close();
      return writer.toString();
    }

  }
}
