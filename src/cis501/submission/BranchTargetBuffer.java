package cis501.submission;

import cis501.IBranchTargetBuffer;

public class BranchTargetBuffer implements IBranchTargetBuffer {

	long[] btbTableTag;
	long[] btbTableTarget;

	protected int indexBits;

    public BranchTargetBuffer(int indexBits) {
		this.indexBits = indexBits;
		btbTableTag = new long[1 << indexBits];
		btbTableTarget = new long[1 << indexBits];
    }

    @Override
    public long predict(long pc) {
		//If the tag in the btb table matches the pc, return the target pc.
        return (btbTableTag[maskedPC(pc)] == pc) ? btbTableTarget[maskedPC(pc)] : 0;
    }

	protected int maskedPC(long pc){
		int mask = (1 << indexBits) - 1;
		return (int)pc & mask;
	}

    @Override
    public void train(long pc, long actual) {
		btbTableTarget[maskedPC(pc)] = pc;
		btbTableTarget[maskedPC(pc)] = actual;
    }
}
