package cis501.submission;

import cis501.Bypass;
import cis501.IInorderPipeline;
import cis501.Insn;
import cis501.MemoryOp;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InorderPipelineAllBypassTest {

    private static IInorderPipeline sim;

    private static Insn makeInsn(int dst, int src1, int src2, MemoryOp mop) {
        return new Insn(dst, src1, src2, 1, 4, null, 0, null,
                mop, 1, 1, "synthetic");
    }

    @Before
    public void setup() {
        sim = null;
    }

    /*
		No Instructions, No MemLatency 
	 */
    @Test
    public void testFullBypass1(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(0, Bypass.FULL_BYPASS);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // 
        //
        final long expected = 0;
        assertEquals(expected, sim.getCycles());
    }

	/*
		1 Non-memory Instruction, No MemLatency
	*/
    @Test
    public void testFullBypass2(){
        List<Insn> insns = new LinkedList<>();
		insns.add(makeInsn(1,2,3, null));
        sim = new InorderPipeline(0, Bypass.FULL_BYPASS);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW|
        //
        final long expected = 6;
        assertEquals(expected, sim.getCycles());
    }

    /*
        1 Memory Instruction, No MemLatency
    */
    @Test
    public void testFullBypass3(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        sim = new InorderPipeline(0, Bypass.FULL_BYPASS);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW
        //
        final long expected = 6;
        assertEquals(expected, sim.getCycles());
    }
    /*
        1 Memory Instruction, 1 Non-Memory Instruction, No dependency, No MemLatency
    */
    @Test
    public void testFullBypass4(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
		insns.add(makeInsn(4,5,6, null));
        sim = new InorderPipeline(0, Bypass.FULL_BYPASS);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW
        //
        final long expected = 6 + 1;
        assertEquals(expected, sim.getCycles());
    }

}
