package org.mskcc.cbio.portal.web.config;

import java.util.HashMap;
import java.util.Map;

import org.cbioportal.model.DataAccessToken;
import org.cbioportal.web.mixin.DataAccessTokenMixin;
import org.cbioportal.web.parameter.Session;
import org.cbioportal.web.parameter.StudyPageSettings;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.weblegacy.mixin.SessionMixin;
import org.cbioportal.weblegacy.mixin.StudyPageSettingsMixin;
import org.cbioportal.weblegacy.mixin.VirtualStudyDataMixin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {

        super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        super.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Map<Class<?>, Class<?>> mixinMap = new HashMap<>();
        mixinMap.put(Session.class, SessionMixin.class);
        mixinMap.put(StudyPageSettings.class, StudyPageSettingsMixin.class);
        mixinMap.put(VirtualStudyData.class, VirtualStudyDataMixin.class);
        mixinMap.put(DataAccessToken.class, DataAccessTokenMixin.class);
        super.setMixIns(mixinMap);
    }
}
