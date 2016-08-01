package org.cbioportal.service.impl.util;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DaoMutSig.class})
public class MutSigUtilTest {

    @InjectMocks
    private MutSigUtil mutSigUtil;

    @Test
    public void getMutSig() throws Exception {

        PowerMockito.mockStatic(DaoMutSig.class);

        int cancerStudyId = 3;
        long entrezGeneId = 12;
        String hugoGeneSymbol = "GENE";
        float qValue = 32;

        MutSig mutSig = new MutSig();
        CanonicalGene canonicalGene = new CanonicalGene(entrezGeneId, hugoGeneSymbol);
        mutSig.setCanonicalGene(canonicalGene);
        mutSig.setqValue(qValue);
        List<MutSig> mutSigList = new ArrayList<>();
        mutSigList.add(mutSig);
        Mockito.when(DaoMutSig.getAllMutSig(cancerStudyId)).thenReturn(mutSigList);

        Map<Long, Double> result = mutSigUtil.getMutSig(cancerStudyId);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(qValue, (float) result.get(entrezGeneId).doubleValue());
    }
}