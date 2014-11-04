/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
  * 
  *  This library is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  *  documentation provided hereunder is on an "as is" basis, and
  *  Memorial Sloan-Kettering Cancer Center 
  *  has no obligations to provide maintenance, support,
  *  updates, enhancements or modifications.  In no event shall
  *  Memorial Sloan-Kettering Cancer Center
  *  be liable to any party for direct, indirect, special,
  *  incidental or consequential damages, including lost profits, arising
  *  out of the use of this software and its documentation, even if
  *  Memorial Sloan-Kettering Cancer Center 
  *  has been advised of the possibility of such damage.
 */

package org.mskcc.cbio.importer.persistence.staging;

import com.google.inject.internal.Preconditions;
import org.mskcc.cbio.importer.model.CaseListMetadata;

/*
responsible for CRUD operations against a cbio staging file specified
via a CaseStudyMetadata object
*/
public class CaseListFileHandler {
    private final CaseListMetadata metadata;
    
    public CaseListFileHandler(CaseListMetadata meta) {
        Preconditions.checkArgument(null != meta,
                "A CaselistMetadata object is required");
        this.metadata = meta;
    }
    
    private class CaseList{
        
    }
}
