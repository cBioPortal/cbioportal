/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/
package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.service.EntityAttributeService;;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Controller
@RequestMapping("/attributes")
public class AttributesController
{
	@Autowired
	private EntityAttributeService entityAttributeService;

  @RequestMapping(value="/clinical")
  public @ResponseBody List<AttributeMetadata> getClinicalAttributes()
  {
    return entityAttributeService.getAllAttributeMetadata();
  }

  @RequestMapping(value="/clinical/{study}")
  public @ResponseBody List<AttributeMetadata> getClinicalAttribute(@PathVariable String study,
                                                                    @RequestParam(value="patient", required=false) String patient,
                                                                    @RequestParam(value="sample", required=false) String sample)
  {
    return new java.util.ArrayList<AttributeMetadata>();
  }
}