package org.mskcc.cbio.importer.persistence.staging.clinical;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;
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
 * Created by criscuof on 1/25/15.
 */
public abstract class ImpactClinicalDataModel {

    public static final String SAMPLE_ID_COLUMN_NAME = "SAMPLE_ID";

    public static List<String> resolveColumnNames() {
        return FluentIterable.from(transformationMap.keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

    public static final Map<String,String> transformationMap = Maps.newTreeMap();
    static {
        transformationMap.put("001SAMPLE_ID", "getSampleId"); //1
        transformationMap.put("002PATIENT_ID", "getPatientId"); //2
        transformationMap.put("003CANCER_TYPE","getCancerType" ); //3
        transformationMap.put("004SAMPLE_TYPE","getSampleType" ); //4
        transformationMap.put("005SAMPLE_CLASS","getSampleClass" ); //5
        transformationMap.put("006METASTATIC_SITE","getMetastaticSite" );
        transformationMap.put("007PRIMARY_SITE","getPrimarySite"); //7
        transformationMap.put("008CANCER_TYPE_DETAILED","getCancerTypeDetailed"); //8
        transformationMap.put("009KNOWN_MOLECULAR_CLASSIFIER","getKnownMolecularClassifier" ); //9
    }
    /*
    abstract getters that a subclass must implement
    */

    public abstract String getSampleId();
    public abstract String getPatientId();
    public abstract String getCancerType();
    public abstract String getSampleType();
    public abstract String getSampleClass();
    public abstract String getMetastaticSite();
    public abstract String getPrimarySite();
    public abstract String getCancerTypeDetailed();
    public abstract String getKnownMolecularClassifier();

    /*
Function to transform attributes from a  Foundation Short Variant object into MAF attributes collected in
a tsv String for subsequent output
*/
    final static Function<ImpactClinicalDataModel, String> transformationFunction =
            new Function<ImpactClinicalDataModel, String>() {
        @Override
        public String apply(final ImpactClinicalDataModel icdm) {
            Set<String> attributeList = transformationMap.keySet();
            List<String> mafAttributes = FluentIterable.from(attributeList)
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String attribute) {
                            String getterName = transformationMap.get(attribute);
                            return StagingUtils.pojoStringGetter(getterName, icdm);

                        }
                    }).toList();
            String retRecord = StagingCommonNames.tabJoiner.join(mafAttributes);

            return retRecord;
        }

    };

    /*
     provide access to the transformation function
      */
    public static Function<ImpactClinicalDataModel,String> getTransformationFunction () { return transformationFunction;}


}
