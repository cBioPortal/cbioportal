package org.cbioportal.legacy.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.persistence.DataAccessTokenRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

public class UuidDataAccessTokenServiceImplLimitTest {

  private static final String USERNAME = "user";

  private DataAccessTokenRepository repository;
  private UuidDataAccessTokenServiceImpl service;

  @Before
  public void setUp() {
    repository = mock(DataAccessTokenRepository.class);
    service = new UuidDataAccessTokenServiceImpl();
    ReflectionTestUtils.setField(service, "dataAccessTokenRepository", repository);
    ReflectionTestUtils.setField(service, "datTtlSeconds", 60);
  }

  // repository returns tokens ordered by expiration ascending, oldest first
  private void givenExistingTokens(String... tokens) {
    List<DataAccessToken> existing = new ArrayList<>();
    for (String token : tokens) {
      existing.add(new DataAccessToken(token, USERNAME, new Date(), new Date()));
    }
    when(repository.getAllDataAccessTokensForUsername(USERNAME)).thenReturn(existing);
  }

  @Test
  public void createBelowLimitRevokesNothing() {
    ReflectionTestUtils.setField(service, "maxNumberOfAccessTokens", 3);
    givenExistingTokens("oldest", "newest");

    service.createDataAccessToken(USERNAME);

    verify(repository, never()).removeDataAccessToken(anyString());
    verify(repository).addDataAccessToken(any(DataAccessToken.class));
  }

  @Test
  public void createAtLimitRevokesOldestToken() {
    ReflectionTestUtils.setField(service, "maxNumberOfAccessTokens", 2);
    givenExistingTokens("oldest", "newest");

    service.createDataAccessToken(USERNAME);

    verify(repository).removeDataAccessToken("oldest");
    verify(repository, never()).removeDataAccessToken("newest");
  }

  @Test
  public void createAboveLoweredLimitRevokesOldestTokensDownToLimit() {
    ReflectionTestUtils.setField(service, "maxNumberOfAccessTokens", 2);
    givenExistingTokens("first", "second", "third", "fourth");

    service.createDataAccessToken(USERNAME);

    InOrder order = inOrder(repository);
    order.verify(repository).removeDataAccessToken("first");
    order.verify(repository).removeDataAccessToken("second");
    order.verify(repository).removeDataAccessToken("third");
    order.verify(repository).addDataAccessToken(any(DataAccessToken.class));
    verify(repository, never()).removeDataAccessToken("fourth");
  }
}
