package cis501.submission;


import cis501.*;

import java.util.Set;

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
		moveToNextStage = new boolean[5];
		dependenciesUnmet = new boolean[5];
		instructionCount = 0;
		cycleCount = 0;
    }

	private int additionalMemLatency;
	private Set<Bypass> bypasses;
	private Insn[] latches;
	private int currentMemoryTimer;
	private int instructionCount;
	private int cycleCount;
	private boolean[] moveToNextStage;
	private boolean[] dependenciesUnmet;

	private boolean latchesEmpty(){
		for(int i = 0; i < 5; i++){
			if(latches[i] != null) return false;
		}
		return true;
	}
	private void advanceLatchInsns(Iterator<Insn> instructionIterator){
		//WRITEBACK
		latches[Stage.WRITEBACK.i()] = null;

		//MEMORY
		if(moveToNextStage[Stage.MEMORY.i()]){
			latches[Stage.WRITEBACK.i()] = latches[Stage.MEMORY.i()];
			dependenciesUnmet[Stage.MEMORY.i()] = true;
		}

		//EXECUTE
		if(moveToNextStage[Stage.EXECUTE.i()]){
			latches[Stage.MEMORY.i()] = latches[Stage.EXECUTE.i()];
			dependenciesUnmet[Stage.EXECUTE.i()] = true;
		}

		//DECODE
		if(moveToNextStage[Stage.DECODE.i()]){
			latches[Stage.EXECUTE.i()] = latches[Stage.DECODE.i()];
			dependenciesUnmet[Stage.DECODE.i()] = true;
		}

		//FETCH
		if(moveToNextStage[Stage.FETCH.i()]){
			latches[Stage.DECODE.i()] = latches[Stage.FETCH.i()];

			if(instructionIterator.hasNext()){
				latches[Stage.FETCH.i()] = instructionIterator.next();
				instructionCount++;
			}
			else {
				latches[Stage.FETCH.i()] = null;
			}
		}
	}
	private void checkDelays(){
		//WRITEBACK
		//No instruction in the WRITEBACK stage will ever stall.
		moveToNextStage[Stage.WRITEBACK.i()] = true;

		//MEMORY
		//Instructions may stall due to memory latency.
		
		moveToNextStage[Stage.MEMORY.i()] = (currentMemoryTimer == additionalMemoryLatency);

		//EXECUTE
		moveToNextStage[Stage.EXECUTE.i()] = moveToNextStage[Stage.MEMORY.i()];

		//DECODE
		//DECODE may be unable to move to the next stage due to load-use issues.
		Insn w_Insn = latches[Stage.WRITEBACK.i()];
		Insn m_Insn = latches[Stage.MEMORY.i()];
		Insn x_Insn = latches[Stage.EXECUTE.i()];
		Insn d_Insn = latches[Stage.DECODE.i()];
		if((x_Insn != null) && (d_Insn != null)  && (x_Insn.mem == MemoryOp.Load)
			&& ((d_Insn.srcReg2 == x_Insn.dstReg)
				|| ((d_Insn.srcReg1 == x_Insn.dstReg) && d_Insn.mem != MemoryOp.Store))){
			moveToNextStage[Stage.DECODE.i()] = false;
		}
		else {
			//It is possible that even if there is no load-use dependency, Execute may stall.
			moveToNextStage[Stage.DECODE.i()] = moveToNextStage[Stage.EXECUTE.i()];
		}

		//FETCH
		//The only reason FETCH would be unable to move to the next stage is if DECODE is unable to move to the next stage.
		moveToNextStage[Stage.FETCH.i()] = moveToNextStage[Stage.DECODE.i()];
	}

    @Override
    public String[] groupMembers() {
        return new String[]{"rtian/Richard Zang", "Omar"};
    }

    @Override
    public void run(Iterable<Insn> ii) {
		Iterator<Insn> instructionIterator = ii.iterator();

		while(instructionIterator.hasNext() || !latchesEmpty()){
			checkDelays();
			advanceLatchInsn(instructionIterator);

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
