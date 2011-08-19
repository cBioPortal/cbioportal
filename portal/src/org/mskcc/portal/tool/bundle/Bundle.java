package org.mskcc.portal.tool.bundle;

import org.mskcc.portal.model.ProfileData;
import org.mskcc.cgds.model.ExtendedMutation;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Bundle {

    public abstract ArrayList<GeneRequest> getGeneRequestList();

    public abstract String getCaseSetId();

//    public abstract boolean isEpigeniticallySilenced (String caseId, String gene, ProfileData mergedProfile);
//
//    public abstract String getCnaCall (String caseId, String gene, ProfileData mergedProfile);

    public abstract int binAccept (String caseId,
       HashMap<String, ArrayList<ExtendedMutation>> mutationMap,
       HashMap <String, ProfileData> cnaMap,
       HashMap <String, ProfileData> binaryMethylationMap);

    public abstract String getSummary();
}
