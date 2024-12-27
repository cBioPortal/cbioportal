package org.cbioportal.test.integration;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

public class MockFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> type;

    public MockFactoryBean(Class<T> type) {
        this.type = type;
    }

    @Override
    public T getObject() {
        return Mockito.mock(type);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}