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

package org.mskcc.cbio.portal.mapback;

import java.util.ArrayList;

/**
 * Maps Back from a global chromosomal location to a local nucleotide position, based
 * on a GenBank Record.
 *
 * @author Ethan Cerami.
 */
public class MapBack {
    private MapBackGene mapBackGene;
    private ArrayList<Mapping> mappingList;
    private long globalMutationLocation;
    private long ntPosition = -1;

    /**
     * Constructor.
     *
     * @param mapBackGene       MapBackGene Object.
     * @param globalLocation    Global chromosomal location.
     */
    public MapBack(MapBackGene mapBackGene, long globalLocation) {
        this.mapBackGene = mapBackGene;
        this.mappingList = mapBackGene.getMappingList();
        this.globalMutationLocation = globalLocation;
        mapBack();
    }

    private void mapBack() {
        Mapping lastMapping = mappingList.get(mappingList.size()-1);
        long sequenceLen = lastMapping.getQStart() + lastMapping.getBlockSize();

        Mapping matchedMapping = null;
        for (int i =0; i< mappingList.size(); i++) {
            Mapping mapping = mappingList.get(i);
            if (globalMutationLocation > mapping.getTStart()
                    && globalMutationLocation < mapping.getTStop()) {
                matchedMapping = mapping;
            }
        }

        if (matchedMapping != null) {
            long offset = globalMutationLocation - matchedMapping.getTStart();
            long plusQuery = matchedMapping.getQStart() + offset;
            ntPosition = plusQuery;
            if (!mapBackGene.isForwardStrand()) {
                ntPosition = sequenceLen - plusQuery +1;
            }
        }
    }

    public long getNtPositionWhereMutationOccurs() {
        return ntPosition;
    }

    public char getBpWhereMutationOccurs() {
        return mapBackGene.getFullDna().charAt((int) ntPosition-1);
    }
}