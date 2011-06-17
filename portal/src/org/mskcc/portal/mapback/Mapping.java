package org.mskcc.portal.mapback;

public class Mapping {
    private long qStart;
    private long tStart;
    private long tStop;
    private int blockSize;

    public Mapping (long qStart, long tStart, int blockSize) {
        this.qStart = qStart;
        this.tStart = tStart;
        this.blockSize = blockSize;
        this.tStop = tStart + blockSize -1;
    }

    public long getQStart() {
        return qStart;
    }

    public long getTStart() {
        return tStart;
    }

    public long getTStop() {
        return tStop;
    }

    public int getBlockSize() {
        return blockSize;
    }
}