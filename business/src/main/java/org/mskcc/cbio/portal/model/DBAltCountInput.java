/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jiaojiao
 */
public class DBAltCountInput implements Serializable {
    public String type;
    public Boolean per_study;
    public List<Map<String, String>> data;
    public List<String> echo;
    public List<String> studyId;
}
