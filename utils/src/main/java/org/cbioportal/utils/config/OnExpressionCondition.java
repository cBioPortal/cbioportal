package org.cbioportal.utils.config;

import org.cbioportal.utils.config.annotation.ConditionalOnExpression;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;

// Adapted from Spring Boot
public class OnExpressionCondition implements Condition {

    public OnExpressionCondition() {
        super();
    }
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String expression = (String) metadata.getAnnotationAttributes(ConditionalOnExpression.class.getName()).get("value");
        expression = wrapIfNecessary(expression);
        expression = context.getEnvironment().resolvePlaceholders(expression);
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory != null) {
            return evaluateExpression(beanFactory, expression);
        }
        return false;
    }

    private Boolean evaluateExpression(ConfigurableListableBeanFactory beanFactory, String expression) {
        BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
        if (resolver == null) {
            resolver = new StandardBeanExpressionResolver();
        }
        BeanExpressionContext expressionContext = new BeanExpressionContext(beanFactory, null);
        Object result = resolver.evaluate(expression, expressionContext);
        return (result != null && (boolean) result);
    }

    private String wrapIfNecessary(String expression) {
        if (!expression.startsWith("#{")) {
            return "#{" + expression + "}";
        }
        return expression;
    }
    
}
