package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.DataAccessToken;

public interface DataAccessTokenMapper {
    List<DataAccessToken> getAllDataAccessTokensForUsername(String username);

    DataAccessToken getDataAccessToken(String token);

    void addDataAccessToken(DataAccessToken token);

    void removeDataAccessToken(String token);

    void removeAllDataAccessTokensForUsername(String username);
}
