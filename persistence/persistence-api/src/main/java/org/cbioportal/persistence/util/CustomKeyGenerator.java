/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.persistence.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.CacheEnabledConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.DigestUtils;


public class CustomKeyGenerator implements KeyGenerator {
    public static final String DELIMITER = "_";

    @Autowired
    private CacheEnabledConfig cacheEnabledConfig;
    
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(CustomKeyGenerator.class);

    public Object generate(Object target, Method method, Object... params) {
        if (!cacheEnabledConfig.isEnabled()) {
            return "";
        }
        String key = target.getClass().getSimpleName() + DELIMITER
            + method.getName() + DELIMITER
            + Arrays.stream(params)
                .map(this::exceptionlessWrite)
                .collect(Collectors.joining(DELIMITER));
        LOG.debug("Created key: " + key);
        return key;
    }
    
    private String exceptionlessWrite(Object toSerialize) {
        if (toSerialize instanceof Select && ((Select) toSerialize).hasAll()) {
            // Select implements Iterable, but Select.All throws an exception
            // when you call iterator(), which breaks Jackson, so we need some custom logic
            return "Select.ALL";
        }
        try {
            String json = mapper.writeValueAsString(toSerialize);
            if (json.length() > 1024) {
                // hash long keys
                return DigestUtils.md5DigestAsHex(json.getBytes());
            } else {
                // leave short keys intact, but remove semicolons to make things look cleaner in redis
                return json.replaceAll(":", DELIMITER);
            }
        } catch (JsonProcessingException e) {
            LOG.error("Could not serialize param to string: ", e);
            return "";
        }
    }
}
