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

package org.mskcc.cbio.portal.util;

/**
 * Encapsulates a single XDebug log message.
 * An XDebug log message consists of three parts:
 * <UL>
 * <LI>name of the class that invoked the logger
 * <LI>the log message
 * <LI>color of the log message (defaults to black)
 * </UL>
 */
public class XDebugMessage {
    private String message;
    private String className;
    private String color;
    private static final String DEFAULT_COLOR = "black";

    /**
     * Constructor.
     *
     * @param className name of the class that invoke the logger
     * @param message   the log message
     */
    public XDebugMessage(String className, String message) {
        this.className = className;
        this.message = message;
        this.color = DEFAULT_COLOR;
    }

    /**
     * Constructor with Color Parameter.
     *
     * @param className name of the class that invoke the logger
     * @param message   the log message
     * @param color     the log message color
     */
    public XDebugMessage(String className, String message, String color) {
        this.className = className;
        this.message = message;
        this.color = color;
    }

    /**
     * Get Debug Message.
     *
     * @return the log message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get Class Name.
     *
     * @return the class name of the object that invoked the logger
     */
    public String getClassName() {
        return className;
    }

    /**
     * Get Color.
     *
     * @return color of the log message
     */
    public String getColor() {
        return color;
    }
}
