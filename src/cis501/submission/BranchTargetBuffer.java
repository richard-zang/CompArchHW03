package cis501.submission;

import cis501.IBranchTargetBuffer;

public class BranchTargetBuffer implements IBranchTargetBuffer {

    long[] btbTableTag;
    long[] btbTableTarget;
    protected int tableSize;

    protected int indexBits;

    public BranchTargetBuffer(int indexBits) {
        this.indexBits = indexBits;
        this.tableSize = 1 << indexBits;
        btbTableTag = new long[tableSize];
        btbTableTarget = new long[tableSize];
    }

    @Override
    public long predict(long pc) {
        //If the tag in the btb table matches the pc, return the target pc.
        return (btbTableTag[maskedPC(pc)] == pc) ? btbTableTarget[maskedPC(pc)] : 0;
    }

    protected int maskedPC(long pc){
        int mask = tableSize - 1;
        return (int)pc & mask;
    }

    @Override
    public void train(long pc, long actual) {
        btbTableTag[maskedPC(pc)] = pc;
        btbTableTarget[maskedPC(pc)] = actual;
    }
}
