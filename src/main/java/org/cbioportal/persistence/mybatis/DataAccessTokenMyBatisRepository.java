package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.persistence.DataAccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DataAccessTokenMyBatisRepository implements DataAccessTokenRepository {

    @Autowired
    private DataAccessTokenMapper dataAccessTokenMapper;

    @Override
    public List<DataAccessToken> getAllDataAccessTokensForUsername(String username) {
        return dataAccessTokenMapper.getAllDataAccessTokensForUsername(username);
    }

    @Override
    public DataAccessToken getDataAccessToken(String token) {
        return dataAccessTokenMapper.getDataAccessToken(token);
    }
    
    @Override
    public void addDataAccessToken(DataAccessToken token) {
        dataAccessTokenMapper.addDataAccessToken(token);
    }

    @Override
    public void removeDataAccessToken(String token) {
        dataAccessTokenMapper.removeDataAccessToken(token);
    }

    @Override
    public void removeAllDataAccessTokensForUsername(String username) {
        dataAccessTokenMapper.removeAllDataAccessTokensForUsername(username);
    }
}
