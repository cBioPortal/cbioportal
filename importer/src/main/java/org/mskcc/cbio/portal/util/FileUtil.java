/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.io.*;

/**
 * Misc File Utilities.
 *
 * @author Ethan Cerami.
 */
public class FileUtil {

    /**
     * Gets Number of Lines in Specified File.
     *
     * @param file File.
     * @return number of lines.
     * @throws java.io.IOException Error Reading File.
     */
    public static int getNumLines(File file) throws IOException {
        int numLines = 0;
        try (FileReader reader = new FileReader(file); BufferedReader buffered = new BufferedReader(reader)) {
            String line = buffered.readLine();
            while (line != null) {
                if (TsvUtil.isDataLine(line)) {
                    numLines++;
                }
                line = buffered.readLine();
            }
            return numLines;
        }
    }

}
