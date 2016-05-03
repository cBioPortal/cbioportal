/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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
 * This file is part of cBioPortal CMO-Pipelines.
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

package org.mskcc.cbio.portal.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
* Genome Directed Diagnosis Schema
* <p>
* Output format description for GDD web service
* 
*/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
"darwinAuthResponse",
"p_userName",
"p_dmp_pid"
})

public class DarwinAccess {
    /**
    * 
    * (Required)
    * 
    */
    @JsonProperty("darwinAuthResponse")
    private String darwinAuthResponse;
    /**
    * 
    * (Required)
    * 
    */
    @JsonProperty("p_userName")
    private String p_userName;
    /**
    * 
    * (Required)
    * 
    */
    @JsonProperty("p_dmp_pid")
    private String p_dmp_pid;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    
//    private String darwinAccessUrl;


/**
* No args constructor for use in serialization
* 
*/
public DarwinAccess() {}

/**
* 
* @param darwinAuthResponse
* @param p_userName
* @param p_dmp_pid
*/
public DarwinAccess(String darwinAuthResponse, String p_userName, String p_dmp_pid) {
this.darwinAuthResponse = darwinAuthResponse;
this.p_userName = p_userName;
this.p_dmp_pid = p_dmp_pid;
}

/**
* 
* (Required)
* 
* @return
* The darwinAuthResponse
*/
@JsonProperty("darwinAuthResponse")
public String getDarwinAuthResponse() {
return darwinAuthResponse;
}

/**
* 
* (Required)
* 
* @param darwinAuthResponse
* The Darwin authorization response
*/
@JsonProperty("darwinAuthResponse")
public void setDarwinAuthResponse(String darwinAuthResponse) {
this.darwinAuthResponse = darwinAuthResponse;
}

public DarwinAccess withDarwinAuthResponse(String darwinAuthResponse) {
this.darwinAuthResponse = darwinAuthResponse;
return this;
}

/**
* 
* (Required)
* 
* @return
* The p_userName
*/
@JsonProperty("p_userName")
public String getP_UserName() {
return p_userName;
}

/**
* 
* (Required)
* 
* @param p_userName
* The p_userName
*/
@JsonProperty("p_userName")
public void setP_UserName(String p_userName) {
this.p_userName = p_userName;
}

public DarwinAccess withP_UserName(String p_userName) {
this.p_userName = p_userName;
return this;
}

/**
* 
* (Required)
* 
* @return
* The p_dmp_pid
*/
@JsonProperty("p_dmp_pid")
public String getP_Dmp_Pid() {
return p_dmp_pid;
}

/**
* 
* (Required)
* 
* @param p_dmp_pid
* The p_dmp_pid
*/
@JsonProperty("p_dmp_pid")
public void setP_Dmp_Pid(String p_dmp_pid) {
this.p_dmp_pid = p_dmp_pid;
}

public DarwinAccess withP_Dmp_Pid(String p_dmp_pid) {
this.p_dmp_pid = p_dmp_pid;
return this;
}


@Override
public String toString() {
return ToStringBuilder.reflectionToString(this);
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

public DarwinAccess withAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
return this;
}

}