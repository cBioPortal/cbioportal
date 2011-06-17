package org.mskcc.portal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
     * @return String Object containing the full Document Content.
     * @throws IOException Read Error.
     */
    public static String[][] retrieveMatrix(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String content = readFile(in);
        String lines[] = content.split("\n");
        String matrix[][] = null;
        int numRows = lines.length;
        int numCols = 0;
        if (lines.length > 0) {
            String firstLine = lines[0];
            String parts[] = firstLine.split("\t");
            numCols = parts.length;
        }
        if (numRows > 0 && numCols > 0) {
            matrix = new String[numRows][numCols];
            for (int i = 0; i < lines.length; i++) {
                String parts[] = lines[i].split("\t");
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
