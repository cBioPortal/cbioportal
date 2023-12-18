package org.cbioportal.persistence;

import org.cbioportal.model.DataAccessToken;
import java.util.List;

public interface DataAccessTokenRepository {

    List<DataAccessToken> getAllDataAccessTokensForUsername(String username);

    DataAccessToken getDataAccessToken(String token);

    void addDataAccessToken(DataAccessToken token);

    void removeDataAccessToken(String token);

    void removeAllDataAccessTokensForUsername(String username);
}
