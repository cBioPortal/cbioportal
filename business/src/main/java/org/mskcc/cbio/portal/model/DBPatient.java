/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 *
 * @author abeshoua
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DBPatient implements Serializable {
    public String id;
    public String internal_id;
    public String study_id;
}
