/**
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */
package org.mskcc.cbio.importer.model;

import com.google.common.base.Splitter;
import com.google.gdata.util.common.base.Preconditions;
import com.google.inject.internal.Lists;
import java.util.List;

/**
 * Class which contains tumor type metadata.
 */
public class FoundationMetadata {

    // delimiter between tumor type & name within the reference file
   public static final Splitter semicolonSplitter = Splitter.on(';');

    // bean properties
    private String cancerStudy;
    private List<String> dependencies;

    /**
     * Create a TumorTypeMetadata instance with properties in given array. Its
     * assumed order of properties is that from google worksheet.
     *
     * @param properties String[]
     */
    public FoundationMetadata(String[] properties) {
        Preconditions.checkArgument(null != properties, "No properties have been provided");
        Preconditions.checkArgument(properties.length < 2, "Insufficient number of properties provided");
        this.cancerStudy = properties[0].trim();
        this.dependencies = Lists.newArrayList(semicolonSplitter.split(properties[1]));
        

    }
}
