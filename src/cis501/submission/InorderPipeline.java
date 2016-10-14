package cis501.submission;

import cis501.*;

import java.util.Set;
import java.util.Iterator;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Note: Stages are declared in "reverse" order to simplify iterating over them in
 * reverse order, as the simulator does.
 */
enum Stage {
    WRITEBACK(4), MEMORY(3), EXECUTE(2), DECODE(1), FETCH(0);

    private static Stage[] vals = values();
    private final int index;

    Stage(int idx) {
        this.index = idx;
    }

    /** Returns the index of this stage within the pipeline */
    public int i() {
        return index;
    }

    /** Returns the next stage in the pipeline, e.g., next after Fetch is Decode */
    public Stage next() {
        return vals[(this.ordinal() - 1 + vals.length) % vals.length];
    }
}

public class InorderPipeline implements IInorderPipeline {

    /**
     * Create a new pipeline with the given additional memory latency.
     *
     * @param additionalMemLatency: The number of extra cycles mem insns require in the
     * M stage. If 0, mem insns require just 1 cycle in the M stage, like all other insns.
     * If x, mem insns require 1+x cycles in the M stage.
     * @param bypasses: Which bypasses should be modeled. For example, if this is an empty
     * set, then your pipeline should model no bypassing, using stalling to resolve all
     * data hazards.
     */
    public InorderPipeline(int additionalMemLatency, Set<Bypass> bypasses) {
        this.additionalMemLatency = additionalMemLatency;
        this.bypasses = bypasses;
        latches = new Insn[5];

        instructionCount = 0;
        cycleCount = 0;
    }
    //====================================================================================
    /**
     * Create a new pipeline with the additional memory latency and branch predictor. The
     * pipeline should model full bypassing (MX, Wx, WM).
     * @param additionalMemLatency see InorderPipeline(int, Set<Bypass>)
     * @param bp                   the branch predictor to use
     */
    public InorderPipeline(int additionalMemLatency, BranchPredictor bp){
        this.bp = bp;
        branchPredictionOn = true;
        this.additionalMemLatency = additionalMemLatency;
        this.bypasses = Bypass.FULL_BYPASS;
        latches = new Insn[5];
        instructionCount = 0;
        cycleCount = 0;

        return;
    }

    public InorderPipeline(BranchPredictor bp, ICache ic, ICache dc){
    }

    //====================================================================================
    private int additionalMemLatency;
    private Set<Bypass> bypasses;
    private Insn[] latches;

    /** Stalling counter for memory latency issues. */
    private int currentMemoryTimer;
    private int currentBranchTimer;

    private long instructionCount;
    private long cycleCount;
    private boolean mInsnCanAdvance;
    /** Decode instruction can't advance due to data hazards. */
    private boolean dInsnCanAdvance = false;
    /** Decode instruction can't advance due to branch mispredictions. */
    private boolean dInsnCanAdvanceBranch;
    private final int branchStallTime = 2;

    private boolean branchStalling = false;
    private boolean insnUsedForTraining = false;

    BranchPredictor bp = null;
    private boolean branchPredictionOn = false;

    /** Prediction made during fetch of whether this instruction was a branch.
        At most we may want to know what was predicted for the past 3 instructions.
        So we keep this information in our queue of fun. */
    private Queue<Long> predictedPC = new LinkedList<>();
    //====================================================================================
    private boolean latchesEmpty(){
        for(int i = 0; i < 5; i++){
            if(latches[i] != null) return false;
        }
        return true;
    }
    //====================================================================================
    private void clearLatch(Stage s){
        latches[s.i()] = null;
    }
    //====================================================================================
    private Insn getLatch(Stage s){
        return latches[s.i()];
    }
    //====================================================================================
    private void assignLatch(Stage s1, Stage s2){
        latches[s1.i()] = latches[s2.i()];
        return;
    }
    //====================================================================================
    private void assignLatch(Stage s1, Insn insn){
        latches[s1.i()] = insn;
        return;
    }
    //====================================================================================
    /**
     * Main function to check if an instruction can move forward in the pipeline.
     * updates latches as well as clears latches based on values of
     * mInsnCanAdvance, dInsnCanAdvance.
     */
    private void advanceLatchInsns(Iterator<Insn> instructionIterator){
        //WRITEBACK
        clearLatch(Stage.WRITEBACK);

        //MEMORY
        if(mInsnCanAdvance){
            assignLatch(Stage.WRITEBACK, Stage.MEMORY);
            clearLatch(Stage.MEMORY);
        }

        //EXECUTE
        if(getLatch(Stage.MEMORY) == null){
            // Move instruction along.
            assignLatch(Stage.MEMORY, Stage.EXECUTE);
            currentMemoryTimer = 0;
            clearLatch(Stage.EXECUTE);
            branchStalling = false;
            insnUsedForTraining = false;
        }

        //DECODE
        if(dInsnCanAdvance && dInsnCanAdvanceBranch && getLatch(Stage.EXECUTE) == null){
            assignLatch(Stage.EXECUTE, Stage.DECODE);
            clearLatch(Stage.DECODE);
        }

        //FETCH
        if(getLatch(Stage.DECODE) == null){
            assignLatch(Stage.DECODE, Stage.FETCH);

            // Get next instruction from list.
            if(instructionIterator.hasNext()){
                assignLatch(Stage.FETCH, instructionIterator.next());
                Insn fetchInsn = getLatch(Stage.FETCH);
                instructionCount++;

                // Call branch predictor on our new instruction. Attempt to find
                // whether it's a branch and whether it's taken/not taken.
                if(branchPredictionOn){
                    predictedPC.add(bp.predict(fetchInsn.pc, fetchInsn.fallthroughPC()));
                }
            }
            else{
                clearLatch(Stage.FETCH);
            }
        }
    }
    //====================================================================================
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
        // If this instruction was a branch, we know the correct address it jumped. We
        // check our prediction and stall if necessary.
        dInsnCanAdvanceBranch = true;

