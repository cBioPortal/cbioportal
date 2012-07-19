package org.mskcc.cbio.cgds.scripts.drug;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DrugDataResource {
    private String name;
    private String resourceURL;
    private String version;

    public DrugDataResource() {
    }

    public DrugDataResource(String name, String resourceURL, String version) {
        this.name = name;
        this.resourceURL = resourceURL;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceURL() {
        return resourceURL;
    }

    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public InputStream getResourceAsStream() throws IOException {
        URL url = new URL(resourceURL);
        return url.openStream();
    }
}
