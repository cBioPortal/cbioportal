package org.mskcc.cbio.importer.cvr.darwin.dao;

import org.mskcc.cbio.importer.cvr.darwin.model.IdMapper;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mskcc.cbio.importer.cvr.darwin.model.IdMapper;

public class IdMapperMapper {
    
    private SqlSessionFactory sqlSessionFactory = null;

    public IdMapperMapper(SqlSessionFactory sqlSessionFactory){
        this.sqlSessionFactory = sqlSessionFactory;
    }
    
    public IdMapper selectByDmpId(String dmpId){
        IdMapper idMapper = null;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            idMapper = session.selectOne("IdMapperMapper.selectByDmpId", dmpId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idMapper;
    }
    
    public IdMapper selectByDarwinId(String darwinId){
        IdMapper idMapper = null;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            idMapper = session.selectOne("IdMapperMapper.selectByDarwinId", darwinId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idMapper;
    }
    
}