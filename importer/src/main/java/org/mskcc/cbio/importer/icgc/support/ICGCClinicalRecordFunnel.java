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

package org.mskcc.cbio.importer.icgc.support;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import java.nio.charset.Charset;

/**
 *
 * @author criscuof
 */
public enum IcgcClinicalRecordFunnel  implements Funnel<IcgcClinicalRecord>{

    INSTANCE;

    @Override
    public void funnel(IcgcClinicalRecord t, PrimitiveSink into) {
        into.putString(t.getDonorId(), Charset.defaultCharset())
                .putString(t.getIcgcSpecimenId(),  Charset.defaultCharset())
                .putString(t.getSubmittedDonorId(), Charset.defaultCharset())
                .putString(t.getSubmittedSpecimenId(), Charset.defaultCharset());
                
    }
    
}
