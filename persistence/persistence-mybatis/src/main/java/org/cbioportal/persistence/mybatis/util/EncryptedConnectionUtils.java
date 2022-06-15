/*
 * Copyright (C) 2017-2021 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cbioportal.persistence.mybatis.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * Utility methods for encryption private keys.
 */
public class EncryptedConnectionUtils {
  private EncryptedConnectionUtils() {
  }

  /**
   * Generates an InputStream that contains certificates for a private key.
   *
   * @param keyStorePath     path to the keystore
   * @param keyStorePassword password for the keystore
   * @return a new InputStream containing the certificates
   * @throws Exception if there was an error looking up the private key or certificates
   */
  public static InputStream getCertificateStream(String keyStorePath, String keyStorePassword) throws Exception {
    final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (final InputStream keyStoreStream = Files.newInputStream(Paths.get(keyStorePath))) {
      keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
    }

    final Enumeration<String> aliases = keyStore.aliases();
    while (aliases.hasMoreElements()) {
      final String alias = aliases.nextElement();
      if (keyStore.isCertificateEntry(alias)) {
        final Certificate certificates = keyStore.getCertificate(alias);
        return toInputStream(certificates);
      }
    }
    throw new RuntimeException("Keystore did not have a private key.");
  }

  private static InputStream toInputStream(Certificate certificate) throws IOException {
    try (final StringWriter writer = new StringWriter();
         final JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
      pemWriter.writeObject(certificate);
      pemWriter.flush();
      return new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
    }
  }
}
