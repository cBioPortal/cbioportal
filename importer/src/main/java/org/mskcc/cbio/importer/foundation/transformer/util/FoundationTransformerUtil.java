package org.mskcc.cbio.importer.foundation.transformer.util;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.model.FoundationMetadata;

import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
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
 * <p/>
 * Created by criscuof on 11/11/14.
 */
public class FoundationTransformerUtil {


    // resolve the FoundationMetadata object  based on a partial match to the Foundation XML file name
    public static  Optional<FoundationMetadata> resolveFoundationMetadataFromXMLFilename(final Config config, final String filename) {
        final Collection<FoundationMetadata> mdc = config.getFoundationMetadata();
        final List<String> fileList = Lists.newArrayList(filename);
        return FluentIterable.from(mdc)
                .firstMatch(new Predicate<FoundationMetadata>() {
                    @Override
                    public boolean apply(final FoundationMetadata meta) {
                        List<String> fl = FluentIterable.from(fileList).filter(meta.getRelatedFileFilter()).toList();
                        return (!fl.isEmpty());
                    }
                });
    }
}
