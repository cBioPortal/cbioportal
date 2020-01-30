package org.mskcc.cbio.portal.web.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.HashMap;
import java.util.Map;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.session_service.domain.Session;
import org.cbioportal.web.mixin.DataAccessTokenMixin;
import org.cbioportal.web.parameter.PageSettings;
import org.cbioportal.web.parameter.PageSettingsData;
import org.cbioportal.web.parameter.StudyPageSettings;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.weblegacy.mixin.SessionDataMixin;
import org.cbioportal.weblegacy.mixin.SessionMixin;

public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {
        super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        super.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Map<Class<?>, Class<?>> mixinMap = new HashMap<>();
        mixinMap.put(Session.class, SessionMixin.class);
        mixinMap.put(VirtualStudy.class, SessionMixin.class);
        mixinMap.put(VirtualStudyData.class, SessionDataMixin.class);
        mixinMap.put(PageSettings.class, SessionMixin.class);
        mixinMap.put(StudyPageSettings.class, SessionDataMixin.class);
        mixinMap.put(PageSettingsData.class, SessionDataMixin.class);
        mixinMap.put(DataAccessToken.class, DataAccessTokenMixin.class);
        super.setMixIns(mixinMap);
    }
}
