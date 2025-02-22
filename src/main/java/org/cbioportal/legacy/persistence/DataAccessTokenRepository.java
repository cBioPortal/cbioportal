package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.DataAccessToken;

public interface DataAccessTokenRepository {

  List<DataAccessToken> getAllDataAccessTokensForUsername(String username);

  DataAccessToken getDataAccessToken(String token);

  void addDataAccessToken(DataAccessToken token);

  void removeDataAccessToken(String token);

  void removeAllDataAccessTokensForUsername(String username);
}
