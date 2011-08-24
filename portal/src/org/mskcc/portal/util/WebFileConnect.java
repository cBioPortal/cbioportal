package org.mskcc.portal.util;

import org.mskcc.cgds.web_api.WebApiUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Web/File Connect Utility Class.
 *
 * @author Ethan Cerami.
 */
public class WebFileConnect {

    /**
     * Retrieves the Content from the Specified File.
     *
     * @param file File Object.
     * @return String Object containing the full Document Content.
     * @throws IOException Read Error.
     */
    public static String retrieveDocument(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        return readFile(in);
    }

    /**
     * Retrieves the Matrix Content from the Specified File.
     *
     * @param file File Object.
     * @return Matrix of Strings.
     * @throws IOException Read Error.
     */
    public static String[][] retrieveMatrix(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String content = readFile(in);
        return prepareMatrix(content);
    }

    /**
     * Parses Matrix Content from the Specified String.
     *
     * @param content Tab-Delim String.
     * @return Matrix of Strings.
     * @throws IOException Read Error.
     */
    public static String[][] parseMatrix(String content) throws IOException {
        return prepareMatrix(content);
    }

    private static String[][] prepareMatrix(String content) {
        String rawLines[] = content.split(WebApiUtil.NEW_LINE);

        // Ignore all lines starting with # sign;  these are comments or warnings, not data.
        ArrayList<String> finalLines = new ArrayList<String>();
        for (String currentLine:  rawLines) {
            if (!currentLine.startsWith("#")) {
                finalLines.add(currentLine);
            }
        }

        String lines[] = finalLines.toArray(new String[finalLines.size()]);
        String matrix[][] = null;
        int numRows = lines.length;
        int numCols = 0;
        if (lines.length > 0) {
            String firstLine = lines[0];
            String parts[] = firstLine.split(WebApiUtil.TAB);
            numCols = parts.length;
        }
        if (numRows > 0 && numCols > 0) {
            matrix = new String[numRows][numCols];
            for (int i = 0; i < lines.length; i++) {
                String parts[] = lines[i].split(WebApiUtil.TAB);
                for (int j = 0; j < parts.length; j++) {
                    matrix[i][j] = parts[j];
                }
            }
        }
        return matrix;
    }

    /**
     * Reads Content from a Buffered Reader.
     */
    private static String readFile(BufferedReader in) throws IOException {
        StringBuffer buf = new StringBuffer();
        String str;
        while ((str = in.readLine()) != null) {
            buf.append(str + "\n");
        }
        in.close();
        return buf.toString();
    }
}
