package org.mskcc.cbio.cgds.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Misc File Utilities.
 *
 * @author Ethan Cerami.
 */
public class FileUtil {
    /**
     * BioPAX File Type.
     */
    public static final int BIOPAX = 0;

    /**
     * PSI_MI File Type.
     */
    public static final int PSI_MI = 1;

    /**
     * External DBs File Type.
     */
    public static final int EXTERNAL_DBS = 2;

    /**
     * Identifiers File Type.
     */
    public static final int IDENTIFIERS = 3;

    /**
     * Unknown File Type.
     */
    public static final int UNKNOWN = 4;

    /**
     * Gets Number of Lines in Specified File.
     *
     * @param file File.
     * @return number of lines.
     * @throws java.io.IOException Error Reading File.
     */
    public static int getNumLines(File file) throws IOException {
        int numLines = 0;
        FileReader reader = new FileReader(file);
        BufferedReader buffered = new BufferedReader(reader);
        String line = buffered.readLine();
        while (line != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                numLines++;
            }
            line = buffered.readLine();
        }
        reader.close();
        return numLines;
    }

    /**
     * Gets Next Line of Input.  Filters out Empty Lines and Comments.
     *
     * @param buf BufferedReader Object.
     * @return next line of input.
     * @throws IOException Error reading input stream.
     */
    public static String getNextLine(BufferedReader buf) throws IOException {
        String line = buf.readLine();
        while (line != null && (line.trim().length() == 0
                || line.trim().startsWith("#"))) {
            line = buf.readLine();
        }
        return line;
    }
}