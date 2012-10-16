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

/**
 * Mapping between local NT position and global chromosomal position.
 * This data is usually gathered from the USCS Genome Browser.
 *
 * @author Ethan Cerami.
 */
public class Mapping {
    private long qStart;
    private long tStart;
    private long tStop;
    private int blockSize;

    /**
     * Constructor.
     *
     * @param qStart        NT position.
     * @param tStart        Global chromosomal position.
     * @param blockSize     Block size.
     */
    public Mapping (long qStart, long tStart, int blockSize) {
        this.qStart = qStart;
        this.tStart = tStart;
        this.blockSize = blockSize;
        this.tStop = tStart + blockSize -1;
    }

    /**
     * Gets the Nucleotide start.
     * @return Nucleotide start.
     */
    public long getQStart() {
        return qStart;
    }

    /**
     * Gets the Global Chromosomal strt.
     * @return Global chromosomal start.
     */
    public long getTStart() {
        return tStart;
    }

    /**
     * Gets the Global Chromosomal Stop.
     * @return Global chromosomal stop.
     */
    public long getTStop() {
        return tStop;
    }

    /**
     * Gets the bloack size.
     * @return block size.
     */
    public int getBlockSize() {
        return blockSize;
    }
}