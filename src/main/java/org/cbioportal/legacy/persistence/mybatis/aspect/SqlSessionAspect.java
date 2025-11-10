package org.cbioportal.legacy.persistence.mybatis.aspect;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.cbioportal.legacy.persistence.mybatis.annotation.UseSameSqlSession;
import org.mybatis.spring.SqlSessionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Aspect that manages SqlSession lifecycle for methods annotated with @UseSameSqlSession.
 *
 * This ensures all MyBatis mapper calls within the annotated method share the same SqlSession,
 * providing consistency similar to @Transactional but without requiring actual database transactions.
 */
@Aspect
@Component
public class SqlSessionAspect {

    private static final Logger log = LoggerFactory.getLogger(SqlSessionAspect.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Around("@annotation(org.cbioportal.legacy.persistence.mybatis.annotation.UseSameSqlSession)")
    public Object manageSqlSession(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("SqlSessionAspect triggered for method: {}", joinPoint.getSignature().toShortString());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        UseSameSqlSession annotation = signature.getMethod().getAnnotation(UseSameSqlSession.class);

        if (annotation == null) {
            log.error("Annotation is null for method: {}", signature.getMethod().getName());
            return joinPoint.proceed();
        }

        String sessionFactoryBeanName = annotation.value();
        log.info("Looking for SqlSessionFactory bean: {}", sessionFactoryBeanName);
        SqlSessionFactory sessionFactory = applicationContext.getBean(sessionFactoryBeanName, SqlSessionFactory.class);

        // Check if a SqlSession is already bound to the current thread
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        boolean newSession = false;
        if (holder == null) {
            // Create and bind a new SqlSession
            SqlSession session = sessionFactory.openSession(ExecutorType.SIMPLE);
            holder = new SqlSessionHolder(session, ExecutorType.SIMPLE, null);
            TransactionSynchronizationManager.bindResource(sessionFactory, holder);
            newSession = true;
        }

        try {
            // Execute the method - all mapper calls will use the bound SqlSession
            return joinPoint.proceed();
        } finally {
            // Only unbind and close if we created the session
            if (newSession) {
                TransactionSynchronizationManager.unbindResource(sessionFactory);
                holder.getSqlSession().close();
            }
        }
    }
}