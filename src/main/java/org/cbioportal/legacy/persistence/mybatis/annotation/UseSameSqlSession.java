package org.cbioportal.legacy.persistence.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to ensure all MyBatis mapper calls within a method
 * use the same SqlSession. This is useful when @Transactional
 * cannot be used (e.g., with ClickHouse).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseSameSqlSession {
    /**
     * The SqlSessionFactory bean name to use.
     * Defaults to "sqlSessionFactory".
     */
    String value() default "sqlSessionFactory";
}