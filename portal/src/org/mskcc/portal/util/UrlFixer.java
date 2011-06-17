package org.mskcc.portal.util;

public class UrlFixer {

    public static String fixVarLink(String url) {
        if (url != null) {
            if (! url.startsWith("http://")) {
                url = new String ("http://" + url);
            }
            return url.replace("http://172.21.218.211/", "http://xvar.org/");
        } else {
            return url;
        }
    }
}


