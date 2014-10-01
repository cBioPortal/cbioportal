/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

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