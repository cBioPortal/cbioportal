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

import java.util.*;

/**
 * Annotated Sample Sets.
 *
 * @author Ethan Cerami
 */
public class AnnotatedSampleSets {
    private static final String ALL_COMPLETE_TUMORS = "ALL COMPLETE TUMORS";
    private static final String ALL_TUMORS = "ALL TUMORS";
    private static final String ALL = "ALL";

    private SampleList defaultSampleList;

    public AnnotatedSampleSets(List<SampleList> sampleSetList, Integer priorityLevel) {
        this.defaultSampleList = determineDefaultSampleSet(sampleSetList, priorityLevel);
    }

    public AnnotatedSampleSets(List<SampleList> sampleSetList) {
        this(sampleSetList, 0);
    }

    /**
     * Gets the "best" default sample set.
     *
     * @return "best" default sample set.
     */
    public SampleList getDefaultSampleList() {
        return defaultSampleList;
    }

    /**
     * This code makes an attempts at selecting the "best" default sample set.
     *
     *
     * @param sampleSetList List of all Sample Sets.
     * @param priorityLevel Priority level, all priorities below this one will be ignored
     * @return the "best" default sample set.
     */
    private SampleList determineDefaultSampleSet(List<SampleList> sampleSetList, Integer priorityLevel) {
        List<SampleSetWithPriority> priSampleList = new ArrayList<SampleSetWithPriority>();
        for (SampleList sampleSet : sampleSetList) {
            Integer priority = null;

            // These are the new category overrides
            switch (sampleSet.getSampleListCategory()) {
                case ALL_CASES_WITH_MUTATION_AND_CNA_DATA:
                    priority = 0;
                    break;
                case ALL_CASES_WITH_MUTATION_DATA:
                    priority = 1;
                    break;
                case ALL_CASES_WITH_CNA_DATA:
                    priority = 2;
                    break;
            }

            // If category matches none of the overrides, fallback to the old way
            if(priority == null) {
                registerPrioritySampleList(sampleSet, ALL_COMPLETE_TUMORS, 3, priSampleList);
                registerPrioritySampleList(sampleSet, ALL_TUMORS, 4, priSampleList);
                registerPrioritySampleList(sampleSet, ALL, 5, priSampleList);
            } else {
                // If we define a higher t-hold, just shift the priority level
                if(priority < priorityLevel)
                    priority += 10;

                priSampleList.add(new SampleSetWithPriority(sampleSet, priority));
            }
        }

        Collections.sort(priSampleList, new SampleSetWithPriorityComparator());
        if (priSampleList.size() > 0) {
            return priSampleList.get(0).getSampleList();
        } else {
            return failSafeReturn(sampleSetList);
        }
    }

    private SampleList failSafeReturn(List<SampleList> sampleSetList) {
        if (sampleSetList.size() > 0) {
            return sampleSetList.get(0);
        } else {
            return null;
        }
    }

    private void registerPrioritySampleList (SampleList sampleSet, String target, int priority,
            List<SampleSetWithPriority> priSampleList) {
        String name = sampleSet.getName();
        if (name.toUpperCase().startsWith(target)) {
            priSampleList.add(new SampleSetWithPriority(sampleSet, priority));
        }
    }
}

class SampleSetWithPriority {
    private SampleList sampleList;
    private int priority;

    SampleSetWithPriority(SampleList sampleList, int priority) {
        this.sampleList = sampleList;
        this.priority = priority;
    }

    public SampleList getSampleList() {
        return sampleList;
    }

    public int getPriority() {
        return priority;
    }
}

class SampleSetWithPriorityComparator implements Comparator {

    public int compare(Object o0, Object o1) {
        SampleSetWithPriority sampleSet0 = (SampleSetWithPriority) o0;
        SampleSetWithPriority sampleSet1 = (SampleSetWithPriority) o1;
        return Integer.valueOf(sampleSet0.getPriority()).compareTo(sampleSet1.getPriority());
    }
}
