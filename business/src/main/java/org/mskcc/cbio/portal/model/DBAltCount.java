package org.mskcc.cbio.portal.model;


//import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jiaojiao
 */
//@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonInclude(JsonInclude.Include.NON_DEFAULT)

public class DBAltCount implements Serializable {
    public int count;
    public double frequency;
    public String studyID;
    public String gene;
    public int start;
    public int end;
     
}
