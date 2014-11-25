package org.mskcc.cbio.importer.persistence.staging.segment;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Created by fcriscuo on 11/15/14.
 */
public abstract class SegmentModel {

    public abstract String getID();
    public abstract String getChromosome();
    public abstract String getLocStart();
    public abstract String getLocEnd();
    public abstract String getNumMark();
    public abstract String getSegMean();

    public static final Map<String,String> transformationMap = Maps.newTreeMap();
    static {
        transformationMap.put("001ID",  "getID");
        transformationMap.put("002chromosome", "getChromosome");
        transformationMap.put("003loc.start", "getLocStart");
        transformationMap.put("004loc.end","getLocEnd");
        transformationMap.put("005num.mark", "getNumMark");
        transformationMap.put("006seg.mean", "getSegMean");
    }

    public static List<String> resolveColumnNames() {
        return FluentIterable.from(transformationMap.keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

    final static Function<SegmentModel, String> transformationFunction = new Function<SegmentModel, String>() {
        @Override
        public String apply(final SegmentModel sm) {
            Set<String> attributeList = transformationMap.keySet();
            List<String> segAttributes = FluentIterable.from(attributeList)
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String attribute) {
                            String getterName = transformationMap.get(attribute);
                            return StagingUtils.pojoStringGetter(getterName, sm);

                        }
                    }).toList();
            String retRecord = StagingCommonNames.tabJoiner.join(segAttributes);

            return retRecord;
        }

    };

    /*
    provide public access to the transformation function
     */
    public static Function<SegmentModel,String> getTransformationModel () { return transformationFunction;}

}
