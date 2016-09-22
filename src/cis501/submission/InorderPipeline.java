package cis501.submission;


import cis501.*;

import java.util.Set;
import java.util.Iterator;

/**
 * Note: Stages are declared in "reverse" order to simplify iterating over them in reverse order,
 * as the simulator does.
 */
enum Stage {
    WRITEBACK(4), MEMORY(3), EXECUTE(2), DECODE(1), FETCH(0);

    private final int index;

    private Stage(int idx) {
        this.index = idx;
    }

    /** Returns the index of this stage within the pipeline */
    public int i() {
        return index;
    }
}

public class InorderPipeline implements IInorderPipeline {

    /**
     * Create a new pipeline with the given additional memory latency.
     *
     * @param additionalMemLatency The number of extra cycles mem insns require in the M stage. If
     *                             0, mem insns require just 1 cycle in the M stage, like all other
     *                             insns. If x, mem insns require 1+x cycles in the M stage.
     * @param bypasses             Which bypasses should be modeled. For example, if this is an
     *                             empty set, then your pipeline should model no bypassing, using
     *                             stalling to resolve all data hazards.
     */
    public InorderPipeline(int additionalMemLatency, Set<Bypass> bypasses) {
        this.additionalMemLatency = additionalMemLatency;
        this.bypasses = bypasses;
        latches = new Insn[5];

        instructionCount = 0;
        cycleCount = 0;
    }

    private int additionalMemLatency;
    private Set<Bypass> bypasses;
    private Insn[] latches;
    private int currentMemoryTimer;
    private long instructionCount;
    private long cycleCount;
    private boolean mInsnCanAdvance;
    private boolean dInsnCanAdvance = false;

    private boolean latchesEmpty(){
        for(int i = 0; i < 5; i++){
            if(latches[i] != null) return false;
        }
        return true;
    }

    private void clearLatch(Stage s){
        latches[s.i()] = null;
    }

    private Insn getLatch(Stage s){
        return latches[s.i()];
    }

    private void assignLatch(Stage s1, Stage s2){
        latches[s1.i()] = latches[s2.i()];
        return;
    }

    private void assignLatch(Stage s1, Insn insn){
        latches[s1.i()] = insn;
        return;
    }

    private void advanceLatchInsns(Iterator<Insn> instructionIterator){
        Insn w_Insn = getLatch(Stage.WRITEBACK);
        Insn m_Insn = getLatch(Stage.MEMORY);
        Insn x_Insn = getLatch(Stage.EXECUTE);
        Insn d_Insn = getLatch(Stage.DECODE);
        Insn f_Insn = getLatch(Stage.FETCH);

        //WRITEBACK
        clearLatch(Stage.WRITEBACK);

        //MEMORY
        if(mInsnCanAdvance){
            assignLatch(Stage.WRITEBACK, Stage.MEMORY);
            clearLatch(Stage.MEMORY);
        }

        //EXECUTE
        if(getLatch(Stage.MEMORY) == null){
            assignLatch(Stage.MEMORY, Stage.EXECUTE);
            currentMemoryTimer = 0;
            clearLatch(Stage.EXECUTE);
        }

        //DECODE
        if(dInsnCanAdvance && getLatch(Stage.EXECUTE) == null){
            assignLatch(Stage.EXECUTE, Stage.DECODE);
            clearLatch(Stage.DECODE);
        }

        //FETCH
        if(getLatch(Stage.DECODE) == null){
            assignLatch(Stage.DECODE, Stage.FETCH);

            if(instructionIterator.hasNext()){
                assignLatch(Stage.FETCH, instructionIterator.next());
                instructionCount++;
            }
            else {
                clearLatch(Stage.FETCH);
            }
        }
    }

    private void checkDelays(){
        Insn wInsn = getLatch(Stage.WRITEBACK);
        Insn mInsn = getLatch(Stage.MEMORY);
        Insn xInsn = getLatch(Stage.EXECUTE);
        Insn dInsn = getLatch(Stage.DECODE);


        //WRITEBACK
        //No instruction in the WRITEBACK stage will ever stall.

        //MEMORY
        //Instructions may stall due to memory latency.
        mInsnCanAdvance = true;
        if(mInsn != null && mInsn.mem != null)
            mInsnCanAdvance = (currentMemoryTimer > additionalMemLatency);

        //EXECUTE
        //EXECUTE will never stall.

        //DECODE
        //DECODE may be unable to move to the next stage due to load-to-use issues.

        // This depedency can be solved by a MX bypass.
        boolean mxDep = dataDependecy(dInsn, xInsn);
        // This depedency can be solved by a WX bypass.
        boolean wxDep = dataDependecy(dInsn, mInsn);
        // This depedency can be solved by a WM bypass. This happens when
        // we have a load or a store so we wait until the mem stage is done.
        boolean wmDep = dataDependecy(dInsn, xInsn) &&
            (xInsn.mem == MemoryOp.Store || xInsn.mem == MemoryOp.Load);
        // This dependency happens when the 2nd insn is at write state
        // and 1st isns must way 2nd to finish.
        boolean wdDep = dataDependecy(dInsn, wInsn);

        // Check our bypasses and see if they would resolve any dependencies.
        if( bypasses.contains(Bypass.MX) ){
            mxDep = false;
        }
        if( bypasses.contains(Bypass.WX) ){
            wxDep = false;
        }
        // TODO: Consider case where we write to adress input instead of data input!
        // Slide #39.
        if( bypasses.contains(Bypass.WM) ){
            wmDep = false;
        }

        dInsnCanAdvance = ! (mxDep || wxDep || mxDep || wdDep);

        if( bypasses.equals(Bypass.FULL_BYPASS) ){
            dInsnCanAdvance =
                !(dInsn != null && xInsn != null &&
                  xInsn.mem == MemoryOp.Load &&
                  (dInsn.srcReg2 == xInsn.dstReg ||
                  (dInsn.srcReg1 == xInsn.dstReg && dInsn.mem != MemoryOp.Store)));
        }

        //FETCH
        //FETCH will never cause a stall.
    }

    /**
     * Returns whether we have some data dependency between two instructions
     * on the latches.
     */
    private boolean dataDependecy(Insn decodeInsn, Insn otherInsn){
        if(decodeInsn == null || otherInsn == null)
            return false;
        return decodeInsn.srcReg1 == otherInsn.dstReg ||
            decodeInsn.srcReg2 == otherInsn.dstReg;
    }

    @Override
    public String[] groupMembers() {
        return new String[]{"rtian/Richard Zang", "Omar Navarro Leija"};
    }

    @Override
    public void run(Iterable<Insn> ii) {
        Iterator<Insn> instructionIterator = ii.iterator();
        while(instructionIterator.hasNext() || !latchesEmpty()){
            checkDelays();
            advanceLatchInsns(instructionIterator);

            currentMemoryTimer++;
            cycleCount++;
        }
    }

    @Override
    public long getInsns() {
        return instructionCount;
    }

    @Override
    public long getCycles() {
        return cycleCount;
    }
}
