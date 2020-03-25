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
package org.cbioportal.web.config;

import org.cbioportal.web.DataAccessTokenController;
import org.cbioportal.web.OAuth2DataAccessTokenController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
    @PropertySource("file:///${PORTAL_HOME}/portal.properties", ignoreResourceNotFound=true),
    @PropertySource("classpath:portal.properties", ignoreResourceNotFound=true)
})
public class DataAccessTokenControllerConfig {

    @Bean
    @Conditional(OAuth2DatMethodCondition.class)
    public OAuth2DataAccessTokenController oauth2Controller() {
        return new OAuth2DataAccessTokenController();
    }

    @Bean
    @Conditional(OtherDatMethodCondition.class)
    public DataAccessTokenController otherController() {
        return new DataAccessTokenController();
    }

}