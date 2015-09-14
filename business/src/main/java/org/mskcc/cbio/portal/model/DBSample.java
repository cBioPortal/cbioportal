/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;

/**
 *
 * @author abeshoua
 */
@JsonInclude(Include.NON_NULL)
public class DBSample implements Serializable {
    public String id;
    public String sample_type;
    public String patient_id;
    public String study_id;
    public String internal_id;
}
