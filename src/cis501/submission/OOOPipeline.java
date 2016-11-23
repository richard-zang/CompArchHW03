package cis501.submission;

import cis501.BranchPredictor;
import cis501.ICache;
import cis501.IOOOPipeline;
import cis501.Insn;

public class OOOPipeline<T extends Insn> implements IOOOPipeline<T> {

    @Override
    public String[] groupMembers() {
        return new String[]{"your", "names"};
    }

    public OOOPipeline(int pregs, int width, int robSize, BranchPredictor bp, ICache ic, ICache dc) {

    }

    @Override
    public void run(Iterable<T> uiter) {

    }

    @Override
    public long getInsns() {
        return 0;
    }

    @Override
    public long getCycles() {
        return 0;
    }
}
