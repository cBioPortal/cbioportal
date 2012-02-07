package org.mskcc.portal.mutation.diagram.pfam;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.mskcc.cgds.model.ExtendedMutation;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Pileup of one or mutations at a single location.
 */
final class Pileup {
    private final String label;
    private final int location;
    private final int count;

    Pileup(final String label, final int location, final int count) {
        this.label = label;
        this.location = location;
        this.count = count;
    }

    /**
     * Return the label for this pileup.
     *
     * @return the label for this pileup
     */
    String getLabel() {
        return label;
    }

    /**
     * Return the location of this pileup.
     *
     * @return the location of this pileup
     */
    int getLocation() {
        return location;
    }

    /**
     * Return the count of mutations at this location.
     *
     * @return the count of mutations as this location
     */
    int getCount() {
        return count;
    }


    /**
     * Return a list of pileups for the specified list of mutations.  The list of
     * pileups may be empty but will not be null.
     *
     * @param mutations list of mutations, must not be null
     * @return a list of pileups for the specified list of mutations
     */
    static List<Pileup> pileup(final List<ExtendedMutation> mutations) {
        checkNotNull(mutations, "mutations must not be null");

        List<Pileup> pileups = Lists.newArrayList();
        ListMultimap<Integer, ExtendedMutation> mutationsByLocation = ArrayListMultimap.create();
        for (ExtendedMutation mutation : mutations) {
            mutationsByLocation.put((int) mutation.getStartPosition(), mutation);
        }
        for (Map.Entry<Integer, Collection<ExtendedMutation>> entry : mutationsByLocation.asMap().entrySet()) {
            int count = count(entry.getValue());
            if (count > 0) {
                String label = createLabel(entry.getValue());
                int location = entry.getKey();
                pileups.add(new Pileup(label, location, count));
            }
        }
        return ImmutableList.copyOf(pileups);
    }

    /**
     * Return the count of mutations suitable for display in the specified collection of mutations.
     *
     * @param mutations collection of mutations, must not be null
     * @return the count of mutations suitable for display in the specified collection of mutations
     */
    static int count(final Collection<ExtendedMutation> mutations) {
        checkNotNull(mutations, "mutations must not be null");
        // todo:  check caseId, mutationType, etc.
        return mutations.size();
    }

    /**
     * Return a label for the specified collection of mutations.
     *
     * @param mutations collection of mutations, must not be null
     * @return a label for the specified collection of mutations
     */
    static String createLabel(final Collection<ExtendedMutation> mutations) {
        checkNotNull(mutations, "mutations must not be null");
        SortedSet<String> labels = Sets.newTreeSet();
        for (ExtendedMutation mutation : mutations) {
            // todo: check caseId, mutationType, etc.
            labels.add(mutation.getAminoAcidChange());
        }
        return Joiner.on("/").join(labels);
    }
}