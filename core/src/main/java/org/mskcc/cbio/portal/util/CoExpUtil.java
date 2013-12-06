package org.mskcc.cbio.portal.util;

import org.json.simple.JSONObject;
import java.util.ArrayList;

public class CoExpUtil {

    //Sort json objects array by propName
    public static ArrayList<JSONObject> sortJsonArr(ArrayList<JSONObject> jsonArr, String propName) {

        for (int mainIndex = 0 ; mainIndex < jsonArr.size(); mainIndex++) {
            for (int comparedIndex = mainIndex + 1; comparedIndex < jsonArr.size(); comparedIndex++) {
                JSONObject mainObj = jsonArr.get(mainIndex);
                JSONObject comparedObj = jsonArr.get(comparedIndex);
                double mainScore = (Double)mainObj.get(propName);
                double comparedScore = (Double)comparedObj.get(propName);
                if (Math.abs(mainScore) > Math.abs(comparedScore)) {
                    jsonArr.add(mainIndex, comparedObj);
                    jsonArr.remove(mainIndex + 1);
                    jsonArr.add(comparedIndex, mainObj);
                    jsonArr.remove(comparedIndex + 1);
                }
            }
        }
        return jsonArr;
    }

}
