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

package org.mskcc.cbio.portal.util;


import com.vlkan.hrrs.api.HttpRequestRecordWriter;
import com.vlkan.hrrs.api.HttpRequestRecordWriterTarget;
import com.vlkan.hrrs.serializer.base64.Base64HttpRequestRecord;
import com.vlkan.hrrs.serializer.base64.Base64HttpRequestRecordWriter;
import com.vlkan.hrrs.serializer.base64.guava.GuavaBase64Encoder;
import com.vlkan.hrrs.serializer.file.HttpRequestRecordWriterRotatingFileTarget;
import com.vlkan.hrrs.servlet.HrrsFilter;
import com.vlkan.rfos.RotationConfig;
import com.vlkan.rfos.policy.DailyRotationPolicy;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.*;
import org.cbioportal.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

public class DebugHrrsFilter extends HrrsFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugHrrsFilter.class);

    private static final String DEFAULT_HRRS_LOGGING_NAME = "/hrrs-logging.csv";
    
    private final HttpRequestRecordWriter<String> writer;
    
    private final HttpRequestRecordWriterTarget<String> writerTarget;

    public DebugHrrsFilter() {
        boolean enableHrrsLogging = Boolean.valueOf(GlobalProperties.getProperty("hrrs.enable.logging"));
        // only initialize writers/writing target if logging is enabled in properties file
        if (enableHrrsLogging) {
            RotationConfig rotationConfig = createRotationConfig();
            this.writerTarget = new HttpRequestRecordWriterRotatingFileTarget(rotationConfig, Base64HttpRequestRecord.CHARSET);
            this.writer = new Base64HttpRequestRecordWriter(writerTarget, GuavaBase64Encoder.getInstance());
            this.setEnabled(enableHrrsLogging);
        } else {
            this.writer = null;
            this.writerTarget = null;
        }
    }

    public HttpRequestRecordWriterTarget<String> getWriterTarget() {
        return writerTarget;
    }

    @Override
    protected HttpRequestRecordWriter<String> getWriter() {
        return writer;
    }

    private static RotationConfig createRotationConfig() {
        // default to tmpdir if logging is enabled but no logging path is provided
        String hrrsLoggingFilePath = (!StringUtils.isEmpty(GlobalProperties.getProperty("hrrs.logging.filepath")) ? GlobalProperties.getProperty("hrrs.logging.filepath") : System.getProperty("java.io.tmpdir") + DEFAULT_HRRS_LOGGING_NAME);
        String file = new File(hrrsLoggingFilePath).getAbsolutePath();
        String filePattern = new File(hrrsLoggingFilePath + "-%d{yyyyMMdd-HHmmss-SSS}.csv").getAbsolutePath();
        RotationConfig rotationConfig = RotationConfig
                .builder()
                .file(file)
                .filePattern(filePattern)
                .policy(DailyRotationPolicy.getInstance())
                .build();
        return rotationConfig;
    }
 
    @Override
    public void destroy() {
        try {
            writerTarget.close();
        } catch (IOException error) {
            LOGGER.error("failed closing writer", error);
        }
    }
}
