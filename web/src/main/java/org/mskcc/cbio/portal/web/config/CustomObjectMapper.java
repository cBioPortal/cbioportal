package org.mskcc.cbio.portal.web.config;

import java.util.HashMap;
import java.util.Map;

import org.cbioportal.web.mixin.DataAccessTokenMixin;
import org.cbioportal.weblegacy.mixin.VirtualStudyDataMixin;
import org.cbioportal.model.DataAccessToken;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudyData;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {

        super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        super.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Map<Class<?>, Class<?>> mixinMap = new HashMap<>();
        mixinMap.put(VirtualStudyData.class, VirtualStudyDataMixin.class);
        mixinMap.put(DataAccessToken.class, DataAccessTokenMixin.class);
        super.setMixInAnnotations(mixinMap);
    }
}
