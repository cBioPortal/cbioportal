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