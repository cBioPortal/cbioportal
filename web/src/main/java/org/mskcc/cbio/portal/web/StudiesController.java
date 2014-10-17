/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.model.CancerStudy;
/**
 *
 * @author abeshoua
 */


public class StudiesController {

  public static ArrayList<CancerStudyJSON> getStudies() throws DaoException {
      ArrayList<CancerStudy> studies = DaoCancerStudy.getAllCancerStudies();
      ArrayList<CancerStudyJSON> ret = new ArrayList<>();
      for (CancerStudy cs: studies) {
          ret.add(new CancerStudyJSON(cs));
      }
      return ret;
  }
  public static ArrayList<CancerStudyJSON> getStudy(String id) throws DaoException {
      ArrayList<CancerStudyJSON> ret = new ArrayList<>();
      ret.add(new CancerStudyJSON(DaoCancerStudy.getCancerStudyByStableId(id)));
      return ret;
  }
  public static ArrayList<CancerStudyJSON> getStudy(int internalId) throws DaoException {
      ArrayList<CancerStudyJSON> ret = new ArrayList<>();
      ret.add(new CancerStudyJSON(DaoCancerStudy.getCancerStudyByInternalId(internalId)));
      return ret;
  }
}
