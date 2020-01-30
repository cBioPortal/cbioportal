package org.cbioportal.persistence.mybatis.util;

import java.util.*;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

public class CustomMyBatisObjectFactory extends DefaultObjectFactory {

    public <T> T create(Class<T> type) {
        String typeName = type.getName();
        T toReturn = (T) super.create(type);
        return (toReturn instanceof List)
            ? (T) new LimitedPermissionArrayList<T>()
            : toReturn;
    }

    public <T> T create(
        Class<T> type,
        List<Class<?>> constructorArgTypes,
        List<Object> constructorArgs
    ) {
        String typeName = type.getName();
        T toReturn = (T) super.create(
            type,
            constructorArgTypes,
            constructorArgs
        );
        return (toReturn instanceof List)
            ? (T) new LimitedPermissionArrayList<T>()
            : toReturn;
    }
}
