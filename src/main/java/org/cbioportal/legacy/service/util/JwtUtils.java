/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cbioportal.legacy.service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.*;
import javax.crypto.SecretKey;
import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.service.exception.InvalidDataAccessTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  private byte[] decodedSecretKey;

  @Value("${dat.jwt.secret_key:none}") // default value is none
  private void setDecodedSecretKey(String secretKey) {
    if (!secretKey.isEmpty() && !secretKey.equalsIgnoreCase("none")) {
      this.decodedSecretKey = Decoders.BASE64.decode(secretKey);
    }
  }

  @Value("${dat.ttl_seconds:-1}") // default value is -1
  private int jwtTtlSeconds;

  private static final Logger LOG = LoggerFactory.getLogger(JwtUtils.class);

  public DataAccessToken createToken(String username) {
    return this.createToken(username, this.jwtTtlSeconds);
  }

  public DataAccessToken createToken(String username, int jwtTtlSeconds) {
    if (username == null || username.trim().length() == 0) {
      throw new IllegalArgumentException("subject cannot be empty");
    }
    Calendar calendar = Calendar.getInstance();
    Date creationDate = calendar.getTime();
    calendar.add(Calendar.SECOND, jwtTtlSeconds);
    Date expirationDate = calendar.getTime();
    String jws =
        Jwts.builder()
            .setSubject(username)
            .setIssuedAt(creationDate)
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS256, decodedSecretKey)
            .compact();
    return new DataAccessToken(jws, username, expirationDate, creationDate);
  }

  public void validate(String token) throws InvalidDataAccessTokenException {
    Map<String, Object> properties = extractClaims(token);
  }

  public String extractSubject(String token) throws InvalidDataAccessTokenException {
    Claims claims = extractClaims(token);
    return claims.getSubject();
  }

  public Date extractExpirationDate(String token) throws InvalidDataAccessTokenException {
    Claims claims = extractClaims(token);
    return claims.getExpiration();
  }

  public Map<String, Object> extractProperties(String token)
      throws InvalidDataAccessTokenException {
    return extractClaims(token);
  }

  public Claims extractClaims(String token) throws InvalidDataAccessTokenException {
    Claims claims = null;
    try {
      Jws<Claims> jwsClaims = Jwts.parser().setSigningKey(decodedSecretKey).parseClaimsJws(token);
      claims = jwsClaims.getBody();
    } catch (SignatureException e) {
      LOG.error("Error occurred", e);
      throw new InvalidDataAccessTokenException("signature not valid");
    } catch (ExpiredJwtException e) {
      LOG.error("Error occurred", e);
      throw new InvalidDataAccessTokenException("token has expired");
    }
    return claims;
  }

  public static String createNewSecretKey() {
    SecretKey key =
        Keys.secretKeyFor(
            SignatureAlgorithm
                .HS256); // TODO: consider other Algorithms .. or parameterizing this choice
    String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
    return encodedKey;
  }

  public static void main(String[] args) {
    if (args.length == 1 && args[0].equals("--make-key")) {
      System.out.println("Creating new secret key for JWTS token signing:");
      System.out.println(createNewSecretKey());
    } else {
      System.out.println("usage: JwtUtils --make-key");
    }
  }
}
