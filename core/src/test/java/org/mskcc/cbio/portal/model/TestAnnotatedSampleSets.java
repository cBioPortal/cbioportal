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

package org.mskcc.cbio.portal.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Tests for the Annotated Sample Sets.
 *
 * @author Ethan Cerami.
 */
public class TestAnnotatedSampleSets {
    List<SampleList> sampleSetList;

    @Before
    public void setUp() {
        sampleSetList = new ArrayList<SampleList>();
        SampleList sampleList0 = new SampleList(
            "all",
            1,
            2,
            "all gbm",
            SampleListCategory.OTHER
        );
        SampleList sampleList1 = new SampleList(
            "all",
            1,
            2,
            "all tumors",
            SampleListCategory.OTHER
        );
        SampleList sampleList2 = new SampleList(
            "all",
            1,
            2,
            "expression subset 1",
            SampleListCategory.OTHER
        );
        sampleSetList.add(sampleList0);
        sampleSetList.add(sampleList1);
        sampleSetList.add(sampleList2);
    }

    @Test
    public void test1() {
        AnnotatedSampleSets annotatedSampleSets = new AnnotatedSampleSets(
            sampleSetList
        );
        SampleList defaultSampleSet = annotatedSampleSets.getDefaultSampleList();
        assertEquals("all tumors", defaultSampleSet.getName());
    }

    @Test
    public void test2() {
        SampleList sampleList3 = new SampleList(
            "all",
            1,
            2,
            "all complete tumors",
            SampleListCategory.OTHER
        );
        sampleSetList.add(sampleList3);
        AnnotatedSampleSets annotatedSampleSets = new AnnotatedSampleSets(
            sampleSetList
        );
        SampleList defaultSampleSet = annotatedSampleSets.getDefaultSampleList();
        assertEquals("all complete tumors", defaultSampleSet.getName());
    }

    @Test
    public void test3() {
        sampleSetList = new ArrayList<SampleList>();
        AnnotatedSampleSets annotatedSampleSets = new AnnotatedSampleSets(
            sampleSetList
        );
        SampleList defaultSampleSet = annotatedSampleSets.getDefaultSampleList();
        assertEquals(null, defaultSampleSet);
    }

    @Test
    public void test4() {
        sampleSetList = new ArrayList<SampleList>();
        SampleList sampleList0 = new SampleList(
            "exp1",
            1,
            2,
            "exp1",
            SampleListCategory.OTHER
        );
        SampleList sampleList1 = new SampleList(
            "exp2",
            1,
            2,
            "exp2",
            SampleListCategory.OTHER
        );
        SampleList sampleList2 = new SampleList(
            "exp3",
            1,
            2,
            "exp3",
            SampleListCategory.OTHER
        );
        sampleSetList.add(sampleList0);
        sampleSetList.add(sampleList1);
        sampleSetList.add(sampleList2);
        AnnotatedSampleSets annotatedSampleSets = new AnnotatedSampleSets(
            sampleSetList
        );
        SampleList defaultSampleSet = annotatedSampleSets.getDefaultSampleList();
        assertEquals("exp1", defaultSampleSet.getName());
    }
}
