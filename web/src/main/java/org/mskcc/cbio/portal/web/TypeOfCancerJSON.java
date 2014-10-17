/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.model.TypeOfCancer;

/**
 *
 * @author abeshoua
 */
public class TypeOfCancerJSON {
    public String id;
    public String name;
    public String color;
    public TypeOfCancerJSON(String _id, String _name, String _color) {
        this.id = _id; this.name = _name; this.color = _color;
    }
    public TypeOfCancerJSON(TypeOfCancer model) {
        this.id = model.getTypeOfCancerId();
        this.name = model.getName();
        this.color = model.getDedicatedColor();
    }
    
}
