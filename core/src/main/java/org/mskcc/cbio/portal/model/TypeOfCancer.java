/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.model;

/**
 * A TypeOfCancer is a clinical cancer type, such as Glioblastoma, Ovarian, etc.
 * Eventually, we'll have ontology problems with this, but initially the dbms will
 * be loaded from a file with a static table of types.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class TypeOfCancer {

   private String name;
   private String typeOfCancerId;
   // Separated by commas
   private String clinicalTrialKeywords = "";

   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public String getTypeOfCancerId() {
      return typeOfCancerId;
   }
   public void setTypeOfCancerId(String typeOfCancerId) {
      this.typeOfCancerId = typeOfCancerId;
   }

   public String getClinicalTrialKeywords() {
      return clinicalTrialKeywords;
   }

   public void setClinicalTrialKeywords(String clinicalTrialKeywords) {
      this.clinicalTrialKeywords = clinicalTrialKeywords;
   }
}
