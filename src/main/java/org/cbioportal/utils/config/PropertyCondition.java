/*
 * Copyright (c) 2020 The Hyve B.V.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cbioportal.utils.config;

import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;
import java.util.stream.Stream;

@PropertySources({
        @PropertySource(value="classpath:application.properties", ignoreResourceNotFound=true),
        @PropertySource(value="file:///${PORTAL_HOME}/application.properties", ignoreResourceNotFound=true)
})
// Adapted from Spring Boot
public class PropertyCondition implements Condition {

    public PropertyCondition() {
        super();
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnProperty.class.getName());
        String name = (String) attributes.get("name");
        Object requiredValue = attributes.get("havingValue");
        boolean matchIfMissing = (boolean) attributes.get("matchIfMissing");
        boolean isNot = (boolean) attributes.get("isNot");
        String actualValue = context.getEnvironment().getProperty(name);
        if (actualValue == null)
            return matchIfMissing;
        if (requiredValue instanceof String[]) {
            if (isNot)
                return Stream.of((String[]) requiredValue).noneMatch(value -> value.equalsIgnoreCase(actualValue));
            return Stream.of((String[]) requiredValue).anyMatch(value -> value.equalsIgnoreCase(actualValue));
        }
            return isNot ? !((String) requiredValue).equalsIgnoreCase(actualValue) : ((String) requiredValue).equalsIgnoreCase(actualValue);
    }

}
