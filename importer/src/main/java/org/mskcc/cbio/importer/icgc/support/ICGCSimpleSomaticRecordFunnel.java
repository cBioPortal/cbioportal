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
public enum IcgcSimpleSomaticRecordFunnel  implements Funnel<IcgcSimpleSomaticRecord>{

    INSTANCE;

    @Override
    public void funnel(IcgcSimpleSomaticRecord t, PrimitiveSink into) {
        into.putString(t.getId(), Charset.defaultCharset())
                .putString(t.getProjectCode(),  Charset.defaultCharset())
                .putString(t.getSampleId(), Charset.defaultCharset())
                .putString(t.getChromosome(), Charset.defaultCharset())
                .putString(t.getStart(), Charset.defaultCharset())
                .putString(t.getEnd(), Charset.defaultCharset())
                .putString(t.getRefAllele(), Charset.defaultCharset())
                .putString(t.getMutAllele(), Charset.defaultCharset())
                .putString(t.getTotalReads(), Charset.defaultCharset())
                .putString(t.getMutReads(), Charset.defaultCharset());
    }
    
}
