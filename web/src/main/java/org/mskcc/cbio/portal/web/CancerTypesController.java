/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.springframework.web.bind.annotation.ResponseBody;
/**
 *
 * @author abeshoua
 */


public class CancerTypesController {

  public static ArrayList<TypeOfCancerJSON> getCancerTypes() throws DaoException {
      ArrayList<TypeOfCancer> types = DaoTypeOfCancer.getAllTypesOfCancer();
      ArrayList<TypeOfCancerJSON> ret = new ArrayList<>();
      for (TypeOfCancer t: types) {
          ret.add(new TypeOfCancerJSON(t));
      }
      return ret;
  }
  public static ArrayList<TypeOfCancerJSON> getCancerType(String id) throws DaoException {
      ArrayList<TypeOfCancerJSON> ret = new ArrayList<>();
      ret.add(new TypeOfCancerJSON(DaoTypeOfCancer.getTypeOfCancerById(id)));
      return ret;
  }
}
