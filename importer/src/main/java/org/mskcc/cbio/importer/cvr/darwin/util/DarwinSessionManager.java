package org.mskcc.cbio.importer.cvr.darwin.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/20/14.
 */
public enum DarwinSessionManager {

    /*
    A Singleton implemented as an enum to provide access to a Darwin SQL
    session object
     */
    INSTANCE;
    private SqlSession session = Suppliers.memoize(new DarwinSessionSupplier()).get();

    public SqlSession getDarwinSession(){
        return  this.session;
    }

    private class DarwinSessionSupplier implements Supplier<SqlSession> {
        //TODO: make this a property
        private final String configFileName = "/mybatis-config.xml";

        @Override
        public SqlSession get() {
            InputStream inputStream =DarwinSessionSupplier.class.getResourceAsStream(configFileName);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
           return sqlSessionFactory.openSession();
        }
    }


}
