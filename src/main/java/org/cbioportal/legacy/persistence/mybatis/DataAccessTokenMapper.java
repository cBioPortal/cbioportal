package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.DataAccessToken;

public interface DataAccessTokenMapper {

  List<DataAccessToken> getAllDataAccessTokensForUsername(String username);

  DataAccessToken getDataAccessToken(String token);

  void addDataAccessToken(DataAccessToken token);

  void removeDataAccessToken(String token);

  void removeAllDataAccessTokensForUsername(String username);
}
