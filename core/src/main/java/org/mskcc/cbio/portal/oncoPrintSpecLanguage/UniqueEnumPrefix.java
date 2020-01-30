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

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import static java.lang.System.out;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Given an enum and a String,
 * map any unique prefix to a value of the enum. Cool, huh?
 * return null if not found
 * @author Arthur Goldberg
 *
 */
public class UniqueEnumPrefix {

    public static Object findUniqueEnumMatchingPrefix(
        Class<?> theEnum,
        String abbrev
    )
        throws IllegalArgumentException {
        if (null == theEnum || !theEnum.isEnum()) {
            throw new IllegalArgumentException("Not an enum: " + theEnum);
        }

        if (null == abbrev) {
            throw new IllegalArgumentException("abbrev is null");
        }

        Object maybe = null;
        for (Object o : theEnum.getEnumConstants()) {
            //out.format( "findUniqueEnumMatchingPrefix: matching '%s' with '%s'%n", o.toString(), abbrev );

            if (
                o.toString().regionMatches(true, 0, abbrev, 0, abbrev.length())
            ) {
                //out.format( "findUniqueEnumMatchingPrefix: '%s' matches '%s'%n", o.toString(), abbrev );
                if (maybe != null) {
                    // Another match; not unique
                    //out.format( "findUniqueEnumMatchingPrefix: ret null%n");
                    return null;
                } else {
                    maybe = o;
                }
            }
        }
        //out.format( "findUniqueEnumMatchingPrefix: ret %s%n", maybe.toString());
        return maybe;
    }

    static String convertError =
        "Invalid abbreviation, does not match either a unique substring or a unique prefix: ";

    /**
     * find unique name prefix in nicknames of an enum.
     * if theEnum is an enum with a getNicknames method look for unique prefix matches to the
     * nicknames.
     *
     * @param theEnum an enum that implements getNicknames() which returns String[]
     * @param abbrev an abbreviation that might match a unique prefix of a nickname
     * @return the enum constant if a unique match, otherwise null
     * @throws IllegalArgumentException
     */
    public static Object findUniqueEnumWithNicknameMatchingPrefix(
        Class<?> theEnum,
        String abbrev
    )
        throws IllegalArgumentException {
        if (null == theEnum || !theEnum.isEnum()) {
            throw new IllegalArgumentException("Not an enum: " + theEnum);
        }
        //out.format("invoking uniqueEnumNicknamePrefix on %s%n", theEnum.getName());

        if (null == abbrev) {
            throw new IllegalArgumentException("abbrev is null");
        }

        try {
            Object possibleEnumConst = null;
            Class<?>[] argTypes = new Class[] {};
            Method getNicknames = theEnum.getDeclaredMethod(
                "getNicknames",
                argTypes
            );

            for (Object enumConstant : theEnum.getEnumConstants()) {
                // match any string for a given enum constant
                String[] nicknames = null;
                nicknames = (String[]) getNicknames.invoke(enumConstant);

                Object thisEnumConst = null;
                if (nicknames != null) {
                    for (String nickname : nicknames) {
                        if (
                            nickname.regionMatches(
                                true,
                                0,
                                abbrev,
                                0,
                                abbrev.length()
                            )
                        ) {
                            thisEnumConst = enumConstant;
                            // out.format("%s matches %s for %s%n", nickname, abbrev, enumConstant.toString());
                        }
                    }
                }

                if (thisEnumConst != null) {
                    // don't match multiple enum constants
                    if (possibleEnumConst != null) {
                        return null;
                    } else {
                        possibleEnumConst = thisEnumConst;
                    }
                }
            }
            return possibleEnumConst;
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                "Enum must implement 'getNicknames()': " + theEnum
            );
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null; // for compiler
    }
}
