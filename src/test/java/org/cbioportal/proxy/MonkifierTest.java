package org.cbioportal.proxy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;


@RunWith(MockitoJUnitRunner.class)
public class MonkifierTest {
    private Monkifier monkifier = new Monkifier();

    @Test
    public void decodeBase64() {
        String encoded = "aW5jbHVkZUV2aWRlbmNl";
        String decoded = this.monkifier.decodeBase64(encoded);
        Assert.assertEquals("includeEvidence", decoded);
    }
    
    @Test
    public void encodeBase64() {
        String plain = "13:g.32914438del";
        String encoded = this.monkifier.encodeBase64(plain);
        Assert.assertEquals("MTM6Zy4zMjkxNDQzOGRlbA==", encoded);
    }
    
    @Test
    public void decodeQueryString() {
        // no param
        Map<String, String[]> encodedQueryParamsEmpty = new HashMap<>();
        String decodedQueryString1 = monkifier.decodeQueryString(encodedQueryParamsEmpty);
        Assert.assertEquals("", decodedQueryString1);
        
        // single param
        Map<String, String[]> encodedQueryParamsSingleParam = new HashMap<>();
        encodedQueryParamsSingleParam.put("aW5jbHVkZUV2aWRlbmNl", new String[]{"ZmFsc2U="});
        String decodedQueryString2 = monkifier.decodeQueryString(encodedQueryParamsSingleParam);
        Assert.assertEquals("includeEvidence=false", decodedQueryString2);

        // multiple param
        Map<String, String[]> encodedQueryParamsMultiParam = new HashMap<>();
        encodedQueryParamsMultiParam.put("aW5jbHVkZUV2aWRlbmNl", new String[]{"ZmFsc2U="});
        encodedQueryParamsMultiParam.put("aGd2c2c=", new String[]{"MTM6Zy4zMjkxNDQzOGRlbA=="});
        
        String decodedQueryString3 = monkifier.decodeQueryString(encodedQueryParamsMultiParam);
        Assert.assertEquals("hgvsg=13:g.32914438del&includeEvidence=false", decodedQueryString3);

        // multiple param with a problematic character '>'
        Map<String, String[]> encodedQueryParamsMultiParamUrlBreaker = new HashMap<>();
        encodedQueryParamsMultiParamUrlBreaker.put("cmVmZXJlbmNlR2Vub21l", new String[]{"R1JDaDM3"});
        encodedQueryParamsMultiParamUrlBreaker.put("aGd2c2c=", new String[]{"NzpnLjE0MDQ1MzEzNkE+VA=="});

        String decodedQueryString4 = monkifier.decodeQueryString(encodedQueryParamsMultiParamUrlBreaker);
        Assert.assertEquals("hgvsg=7:g.140453136A%3ET&referenceGenome=GRCh37", decodedQueryString4);
    }
}
