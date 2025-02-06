package org.cbioportal.legacy.persistence;

import org.cbioportal.legacy.model.DataAccessToken;
import java.util.List;

public interface DataAccessTokenRepository {

    List<DataAccessToken> getAllDataAccessTokensForUsername(String username);

    DataAccessToken getDataAccessToken(String token);

    void addDataAccessToken(DataAccessToken token);

    void removeDataAccessToken(String token);

    void removeAllDataAccessTokensForUsername(String username);
}
