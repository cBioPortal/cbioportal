package org.mskcc.cbio.portal.r_bridge;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Gets an R Template.
 *
 * @author Ethan Cerami.
 */
public class RTemplate {
    private StringBuffer content;

    /**
     * Constructor.
     * @param templateFileName  Template File Name.
     * @throws IOException      IO Error.
     */
    public RTemplate (String templateFileName) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(templateFileName);
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(in));
        content = new StringBuffer();
        String line = bufReader.readLine();
        while (line != null) {
            line = line.trim();
            content.append(line + "\n");
            line = bufReader.readLine();
        }
        in.close();
    }

    /**
     * Gets the R Template.
     * @return R Template.
     */
    public String getRTemplate() {
        return content.toString();
    }
}