/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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
package org.mskcc.cbio.portal.scripts;

import java.util.Properties;

@SuppressWarnings("serial")
/**
 * This class overrides getProperties method to return the trimmed
 * value of the property.
 *
 * @author pieterlukasse
 *
 */
public class TrimmedProperties extends Properties {

    @Override
    public String getProperty(String key) {
        if (super.getProperty(key) == null) return null; else return super
            .getProperty(key)
            .trim();
    }
}
