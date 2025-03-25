package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.DataAccessToken;
import java.util.List;

public interface DataAccessTokenMapper {

    List<DataAccessToken> getAllDataAccessTokensForUsername(String username);
    
    DataAccessToken getDataAccessToken(String token);

    void addDataAccessToken(DataAccessToken token);
    
    void removeDataAccessToken(String token);

    void removeAllDataAccessTokensForUsername(String username);
}
 
