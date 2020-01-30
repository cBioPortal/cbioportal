/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.maf;

/**
 * Generic utility class providing simple utility functions for tab
 * delimited data files.
 *
 * @author Selcuk Onur Sumer
 */
public class TabDelimitedFileUtil {
    public static final String NA_STRING = "NA";
    public static final long NA_LONG = Long.MIN_VALUE;
    // TODO use MIN instead of -1, we may have fields with negative values
    public static final int NA_INT = -1;
    public static final float NA_FLOAT = -1;

    /**
     * If field is not found in header or data line, or is empty, it just returns empty
     * field value "NA".
     *
     * @param index: index of the column to parse. Can be set to -1 if the column was not found in
     * 				  header. This method will return "NA" in this case.
     * @param parts: the data line parts, i.e. the line split by separator.
     * @return : the value as is, or "NA" if column was empty, not present in file (indicated by index=-1),
     *           or not present in data line (parts parameter above).
     */
    public static String getPartString(int index, String[] parts) {
        try {
            if (parts[index].length() == 0) {
                return NA_STRING;
            } else {
                return parts[index];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return NA_STRING;
        }
    }

    /**
     * Return the trimmed string from the column, or an empty string if -1.
     *
     * Require the column to exist before the end of the data line. This can
     * be used instead of getPartString() if NA may be a meaningful value and
     * the file is expected to have been validated.
     *
     * @param index : index of the column to parse. May be set to -1 if the
     *                column was not found in header, to return "".
     * @param parts: the data line parts, i.e. the line split by separator.
     *
     * @return : the value as is, or "" if the index is -1.
     */
    public static String getPartStringAllowEmpty(int index, String[] parts) {
        try {
            if (index < 0) {
                //return empty string:
                return "";
            }
            //else just return as is, trimmed version:
            return parts[index].trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            // all lines must have the same number of columns, and the
            // validation script should never allow this to reach the loader
            throw new RuntimeException(
                "Unexpected error while parsing column nr: " + (index + 1),
                e
            );
        }
    }

    /**
     * Return the trimmed string from the column, or an empty string if -1
     * or "NA".
     *
     * Require the column to exist before the end of the data line.
     *
     * @param index : index of the column to parse. May be set to -1 if the
     *                column was not found in header, to return "".
     * @param parts: the data line parts, i.e. the line split by separator.
     *
     * @return : the value as is, or "" if the index is -1.
     */
    public static String getPartStringAllowEmptyAndNA(
        int index,
        String[] parts
    ) {
        String value = getPartStringAllowEmpty(index, parts);
        if (value.equals(NA_STRING)) {
            value = "";
        }
        return value;
    }

    public static Long getPartLong(int index, String[] parts) {
        try {
            String part = parts[index];
            return Long.parseLong(part);
        } catch (ArrayIndexOutOfBoundsException e) {
            return NA_LONG;
        } catch (NumberFormatException e) {
            return NA_LONG;
        }
    }

    public static Integer getPartInt(int index, String[] parts) {
        try {
            String part = parts[index];
            return (int) (Float.parseFloat(part));
        } catch (ArrayIndexOutOfBoundsException e) {
            return NA_INT;
        } catch (NumberFormatException e) {
            return NA_INT;
        }
    }

    public static Float getPartPercentage(int index, String[] parts) {
        try {
            float result = NA_FLOAT;
            String part = parts[index];
            if (part.contains("%")) {
                result =
                    Float.parseFloat(part.replace("%", "")) /
                    Float.parseFloat("100");
            } else {
                result = Float.parseFloat(part);
            }
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            return NA_FLOAT;
        } catch (NumberFormatException e) {
            return NA_FLOAT;
        }
    }

    public static Float getPartFloat(int index, String[] parts) {
        try {
            String part = parts[index];
            return Float.parseFloat(part);
        } catch (ArrayIndexOutOfBoundsException e) {
            return NA_FLOAT;
        } catch (NumberFormatException e) {
            return NA_FLOAT;
        }
    }

    // returning MIN_VALUE instead of NA_FLOAT
    // use this one if -1 is not a safe "NA" value.
    public static Float getPartFloat2(int index, String[] parts) {
        try {
            String part = parts[index];
            return Float.parseFloat(part);
        } catch (ArrayIndexOutOfBoundsException e) {
            return Float.MIN_VALUE;
        } catch (NumberFormatException e) {
            return Float.MIN_VALUE;
        }
    }

    public static String adjustDataLine(String dataLine, int headerCount) {
        String line = dataLine;
        String[] parts = line.split("\t", -1);

        // diff should be zero if (# of headers == # of data cols)
        int diff = headerCount - parts.length;

        // number of header columns are more than number of data columns
        if (diff > 0) {
            // append appropriate number of tabs
            for (int i = 0; i < diff; i++) {
                line += "\t";
            }
        }
        // number of data columns are more than number of header columns
        else if (diff < 0) {
            line = "";

            // just truncate the data (discard the trailing columns)
            for (int i = 0; i < headerCount; i++) {
                line += parts[i];

                if (i < headerCount - 1) {
                    line += "\t";
                }
            }
        }

        return line;
    }
}
