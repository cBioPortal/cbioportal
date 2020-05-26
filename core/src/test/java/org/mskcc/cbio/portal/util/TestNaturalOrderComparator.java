/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author debruiji
 */
public class TestNaturalOrderComparator {
	@Test
	public void testGuessOncotatorKeyword() throws Exception {
		String[] strings = new String[]{"1-2", "1-02", "1-20", "10-20", "fred", "jane", "pic01",
			"pic2", "pic02", "pic02a", "pic3", "pic4", "pic 4 else", "pic 5", "pic05", "pic 5",
			"pic 5 something", "pic 6", "pic   7", "pic33", "pic100", "pic100a", "pic120", "pic121",
			"pic02000", "tom", "x2-g8", "x2-y7", "x2-y08", "x8-y8"};
		
		List scrambled = Arrays.asList(strings);
		Collections.shuffle(scrambled);
		
		Collections.sort(scrambled, new NaturalOrderComparator());
		assertEquals(Arrays.asList(strings), scrambled);
		
	}
}