        if(branchPredictionOn && xInsn != null){
            long thisPredictedPC = predictedPC.peek();

            // We have a branch! Train and check for branch mispredictions.
            Direction branchDir = xInsn.branch;
            if(branchDir != null){
                long actualNextPC = (branchDir == Direction.Taken) ?
                    xInsn.branchTarget :
                    xInsn.fallthroughPC();

                // Train on the first time only. Not anytime after that.
                if(!branchStalling) bp.train(xInsn.pc, actualNextPC, branchDir);

                // Stall if our prediction was wrong.
                boolean predictionCorrect = (branchDir == Direction.Taken) ?
                    thisPredictedPC == xInsn.branchTarget :
                    thisPredictedPC == xInsn.fallthroughPC();

                // We are stalling but are stuck because of a memory stall.
                // We don't want to restart the timer.
                if(!predictionCorrect && !branchStalling){
                    currentBranchTimer = 0;
                    branchStalling = true;
                }
            }
            if(mInsnCanAdvance) predictedPC.remove();
        }

        if(branchPredictionOn)
            dInsnCanAdvanceBranch = (currentBranchTimer >= branchStallTime);
        // We are stalling due to a branch misprediction. These instructions
        // should not be here yet, so ignore any sort of dependency that could happen.
        if(!dInsnCanAdvanceBranch) return;

        //DECODE
        //DECODE may be unable to move to the next stage due to load-to-use issues.

        // Check for any sort of dependency.
        boolean dxDep = dataDependecy(dInsn, xInsn);
        boolean dmDep = dataDependecy(dInsn, mInsn);

        // This depedency can be solved by a MX bypass.
        boolean mxDep = dataDependecy(dInsn, xInsn) &&
            xInsn.mem != MemoryOp.Load;

        // This depedency can be solved by a WX bypass.
        boolean wxDep = dataDependecy(dInsn, mInsn);

        // This depedency can be solved by a WM bypass.
        boolean wmDep = dInsn != null && xInsn != null &&
            dInsn.mem == MemoryOp.Store && xInsn.dstReg != dInsn.srcReg2;

        // Check our bypasses and see if they would resolve any dependencies:
        if(bypasses.contains(Bypass.MX) && mxDep)
            dxDep = false;
        // We can only use WX bypass if we know the instruction in memory
        // will be all done in the next cycle. Else, it must keep stalling...
        if(bypasses.contains(Bypass.WX) && wxDep && mInsnCanAdvance)
            dmDep = false;
        // Case where we have a Load followed by a store. No problem! B)
        // Check to make sure we are not writing to dInsn's second register!
        if(bypasses.contains(Bypass.WM) && wmDep)
            dxDep = false;

        dInsnCanAdvance = ! (dmDep || dxDep);

        //FETCH
        //FETCH will never cause a stall.
    }
    //====================================================================================
    /**
     * Returns whether we have some data dependency between two instructions
     * on the latches.
     */
    private boolean dataDependecy(Insn decodeInsn, Insn otherInsn){
        if(decodeInsn == null || otherInsn == null)
            return false;
        // Compare destination of first to source of second.
        boolean srcToDst = decodeInsn.srcReg1 == otherInsn.dstReg ||
            decodeInsn.srcReg2 == otherInsn.dstReg;
        // Store is a special case where we must check if the destinations
        // match!
        boolean loadCase = decodeInsn.mem == MemoryOp.Store &&
            otherInsn.dstReg == decodeInsn.dstReg;

        return (srcToDst || loadCase);
    }
    //====================================================================================
    @Override
    public String[] groupMembers() {
        return new String[]{"rtian/Richard Zang", "Omar Navarro Leija"};
    }
    //====================================================================================
    @Override
    public void run(Iterable<Insn> ii) {
        Iterator<Insn> instructionIterator = ii.iterator();
        while(instructionIterator.hasNext() || !latchesEmpty()){
            checkDelays();
            advanceLatchInsns(instructionIterator);

            currentMemoryTimer++;
            currentBranchTimer++;
            cycleCount++;
        }
    }
    //====================================================================================
    @Override
    public long getInsns() {
        return instructionCount;
    }
    //====================================================================================
    @Override
    public long getCycles() {
        return cycleCount;
    }
    //====================================================================================
}
