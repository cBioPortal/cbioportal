package org.cbioportal.test.integration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.*;
import java.util.*;

@Configuration
public class MockMissingBeansPostProcessor implements BeanFactoryPostProcessor, Ordered {

    @Override
    public int getOrder() {
        // Ensure we run after @MockBean (which typically doesn't declare an order).
        // By using LOWEST_PRECEDENCE, we come in last.
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        // We'll need the underlying registry to dynamically add mocks.
        // In a typical Spring Boot test context, beanFactory *should* be a BeanDefinitionRegistry.
        if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
            // Not a registry? Skip or throw an error. For test contexts, it usually is.
            return;
        }

        // 1. Gather all bean definitions
        String[] beanNames = registry.getBeanDefinitionNames();
        Set<Class<?>> missingTypes = new HashSet<>();

        // 2. For each bean definition, find its class and parse @Autowired injection points
        for (String beanName : beanNames) {
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            Class<?> beanClass = resolveBeanClass(bd);
            if (beanClass == null) {
                continue;
            }

            // Check constructor, field, and method injection
            List<Class<?>> neededTypes = findAutowiredTypes(beanClass);
            for (Class<?> neededType : neededTypes) {
                // 3. Check if there's already a bean for the neededType.
                //    This will return not-empty if user has defined an actual bean OR a @MockBean.
                String[] existing = beanFactory.getBeanNamesForType(neededType, true, false);
                if (existing.length == 0) {
                    // -> no existing bean, so we might want to auto-mock it
                    missingTypes.add(neededType);
                }
            }
        }

        // 4. Register MockFactoryBean for each missing type, if we haven't already
        for (Class<?> missingType : missingTypes) {
            if (!containsBeanForType(beanFactory, missingType)) {
                registerMockBean(registry, missingType);
            }
        }
    }

    /**
     * Attempts to resolve the actual class from the BeanDefinition.
     */
    private Class<?> resolveBeanClass(BeanDefinition bd) {
        try {
            // If it's a standard @Bean method
            if (bd.getSource() instanceof org.springframework.core.type.StandardMethodMetadata metadata) {
                return metadata.getIntrospectedMethod().getReturnType();
            }
            // If it's a scanned component or a direct class reference
            else if (bd.getBeanClassName() != null) {
                return ClassUtils.forName(bd.getBeanClassName(), this.getClass().getClassLoader());
            }
        } catch (Exception ex) {
            // Could not resolve
        }
        return null;
    }

    /**
     * Finds all @Autowired injection points (constructor params, fields, method params).
     */
    private List<Class<?>> findAutowiredTypes(Class<?> beanClass) {
        List<Class<?>> dependencies = new ArrayList<>();

        // 1) Look for @Autowired constructors
        for (Constructor<?> ctor : beanClass.getDeclaredConstructors()) {
            if (AnnotationUtils.findAnnotation(ctor, Autowired.class) != null) {
                for (Parameter param : ctor.getParameters()) {
                    dependencies.add(param.getType());
                }
            }
        }

        // 2) Look for @Autowired fields
        for (Field field : beanClass.getDeclaredFields()) {
            if (AnnotationUtils.findAnnotation(field, Autowired.class) != null) {
                dependencies.add(field.getType());
            }
        }

        // 3) Look for @Autowired methods
        for (Method method : beanClass.getDeclaredMethods()) {
            if (AnnotationUtils.findAnnotation(method, Autowired.class) != null) {
                for (Parameter param : method.getParameters()) {
                    dependencies.add(param.getType());
                }
            }
        }

        return dependencies;
    }

    /**
     * Check if there's already a bean for the given type in the factory.
     * This is the same approach as getBeanNamesForType(...) but we encapsulate it here.
     */
    private boolean containsBeanForType(ConfigurableListableBeanFactory beanFactory, Class<?> type) {
        return beanFactory.getBeanNamesForType(type, true, false).length > 0;
    }

    /**
     * Register a MockFactoryBean for the given missing type.
     */
    private void registerMockBean(BeanDefinitionRegistry registry, Class<?> missingType) {
        if (!missingType.getName().startsWith("org.cbioportal.")) {
            return;
        }
        System.out.println("Auto-mocking: " + missingType.getName());
        
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition(MockFactoryBean.class)
            .addConstructorArgValue(missingType);

        // We'll create a name that is unlikely to collide.
        String beanName = "autoMockOf" + missingType.getSimpleName();
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }
}