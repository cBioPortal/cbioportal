/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.util;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Tests the String List Util Class.
 *
 * @author Ethan Cerami.
 */
public class TestStringListUtil extends TestCase {

    public void testItems1() {
        ArrayList<String> itemList = new ArrayList<String>();
        String itemSentence = StringListUtil.covertItemsIntoSentence(itemList);
        assertEquals ("", itemSentence);
    }

    public void testItems2() {
        ArrayList<String> itemList = new ArrayList<String>();
        itemList.add("apple");
        String itemSentence = StringListUtil.covertItemsIntoSentence(itemList);
        assertEquals ("apple.", itemSentence);
    }

    public void testItems3() {
        ArrayList<String> itemList = new ArrayList<String>();
        itemList.add("apple");
        itemList.add("orange");
        String itemSentence = StringListUtil.covertItemsIntoSentence(itemList);
        assertEquals ("apple, orange.", itemSentence);
    }

    public void testItems4() {
        ArrayList<String> itemList = new ArrayList<String>();
        itemList.add("apple");
        itemList.add("orange");
        itemList.add("banana");
        String itemSentence = StringListUtil.covertItemsIntoSentence(itemList);
        assertEquals ("apple, orange, banana.", itemSentence);
    }
}