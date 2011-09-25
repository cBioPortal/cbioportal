package org.mskcc.portal.html.special_gene;

import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.portal.mapback.MapBack;
import org.mskcc.portal.mapback.Brca1;
import org.mskcc.portal.mapback.Brca2;
import org.mskcc.portal.html.HtmlUtil;

import java.util.ArrayList;

/**
 * Special Gene Implementation for BRCA2.
 *
 * @author Ethan Cerami.
 */
class SpecialGeneBrca2 extends SpecialGene {
    public static final String BRCA2 = "BRCA2";

    public ArrayList<String> getDataFieldHeaders() {
        ArrayList<String> headerList = new ArrayList<String>();
        headerList.add("NT Position *");
        headerList.add("Notes");
        return headerList;
    }

    public String getFooter() {
        return ("* Known BRCA2 6174delT founder mutation are noted.");
    }

    public ArrayList<String> getDataFields(ExtendedMutation mutation) {
        ArrayList<String> dataFields = new ArrayList<String>();
        MapBack mapBack = new MapBack(new Brca2(), mutation.getEndPosition());
        long ntPosition = mapBack.getNtPositionWhereMutationOccurs();
        String annotation = getAnnotationBrca2(ntPosition);
        setNtPosition(ntPosition, dataFields);
        dataFields.add(HtmlUtil.getSafeWebValue(annotation));
        return dataFields;
    }

    private static String getAnnotationBrca2(long nt) {
        if (nt == 6174) {
            return "6174delT founder mutation.";
        } else {
            return null;
        }
    }
}