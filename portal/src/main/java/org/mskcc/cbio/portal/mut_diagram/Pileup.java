package org.mskcc.cbio.portal.mut_diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mskcc.cbio.cgds.model.ExtendedMutation;

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

    public Pileup(final String label, final int location, final int count) {
        this.label = label;
        this.location = location;
        this.count = count;
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
            String label = mutation.getAminoAcidChange();
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
        for (Map.Entry<Integer, Collection<ExtendedMutation>> entry : mutationsByLocation.asMap().entrySet()) {
            int location = entry.getKey();
            String label = Joiner.on("/").join(labels.get(location));
            Set<String> caseIds = Sets.newHashSet();
            for (ExtendedMutation mutation : entry.getValue()) {
                caseIds.add(mutation.getCaseId() + ":" + mutation.getAminoAcidChange());
            }
            pileups.add(new Pileup(label, location, caseIds.size()));
        }
        return ImmutableList.copyOf(pileups);
    }
}