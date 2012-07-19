package org.mskcc.cbio.portal.test.util;

import junit.framework.TestCase;

import java.util.ArrayList;

import org.mskcc.cbio.portal.util.StringListUtil;

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