package org.mskcc.portal.tool;

import org.mskcc.portal.util.ValueParser;

public class AlterationToPlainEnglish {

    public static String getMutationCall (ValueParser value) {
        if (value.isMutated()) {
            return "Mutated";
        } else {
            return "---";
        }
    }

    public static String getCnaCall (ValueParser value) {

        return value.getCnaValue();
//        if (value.isCnaHomozygouslyDeleted()) {
//            return "Homozygously Deleted";
//        } else if (value.isCnaHemizygouslyDeleted()) {
//            return "LOH";
//        } else if (value.isCnaAmplified()) {
//            return "AMP";
//        } else {
//            //return "---";
//        }
    }
}
