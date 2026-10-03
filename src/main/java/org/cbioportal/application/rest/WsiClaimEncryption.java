package org.cbioportal.application.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypts the server-side slide source into the {@code enc} claim of a wsi_auth_version 3 tile
 * capability (contract wsi-serving-v5, section 4).
 *
 * <pre>
 * key  = HKDF-SHA256(ikm = UTF-8(secret), salt = empty, info = "wsi-claim-enc-v3", L = 32)
 * enc  = base64url-no-padding(nonce[12] || AES-256-GCM(key, nonce, aad = UTF-8(slide_key),
 *            plaintext = {"image_id":..,"tile_source":..,"thumbnail_source":..}) || tag[16])
 * </pre>
 */
final class WsiClaimEncryption {

  static final String HKDF_INFO = "wsi-claim-enc-v3";
  static final int NONCE_BYTES = 12;
  static final int TAG_BITS = 128;
  private static final int KEY_BYTES = 32;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final ObjectMapper JSON = new ObjectMapper();

  /** The access-token secret is fixed per process; derive its AES key once, not per token. */
  private record DerivedKey(String secret, SecretKeySpec key) {}

  private static volatile DerivedKey derivedKey;

  private WsiClaimEncryption() {}

  /** Returns the {@code enc} claim using a fresh random 96-bit nonce. */
  static String encrypt(
      String secret, String slideKey, String imageId, String tileSource, String thumbnailSource) {
    byte[] nonce = new byte[NONCE_BYTES];
    RANDOM.nextBytes(nonce);
    return encrypt(secret, slideKey, imageId, tileSource, thumbnailSource, nonce);
  }

  /** Deterministic variant for the cross-implementation test vector. Never reuse a nonce. */
  static String encrypt(
      String secret,
      String slideKey,
      String imageId,
      String tileSource,
      String thumbnailSource,
      byte[] nonce) {
    if (nonce == null || nonce.length != NONCE_BYTES) {
      throw new IllegalArgumentException("WSI claim nonce must be 12 bytes");
    }
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, aesKey(secret), new GCMParameterSpec(TAG_BITS, nonce));
      cipher.updateAAD(slideKey.getBytes(StandardCharsets.UTF_8));
      byte[] sealed = cipher.doFinal(plaintext(imageId, tileSource, thumbnailSource));
      byte[] payload =
          ByteBuffer.allocate(nonce.length + sealed.length).put(nonce).put(sealed).array();
      return Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("AES-256-GCM is unavailable", exception);
    }
  }

  /** HKDF-SHA256 (RFC 5869) with an empty salt, i.e. an HMAC key of 32 zero bytes. */
  private static SecretKeySpec aesKey(String secret) {
    DerivedKey cached = derivedKey;
    if (cached == null || !cached.secret().equals(secret)) {
      cached = new DerivedKey(secret, new SecretKeySpec(deriveKey(secret), "AES"));
      derivedKey = cached;
    }
    return cached.key();
  }

  static byte[] deriveKey(String secret) {
    try {
      Mac extract = Mac.getInstance("HmacSHA256");
      extract.init(new SecretKeySpec(new byte[extract.getMacLength()], "HmacSHA256"));
      byte[] prk = extract.doFinal(secret.getBytes(StandardCharsets.UTF_8));

      Mac expand = Mac.getInstance("HmacSHA256");
      expand.init(new SecretKeySpec(prk, "HmacSHA256"));
      expand.update(HKDF_INFO.getBytes(StandardCharsets.UTF_8));
      expand.update((byte) 0x01);
      byte[] block = expand.doFinal();
      byte[] key = new byte[KEY_BYTES];
      System.arraycopy(block, 0, key, 0, KEY_BYTES);
      return key;
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("HmacSHA256 is unavailable", exception);
    }
  }

  private static byte[] plaintext(String imageId, String tileSource, String thumbnailSource) {
    Map<String, String> claims = new LinkedHashMap<>();
    claims.put("image_id", imageId);
    claims.put("tile_source", tileSource);
    claims.put("thumbnail_source", thumbnailSource);
    try {
      return JSON.writeValueAsBytes(claims);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to serialize WSI claim", exception);
    }
  }
}
