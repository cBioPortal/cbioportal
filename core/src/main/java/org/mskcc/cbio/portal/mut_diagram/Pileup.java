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

package org.mskcc.cbio.portal.mut_diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.model.ExtendedMutation;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * Pileup of one or mutations at a single location.
 */
public final class Pileup {
    private static final Logger logger = Logger.getLogger(Pileup.class);
    private final String label;
    private final int location;
    private final int count;
	private final int missenseCount;

    public Pileup(final String label,
		    final int location,
		    final int count,
		    final int missenseCount) {
        this.label = label;
        this.location = location;
        this.count = count;
	    this.missenseCount = missenseCount;
    }

    /**
     * Return the label for this pileup.
     *
     * @return the label for this pileup
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return the location of this pileup.
     *
     * @return the location of this pileup
     */
    public int getLocation() {
        return location;
    }

    /**
     * Return the count of mutations at this location.
     *
     * @return the count of mutations as this location
     */
    public int getCount() {
        return count;
    }

	/**
	 * Return the count of missense mutations at this location.
	 *
	 * @return the count of missense mutations as this location
	 */
	public int getMissenseCount() {
		return missenseCount;
	}

    /**
     * Return a list of pileups for the specified list of mutations.  The list of
     * pileups may be empty but will not be null.
     *
     * @param mutations list of mutations, must not be null
     * @return a list of pileups for the specified list of mutations
     */
    public static List<Pileup> pileup(final List<ExtendedMutation> mutations) {
        checkNotNull(mutations, "mutations must not be null");

        List<Pileup> pileups = Lists.newArrayList();
        SetMultimap<Integer, String> labels = HashMultimap.create();
        ListMultimap<Integer, ExtendedMutation> mutationsByLocation = ArrayListMultimap.create();
        for (ExtendedMutation mutation : mutations) {
            String label = mutation.getProteinChange();
            if (label != null) {
                try {
                    int location = Integer.valueOf(label.replaceAll("[A-Za-z\\.*]+", ""));
                    labels.put(location, label);
                    mutationsByLocation.put(location, mutation);
                }
                catch (NumberFormatException e) {
                    logger.warn("ignoring extended mutation " + label + ", no location information");
                }
            }
        }

        for (Map.Entry<Integer, Collection<ExtendedMutation>> entry : mutationsByLocation.asMap().entrySet())
        {
            int location = entry.getKey();
            String label = Joiner.on("/").join(labels.get(location));
	        int missenseCount = 0;
            Set<String> caseIds = Sets.newHashSet();

	        for (ExtendedMutation mutation : entry.getValue())
	        {
                caseIds.add(mutation.getCaseId() + ":" + mutation.getProteinChange());

		        if (mutation.getMutationType() != null &&
		            mutation.getMutationType().toLowerCase().contains("missense"))
		        {
			        missenseCount++;
		        }
            }

            pileups.add(new Pileup(label, location, caseIds.size(), missenseCount));
        }

        return ImmutableList.copyOf(pileups);
    }
}