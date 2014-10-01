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

import org.mskcc.cbio.portal.model.ExtendedMutationMap;
import org.mskcc.cbio.portal.model.ExtendedMutation;

import java.util.ArrayList;
import java.text.DecimalFormat;

/**
 * Calculates Somatic and Germline Mutation Frequency.
 *
 * @author Ethan Cerami.
 */
public class MutationCounter {
    private int numSamplesWithSomaticMutation = 0;
    private int numSamplesWithGermlineMutation = 0;
    private int numSamplesWithMutation = 0;
    private ExtendedMutationMap mutationMap;
    private String gene;

    private int totalNumSamples;

    public MutationCounter (String gene, ExtendedMutationMap mutationMap) {
        this.gene = gene;
        this.mutationMap = mutationMap;
        totalNumSamples = mutationMap.getSampleList().size();
        for (Integer sampleId:  mutationMap.getSampleList()) {
            if (sampleIsMutated(sampleId)) {
                numSamplesWithMutation++;
                MutationStatus mutationStatus = getMutationStatus(sampleId);
                incrementCounters(mutationStatus);
            }
        }
    }

    public String getTextSummary() {
        StringBuffer summary = new StringBuffer();
        DecimalFormat percentFormat = new DecimalFormat("###,###.#%");
        summary.append("[");
        if (getGermlineMutationRate() > 0) {
            summary.append("Germline Mutation Rate:  ");
            summary.append(percentFormat.format(getGermlineMutationRate()));
            summary.append(", ");
        }
        summary.append("Somatic Mutation Rate:  ");
        summary.append(percentFormat.format(getSomaticMutationRate()));
        summary.append("]");
        return summary.toString();
    }

    public double getSomaticMutationRate() {
        return numSamplesWithSomaticMutation / (float) totalNumSamples;
    }

    public double getGermlineMutationRate() {
        return numSamplesWithGermlineMutation / (float) totalNumSamples;
    }

    public double getMutationRate() {
        return numSamplesWithMutation / (float) totalNumSamples;
    }

    private void incrementCounters(MutationStatus mutationStatus) {
        if (mutationStatus.isSampleGermlineMutated()) {
            numSamplesWithGermlineMutation++;
        }
        if (mutationStatus.isSampleSomaticallyMutated()) {
            numSamplesWithSomaticMutation++;
        }
    }

    private boolean sampleIsMutated(Integer sampleId) {
        ArrayList<ExtendedMutation> mutationList = mutationMap.getExtendedMutations(gene, sampleId);
        return mutationList != null && mutationList.size() > 0;
    }

    private MutationStatus getMutationStatus(Integer sampleId) {
        ArrayList<ExtendedMutation> mutationList = mutationMap.getExtendedMutations(gene, sampleId);
        MutationStatus mutationStatus = new MutationStatus();
        for (ExtendedMutation mutation:  mutationList) {
            setMutationStatus(mutation, mutationStatus);
        }
        return mutationStatus;
    }

    private void setMutationStatus(ExtendedMutation mutation, MutationStatus mutationStatus) {
        if (mutation.isGermlineMutation()) {
            mutationStatus.setGermlineMutated(true);
        } else {
            mutationStatus.setSomaticMutated(true);
        }
    }
}

class MutationStatus {
    private boolean germlineMutated;
    private boolean somaticMutated;

    public boolean isSampleGermlineMutated() {
        return germlineMutated;
    }

    public void setGermlineMutated(boolean germlineMutated) {
        this.germlineMutated = germlineMutated;
    }

    public boolean isSampleSomaticallyMutated() {
        return somaticMutated;
    }

    public void setSomaticMutated(boolean somaticMutated) {
        this.somaticMutated = somaticMutated;
    }
}