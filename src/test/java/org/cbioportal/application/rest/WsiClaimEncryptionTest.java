package org.cbioportal.application.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

/** Cross-implementation test vector from the wsi-serving-v5 contract, section 4. */
public class WsiClaimEncryptionTest {

  static final String SECRET = "local-development-wsi-secret-change-me-32chars";
  static final String SLIDE_KEY = "0123456789abcdef0123456789abcdef";
  private static final byte[] NONCE = HexFormat.of().parseHex("000102030405060708090a0b");
  private static final String PLAINTEXT =
      "{\"image_id\":\"1\",\"tile_source\":\"file:///app/testdata/1.svs\","
          + "\"thumbnail_source\":\"file:///app/testdata/1.jpg\"}";
  private static final String EXPECTED_KEY =
      "f176a275656d945cd5ee1c32030b53363375dcc34baa77a7cf94b2ed8f9c6c0b";
  private static final String EXPECTED_ENC =
      "AAECAwQFBgcICQoLNpOTYnY8HVb-KcPzu1F5HPykr7D0YY_UhVbbyjOFRlxC63fxCt09YO1aYC-phb85wDhN5PPPpC0X"
          + "46RSD0K0bRRgSptRb9wDiqMLtftFQ6VpBGfGILaddEV_s-Zsmpp28fG5Z3XTNWnyoBRa9qfr9t209wS7V-LhGl8i";

  @Test
  public void derivesTheContractKeyWithEmptySaltHkdf() {
    assertEquals(EXPECTED_KEY, HexFormat.of().formatHex(WsiClaimEncryption.deriveKey(SECRET)));
  }

  @Test
  public void reproducesTheContractEncValueByteForByte() {
    String enc =
        WsiClaimEncryption.encrypt(
            SECRET,
            SLIDE_KEY,
            "1",
            "file:///app/testdata/1.svs",
            "file:///app/testdata/1.jpg",
            NONCE.clone());

    assertEquals(EXPECTED_ENC, enc);
  }

  @Test
  public void decryptsTheContractVector() throws Exception {
    assertEquals(PLAINTEXT, decrypt(SECRET, SLIDE_KEY, EXPECTED_ENC));
  }

  @Test
  public void roundTripsWithAFreshRandomNonce() throws Exception {
    String first =
        WsiClaimEncryption.encrypt(
            SECRET, SLIDE_KEY, "syn-img-0001", "s3://bucket/a.svs", "s3://bucket/a.jpg");
    String second =
        WsiClaimEncryption.encrypt(
            SECRET, SLIDE_KEY, "syn-img-0001", "s3://bucket/a.svs", "s3://bucket/a.jpg");

    assertNotEquals("each capability must use a fresh nonce", first, second);
    assertEquals(
        "{\"image_id\":\"syn-img-0001\",\"tile_source\":\"s3://bucket/a.svs\","
            + "\"thumbnail_source\":\"s3://bucket/a.jpg\"}",
        decrypt(SECRET, SLIDE_KEY, first));
    assertEquals(decrypt(SECRET, SLIDE_KEY, first), decrypt(SECRET, SLIDE_KEY, second));
  }

  @Test
  public void bindsTheCiphertextToTheSlideKey() {
    assertThrows(
        AEADBadTagException.class,
        () -> decrypt(SECRET, "fedcba9876543210fedcba9876543210", EXPECTED_ENC));
  }

  @Test
  public void rejectsANonceOfTheWrongLength() {
    assertThrows(
        IllegalArgumentException.class,
        () -> WsiClaimEncryption.encrypt(SECRET, SLIDE_KEY, "1", "a", "b", new byte[16]));
  }

  /** Independent decryptor following the contract, as the tile server implements it. */
  static String decrypt(String secret, String slideKey, String enc) throws Exception {
    byte[] sealed = Base64.getUrlDecoder().decode(enc);
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(
        Cipher.DECRYPT_MODE,
        new SecretKeySpec(WsiClaimEncryption.deriveKey(secret), "AES"),
        new GCMParameterSpec(128, Arrays.copyOfRange(sealed, 0, 12)));
    cipher.updateAAD(slideKey.getBytes(StandardCharsets.UTF_8));
    return new String(
        cipher.doFinal(Arrays.copyOfRange(sealed, 12, sealed.length)), StandardCharsets.UTF_8);
  }
}
